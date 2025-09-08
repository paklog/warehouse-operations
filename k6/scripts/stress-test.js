// Stress Test - Test system limits and breaking points
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { getEnvironment, config } from '../config/environments.js';
import {
  commonHeaders,
  checkHealthResponse,
  checkPackageCreationResponse,
  checkPickListResponse,
  generatePackageData,
  generatePickConfirmationData,
  selectRandom,
  sleepWithJitter
} from './utils.js';

// Custom metrics for stress testing
const errorRate = new Rate('errors');
const responseTimeP99 = new Trend('response_time_p99');
const concurrentUsers = new Counter('concurrent_users');

// Stress test configuration - Aggressive load ramping
export const options = {
  stages: [
    { duration: '2m', target: 20 },   // Warm up
    { duration: '3m', target: 50 },   // Ramp to moderate load
    { duration: '3m', target: 100 },  // Ramp to high load
    { duration: '5m', target: 100 },  // Sustain high load
    { duration: '3m', target: 150 },  // Push to stress level
    { duration: '5m', target: 150 },  // Sustain stress level
    { duration: '2m', target: 200 },  // Peak stress
    { duration: '3m', target: 200 },  // Sustain peak stress
    { duration: '3m', target: 0 },    // Recovery
  ],
  thresholds: {
    // More lenient thresholds for stress testing
    http_req_duration: ['p(95)<5000', 'p(99)<10000'], // Allow higher response times
    http_req_failed: ['rate<0.10'],    // Error rate should be less than 10%
    errors: ['rate<0.15'],             // Custom error rate
    
    // Stress-specific thresholds
    'http_req_duration{endpoint:health}': ['p(95)<2000'], // Health should remain fast
    'http_req_failed{endpoint:health}': ['rate<0.05'],    // Health should be reliable
  },
  tags: {
    testType: 'stress',
    environment: __ENV.ENVIRONMENT || 'local'
  },
  // Disable default thresholds for stress testing
  noConnectionReuse: false,
  userAgent: 'K6-Stress-Test/1.0',
};

const environment = getEnvironment();
const BASE_URL = environment.baseUrl;

export function setup() {
  console.log(`🔥 Starting Stress Test on ${environment.description}`);
  console.log(`📍 Base URL: ${BASE_URL}`);
  console.log(`💪 Peak Load: 200 concurrent users`);
  console.log(`⚠️  This test is designed to stress the system - expect some failures`);
  
  // Health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`, { timeout: '10s' });
  if (healthResponse.status !== 200) {
    throw new Error(`❌ Service not available before stress test`);
  }
  
  console.log('✅ Pre-stress health check passed');
  return { 
    baseUrl: BASE_URL,
    testData: config.testData
  };
}

export default function(data) {
  concurrentUsers.add(1);
  
  // More aggressive scenario distribution for stress testing
  const scenario = Math.random();
  
  try {
    if (scenario < 0.5) {
      // 50% - Heavy package operations
      intensivePackageOperations(data);
    } else if (scenario < 0.8) {
      // 30% - Heavy pick list operations
      intensivePickListOperations(data);
    } else {
      // 20% - Mixed high-frequency operations
      rapidFireOperations(data);
    }
  } catch (error) {
    errorRate.add(1);
    console.error(`❌ Error in user scenario: ${error.message}`);
  }
  
  // Reduced sleep time to increase stress
  sleep(sleepWithJitter(0.5, 50));
}

function intensivePackageOperations(data) {
  // Rapid package creation and operations
  for (let i = 0; i < 3; i++) {
    const startTime = Date.now();
    
    const packageData = generatePackageData(data.testData);
    const response = http.post(
      `${BASE_URL}/api/packages`,
      JSON.stringify(packageData),
      {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'stress-create' },
        timeout: '10s'
      }
    );
    
    const duration = Date.now() - startTime;
    responseTimeP99.add(duration);
    
    const success = check(response, {
      'stress package creation': (r) => r.status === 200 || r.status === 201,
      'response time acceptable': (r) => r.timings.duration < 15000,
    });
    
    if (!success) {
      errorRate.add(1);
    }
    
    // Minimal sleep between requests
    if (i < 2) sleep(0.1);
  }
}

function intensivePickListOperations(data) {
  const pickerId = selectRandom(data.testData.pickerIds);
  
  // Rapid pick list queries and operations
  const operations = [
    // Get pick lists by picker
    () => {
      const response = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
        headers: commonHeaders,
        tags: { endpoint: 'picklists', operation: 'stress-picker' },
        timeout: '8s'
      });
      return checkStressResponse(response, 'picklist-picker');
    },
    
    // Get pick lists by status
    () => {
      const status = selectRandom(['PENDING', 'IN_PROGRESS', 'COMPLETED']);
      const response = http.get(`${BASE_URL}/api/picklists/status/${status}`, {
        headers: commonHeaders,
        tags: { endpoint: 'picklists', operation: 'stress-status' },
        timeout: '8s'
      });
      return checkStressResponse(response, 'picklist-status');
    },
    
    // Get next pick list
    () => {
      const response = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}/next`, {
        headers: commonHeaders,
        tags: { endpoint: 'picklists', operation: 'stress-next' },
        timeout: '8s'
      });
      return checkStressResponse(response, 'picklist-next');
    },
    
    // Attempt pick confirmation
    () => {
      const pickListId = `stress-test-${Date.now()}`;
      const pickData = generatePickConfirmationData(data.testData);
      const response = http.post(
        `${BASE_URL}/api/picklists/${pickListId}/confirm-pick`,
        JSON.stringify(pickData),
        {
          headers: commonHeaders,
          tags: { endpoint: 'picklists', operation: 'stress-confirm' },
          timeout: '10s'
        }
      );
      return checkStressResponse(response, 'pick-confirm');
    }
  ];
  
  // Execute 2-4 operations rapidly
  const numOps = Math.floor(Math.random() * 3) + 2;
  for (let i = 0; i < numOps; i++) {
    const operation = selectRandom(operations);
    const success = operation();
    
    if (!success) {
      errorRate.add(1);
    }
    
    sleep(0.05); // Very short sleep between operations
  }
}

function rapidFireOperations(data) {
  // Mix of all operations in rapid succession
  const operations = [];
  
  // Health checks (should remain responsive)
  operations.push(() => {
    const startTime = Date.now();
    const response = http.get(`${BASE_URL}/actuator/health`, {
      headers: commonHeaders,
      tags: { endpoint: 'health', operation: 'stress-health' },
      timeout: '5s'
    });
    
    responseTimeP99.add(Date.now() - startTime);
    
    const success = check(response, {
      'health responsive under stress': (r) => r.status === 200,
      'health fast under stress': (r) => r.timings.duration < 3000,
    });
    
    if (!success) errorRate.add(1);
    return success;
  });
  
  // Package operations
  operations.push(() => {
    const packageData = generatePackageData(data.testData);
    const response = http.post(
      `${BASE_URL}/api/packages`,
      JSON.stringify(packageData),
      {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'rapid-create' },
        timeout: '12s'
      }
    );
    
    const success = checkStressResponse(response, 'rapid-package');
    if (!success) errorRate.add(1);
    return success;
  });
  
  // Pick list operations
  operations.push(() => {
    const pickerId = selectRandom(data.testData.pickerIds);
    const response = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
      headers: commonHeaders,
      tags: { endpoint: 'picklists', operation: 'rapid-picker' },
      timeout: '8s'
    });
    
    const success = checkStressResponse(response, 'rapid-picklist');
    if (!success) errorRate.add(1);
    return success;
  });
  
  // Execute multiple operations with minimal delays
  for (let i = 0; i < 4; i++) {
    const operation = selectRandom(operations);
    operation();
    sleep(0.02); // Almost no delay
  }
}

function checkStressResponse(response, operation) {
  return check(response, {
    [`${operation} - not server error`]: (r) => r.status < 500 || r.status === 503, // 503 acceptable under stress
    [`${operation} - response received`]: (r) => r.body !== null,
    [`${operation} - reasonable timeout`]: (r) => r.timings.duration < 20000,
  });
}

export function teardown(data) {
  console.log('🔥 Stress Test completed');
  console.log('');
  console.log('📊 Stress Test Results:');
  console.log(`   - Total Error Rate: ${(errorRate.rate * 100).toFixed(2)}%`);
  console.log(`   - Peak Concurrent Users: ${concurrentUsers.count}`);
  
  // Post-stress health check
  console.log('🏥 Performing post-stress health check...');
  
  let healthCheckAttempts = 0;
  let healthCheckPassed = false;
  
  while (healthCheckAttempts < 5 && !healthCheckPassed) {
    const healthResponse = http.get(`${BASE_URL}/actuator/health`, { timeout: '15s' });
    
    if (healthResponse.status === 200) {
      console.log('✅ Post-stress health check: PASSED');
      console.log('   System recovered successfully from stress test');
      healthCheckPassed = true;
    } else {
      healthCheckAttempts++;
      console.log(`⚠️  Post-stress health check attempt ${healthCheckAttempts}/5: FAILED (${healthResponse.status})`);
      
      if (healthCheckAttempts < 5) {
        console.log('   Waiting 10 seconds before retry...');
        sleep(10);
      }
    }
  }
  
  if (!healthCheckPassed) {
    console.log('❌ System may need manual intervention to recover');
    console.log('   Consider checking logs and restarting services if needed');
  }
  
  console.log('');
  console.log('💡 Stress Test Analysis Tips:');
  console.log('   - Error rates up to 15% are acceptable during peak stress');
  console.log('   - Response times may degrade significantly under stress');
  console.log('   - Focus on system recovery and no data corruption');
  console.log('   - Check application logs for any critical errors');
}