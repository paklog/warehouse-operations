// Spike Test - Sudden traffic spikes to test system resilience
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { getEnvironment, config } from '../config/environments.js';
import {
  commonHeaders,
  checkHealthResponse,
  checkPackageCreationResponse,
  selectRandom,
  generatePackageData,
  sleepWithJitter
} from './utils.js';

// Custom metrics for spike testing
const spikeErrorRate = new Rate('spike_errors');
const spikeResponseTime = new Trend('spike_response_time');
const recoveryTime = new Trend('recovery_time');

// Spike test configuration - Sudden load increases
export const options = {
  stages: [
    { duration: '1m', target: 5 },     // Baseline load
    { duration: '30s', target: 5 },    // Maintain baseline
    { duration: '10s', target: 100 },  // SPIKE! 20x increase
    { duration: '1m', target: 100 },   // Hold spike
    { duration: '10s', target: 5 },    // Drop back to baseline
    { duration: '2m', target: 5 },     // Recovery period
    { duration: '10s', target: 150 },  // Second spike (even higher)
    { duration: '30s', target: 150 },  // Brief hold
    { duration: '10s', target: 5 },    // Drop back
    { duration: '2m', target: 5 },     // Final recovery
    { duration: '30s', target: 0 },    // Shutdown
  ],
  thresholds: {
    // Spike-specific thresholds - more lenient during spikes
    http_req_duration: ['p(95)<10000'], // Allow up to 10s during spikes
    http_req_failed: ['rate<0.20'],     // Up to 20% errors acceptable during spikes
    spike_errors: ['rate<0.25'],        // Spike-specific error tracking
    
    // Recovery thresholds - should be stricter
    'http_req_duration{phase:recovery}': ['p(95)<3000'],
    'http_req_failed{phase:recovery}': ['rate<0.05'],
  },
  tags: {
    testType: 'spike',
    environment: __ENV.ENVIRONMENT || 'local'
  }
};

const environment = getEnvironment();
const BASE_URL = environment.baseUrl;

export function setup() {
  console.log(`⚡ Starting Spike Test on ${environment.description}`);
  console.log(`📍 Base URL: ${BASE_URL}`);
  console.log(`📊 Pattern: 5→100→5→150→5 users with sudden transitions`);
  console.log(`🎯 Goal: Test system resilience to sudden load changes`);
  
  // Pre-test health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`);
  if (healthResponse.status !== 200) {
    throw new Error(`❌ Service not healthy before spike test`);
  }
  
  console.log('✅ Pre-spike health check passed');
  return { 
    baseUrl: BASE_URL,
    testData: config.testData,
    startTime: Date.now()
  };
}

export default function(data) {
  const testPhase = determineTestPhase();
  const startTime = Date.now();
  
  try {
    if (testPhase === 'spike') {
      spikeScenario(data);
    } else if (testPhase === 'recovery') {
      recoveryScenario(data);
    } else {
      baselineScenario(data);
    }
  } catch (error) {
    spikeErrorRate.add(1);
    console.error(`❌ Spike test error: ${error.message}`);
  }
  
  const duration = Date.now() - startTime;
  spikeResponseTime.add(duration, { phase: testPhase });
  
  // Adjust sleep based on test phase
  if (testPhase === 'spike') {
    sleep(sleepWithJitter(0.2, 100)); // Very short sleep during spikes
  } else if (testPhase === 'recovery') {
    sleep(sleepWithJitter(1, 30)); // Normal sleep during recovery
  } else {
    sleep(sleepWithJitter(2, 20)); // Longer sleep during baseline
  }
}

function determineTestPhase() {
  // Determine test phase based on current time in the test
  const currentTime = Date.now();
  const testStartTime = __ENV.TEST_START_TIME ? parseInt(__ENV.TEST_START_TIME) : currentTime;
  const elapsedSeconds = (currentTime - testStartTime) / 1000;
  
  // Based on the stages configuration
  if (elapsedSeconds >= 90 && elapsedSeconds <= 210) {
    return 'spike'; // First spike period
  } else if (elapsedSeconds >= 210 && elapsedSeconds <= 330) {
    return 'recovery'; // First recovery period
  } else if (elapsedSeconds >= 340 && elapsedSeconds <= 370) {
    return 'spike'; // Second spike period
  } else if (elapsedSeconds >= 380 && elapsedSeconds <= 500) {
    return 'recovery'; // Second recovery period
  }
  
  return 'baseline';
}

function spikeScenario(data) {
  // Aggressive operations during spike
  console.log('⚡ Executing spike scenario');
  
  // Multiple rapid operations to stress the system
  const operations = [
    () => {
      // Rapid package creation
      const packageData = generatePackageData(data.testData);
      const response = http.post(
        `${BASE_URL}/api/packages`,
        JSON.stringify(packageData),
        {
          headers: commonHeaders,
          tags: { endpoint: 'packages', phase: 'spike' },
          timeout: '15s'
        }
      );
      
      return check(response, {
        'spike package creation - not timeout': (r) => r.status !== 0,
        'spike package creation - server responsive': (r) => r.status < 500 || r.status === 503,
      });
    },
    
    () => {
      // Health check during spike
      const response = http.get(`${BASE_URL}/actuator/health`, {
        headers: commonHeaders,
        tags: { endpoint: 'health', phase: 'spike' },
        timeout: '8s'
      });
      
      return check(response, {
        'spike health - responsive': (r) => r.status === 200 || r.status === 503,
        'spike health - timely': (r) => r.timings.duration < 10000,
      });
    },
    
    () => {
      // Pick list operations during spike
      const pickerId = selectRandom(data.testData.pickerIds);
      const response = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
        headers: commonHeaders,
        tags: { endpoint: 'picklists', phase: 'spike' },
        timeout: '10s'
      });
      
      return check(response, {
        'spike picklist - not server error': (r) => r.status < 500 || r.status === 503,
        'spike picklist - response received': (r) => r.body !== null,
      });
    }
  ];
  
  // Execute 2-3 operations rapidly
  const numOps = Math.floor(Math.random() * 2) + 2;
  for (let i = 0; i < numOps; i++) {
    const operation = selectRandom(operations);
    const success = operation();
    
    if (!success) {
      spikeErrorRate.add(1);
    }
    
    // Almost no delay between operations during spike
    if (i < numOps - 1) {
      sleep(0.05);
    }
  }
}

function recoveryScenario(data) {
  // Monitor recovery after spike
  console.log('🏥 Executing recovery scenario');
  const recoveryStart = Date.now();
  
  // Test if system is recovering properly
  const healthResponse = http.get(`${BASE_URL}/actuator/health`, {
    headers: commonHeaders,
    tags: { endpoint: 'health', phase: 'recovery' },
    timeout: '10s'
  });
  
  const healthSuccess = check(healthResponse, {
    'recovery health - status ok': (r) => r.status === 200,
    'recovery health - fast response': (r) => r.timings.duration < 3000,
    'recovery health - system up': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.status === 'UP';
      } catch (e) {
        return false;
      }
    }
  });
  
  if (healthSuccess) {
    recoveryTime.add(Date.now() - recoveryStart);
  }
  
  sleep(sleepWithJitter(0.5, 20));
  
  // Test normal operations during recovery
  const packageData = generatePackageData(data.testData);
  const packageResponse = http.post(
    `${BASE_URL}/api/packages`,
    JSON.stringify(packageData),
    {
      headers: commonHeaders,
      tags: { endpoint: 'packages', phase: 'recovery' },
      timeout: '8s'
    }
  );
  
  const packageSuccess = check(packageResponse, {
    'recovery package - normal operation': (r) => r.status === 200,
    'recovery package - reasonable time': (r) => r.timings.duration < 5000,
  });
  
  if (!healthSuccess || !packageSuccess) {
    spikeErrorRate.add(1);
  }
}

function baselineScenario(data) {
  // Normal operations during baseline periods
  console.log('📊 Executing baseline scenario');
  
  // Health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`, {
    headers: commonHeaders,
    tags: { endpoint: 'health', phase: 'baseline' },
    timeout: '5s'
  });
  
  checkHealthResponse(healthResponse);
  
  sleep(sleepWithJitter(1, 20));
  
  // Normal package operation
  const packageData = generatePackageData(data.testData);
  const packageResponse = http.post(
    `${BASE_URL}/api/packages`,
    JSON.stringify(packageData),
    {
      headers: commonHeaders,
      tags: { endpoint: 'packages', phase: 'baseline' },
      timeout: '5s'
    }
  );
  
  const success = checkPackageCreationResponse(packageResponse);
  
  if (!success) {
    spikeErrorRate.add(1);
  }
}

export function teardown(data) {
  console.log('⚡ Spike Test completed');
  console.log('');
  console.log('📊 Spike Test Results:');
  console.log(`   - Overall Error Rate: ${(spikeErrorRate.rate * 100).toFixed(2)}%`);
  console.log(`   - Test Duration: ${Math.round((Date.now() - data.startTime) / 1000)}s`);
  
  // Post-spike comprehensive health check
  console.log('🏥 Performing post-spike system assessment...');
  
  const assessmentResults = {
    health: false,
    responseTime: false,
    functionality: false
  };
  
  // 1. Health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`, { timeout: '15s' });
  if (healthResponse.status === 200) {
    assessmentResults.health = true;
    console.log('✅ Health Check: PASSED');
  } else {
    console.log(`❌ Health Check: FAILED (${healthResponse.status})`);
  }
  
  sleep(2);
  
  // 2. Response time check
  const startTime = Date.now();
  const perfResponse = http.get(`${BASE_URL}/actuator/info`, { timeout: '10s' });
  const responseTime = Date.now() - startTime;
  
  if (responseTime < 2000 && perfResponse.status === 200) {
    assessmentResults.responseTime = true;
    console.log(`✅ Response Time: GOOD (${responseTime}ms)`);
  } else {
    console.log(`⚠️  Response Time: SLOW (${responseTime}ms)`);
  }
  
  sleep(2);
  
  // 3. Functionality check
  const testPackageData = generatePackageData(data.testData);
  const funcResponse = http.post(
    `${BASE_URL}/api/packages`,
    JSON.stringify(testPackageData),
    {
      headers: commonHeaders,
      timeout: '10s'
    }
  );
  
  if (funcResponse.status === 200) {
    assessmentResults.functionality = true;
    console.log('✅ Functionality: WORKING');
  } else {
    console.log(`❌ Functionality: IMPAIRED (${funcResponse.status})`);
  }
  
  console.log('');
  console.log('🎯 Spike Test Assessment:');
  
  const passedChecks = Object.values(assessmentResults).filter(result => result).length;
  const totalChecks = Object.keys(assessmentResults).length;
  
  if (passedChecks === totalChecks) {
    console.log('🟢 System Status: EXCELLENT - Fully recovered from spikes');
    console.log('   The system handled traffic spikes gracefully');
  } else if (passedChecks >= 2) {
    console.log('🟡 System Status: GOOD - Minor impact from spikes');
    console.log('   Most functionality recovered, minor issues detected');
  } else {
    console.log('🔴 System Status: NEEDS ATTENTION - Significant impact from spikes');
    console.log('   System may need intervention or tuning');
  }
  
  console.log('');
  console.log('💡 Spike Test Insights:');
  console.log('   - Spikes test how well your system handles sudden load increases');
  console.log('   - Look for graceful degradation rather than complete failures');
  console.log('   - Fast recovery time is more important than perfect spike handling');
  console.log('   - Consider implementing circuit breakers and rate limiting');
}