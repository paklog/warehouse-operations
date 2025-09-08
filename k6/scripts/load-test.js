// Load Test - Normal expected load simulation
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
  sleepWithJitter,
  weightedRandom
} from './utils.js';

// Custom metrics
const packageCreationRate = new Rate('package_creation_success');
const pickConfirmationRate = new Rate('pick_confirmation_success');
const apiResponseTime = new Trend('api_response_time');

// Test configuration for load testing
export const options = {
  stages: [
    { duration: '2m', target: 10 },  // Ramp up to 10 users
    { duration: '5m', target: 10 },  // Stay at 10 users for 5 minutes
    { duration: '2m', target: 20 },  // Ramp up to 20 users
    { duration: '5m', target: 20 },  // Stay at 20 users for 5 minutes
    { duration: '2m', target: 0 },   // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests should be below 2s
    http_req_failed: ['rate<0.02'],    // Error rate should be less than 2%
    package_creation_success: ['rate>0.95'],
    pick_confirmation_success: ['rate>0.90'],
    api_response_time: ['p(95)<3000'],
  },
  tags: {
    testType: 'load',
    environment: __ENV.ENVIRONMENT || 'local'
  }
};

const environment = getEnvironment();
const BASE_URL = environment.baseUrl;

export function setup() {
  console.log(`🚀 Starting Load Test on ${environment.description}`);
  console.log(`📍 Base URL: ${BASE_URL}`);
  console.log(`👥 Target: 10-20 concurrent users over 16 minutes`);
  
  // Health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`);
  if (healthResponse.status !== 200) {
    throw new Error(`❌ Service not available. Health check failed.`);
  }
  
  console.log('✅ Service health check passed - starting load test');
  return { 
    baseUrl: BASE_URL,
    testData: config.testData
  };
}

export default function(data) {
  // Simulate realistic user behavior with weighted scenarios
  const scenario = weightedRandom([
    { name: 'package_workflow', weight: 40 },      // 40% package operations
    { name: 'picklist_workflow', weight: 35 },     // 35% pick list operations
    { name: 'monitoring_check', weight: 15 },      // 15% monitoring
    { name: 'mixed_operations', weight: 10 }       // 10% mixed operations
  ]);
  
  switch (scenario.name) {
    case 'package_workflow':
      packageWorkflow(data);
      break;
    case 'picklist_workflow':
      picklistWorkflow(data);
      break;
    case 'monitoring_check':
      monitoringWorkflow(data);
      break;
    case 'mixed_operations':
      mixedOperationsWorkflow(data);
      break;
  }
  
  // Realistic think time between operations
  sleep(sleepWithJitter(2, 30));
}

function packageWorkflow(data) {
  console.log('📦 Executing package workflow...');
  
  // 1. Create package
  const packageData = generatePackageData(data.testData);
  const createStart = Date.now();
  
  const createResponse = http.post(
    `${BASE_URL}/api/packages`,
    JSON.stringify(packageData),
    {
      headers: commonHeaders,
      tags: { endpoint: 'packages', operation: 'create' }
    }
  );
  
  apiResponseTime.add(Date.now() - createStart);
  const createSuccess = checkPackageCreationResponse(createResponse);
  packageCreationRate.add(createSuccess);
  
  if (createSuccess && createResponse.status === 200) {
    sleep(sleepWithJitter(1, 20));
    
    // 2. Try to retrieve package (may not be implemented)
    const orderId = `test-order-${Date.now()}`;
    const retrieveResponse = http.get(`${BASE_URL}/api/packages/order/${orderId}`, {
      headers: commonHeaders,
      tags: { endpoint: 'packages', operation: 'retrieve' }
    });
    
    checkPickListResponse(retrieveResponse, 'package-retrieve');
    
    // 3. If package exists, try to confirm it
    if (retrieveResponse.status === 200) {
      sleep(sleepWithJitter(0.5, 20));
      
      const confirmResponse = http.patch(`${BASE_URL}/api/packages/test-package/confirm`, '', {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'confirm' }
      });
      
      checkPickListResponse(confirmResponse, 'package-confirm');
    }
  }
}

function picklistWorkflow(data) {
  console.log('📝 Executing pick list workflow...');
  
  const pickerId = selectRandom(data.testData.pickerIds);
  
  // 1. Get next pick list for picker
  const nextPickResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}/next`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'next' }
  });
  
  checkPickListResponse(nextPickResponse, 'picklist-next');
  
  sleep(sleepWithJitter(1, 25));
  
  // 2. Get pick lists by status
  const statuses = ['PENDING', 'IN_PROGRESS', 'COMPLETED'];
  const status = selectRandom(statuses);
  
  const statusResponse = http.get(`${BASE_URL}/api/picklists/status/${status}`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'status' }
  });
  
  checkPickListResponse(statusResponse, 'picklist-status');
  
  sleep(sleepWithJitter(0.5, 20));
  
  // 3. Get pick lists by picker
  const pickerResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'picker' }
  });
  
  checkPickListResponse(pickerResponse, 'picklist-picker');
  
  // 4. Simulate pick confirmation (may fail if pick list doesn't exist)
  const pickListId = `test-picklist-${Date.now()}`;
  const pickData = generatePickConfirmationData(data.testData);
  
  sleep(sleepWithJitter(2, 30)); // Think time for picking
  
  const confirmStart = Date.now();
  const confirmResponse = http.post(
    `${BASE_URL}/api/picklists/${pickListId}/confirm-pick`,
    JSON.stringify(pickData),
    {
      headers: commonHeaders,
      tags: { endpoint: 'picklists', operation: 'confirm-pick' }
    }
  );
  
  apiResponseTime.add(Date.now() - confirmStart);
  const confirmSuccess = confirmResponse.status === 200;
  pickConfirmationRate.add(confirmSuccess);
  
  checkPickListResponse(confirmResponse, 'pick-confirm');
}

function monitoringWorkflow(data) {
  console.log('📊 Executing monitoring workflow...');
  
  // 1. Health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`, {
    headers: commonHeaders,
    tags: { endpoint: 'health' }
  });
  
  checkHealthResponse(healthResponse);
  
  sleep(sleepWithJitter(0.5, 15));
  
  // 2. Info endpoint
  const infoResponse = http.get(`${BASE_URL}/actuator/info`, {
    headers: commonHeaders,
    tags: { endpoint: 'info' }
  });
  
  checkPickListResponse(infoResponse, 'info');
  
  sleep(sleepWithJitter(0.5, 15));
  
  // 3. Metrics endpoint
  const metricsResponse = http.get(`${BASE_URL}/actuator/metrics`, {
    headers: commonHeaders,
    tags: { endpoint: 'metrics' }
  });
  
  checkPickListResponse(metricsResponse, 'metrics');
}

function mixedOperationsWorkflow(data) {
  console.log('🔄 Executing mixed operations workflow...');
  
  // Random mix of operations to simulate real user behavior
  const operations = [
    () => {
      const healthResponse = http.get(`${BASE_URL}/actuator/health`, {
        headers: commonHeaders,
        tags: { endpoint: 'health' }
      });
      checkHealthResponse(healthResponse);
    },
    () => {
      const packageData = generatePackageData(data.testData);
      const createResponse = http.post(
        `${BASE_URL}/api/packages`,
        JSON.stringify(packageData),
        {
          headers: commonHeaders,
          tags: { endpoint: 'packages' }
        }
      );
      const success = checkPackageCreationResponse(createResponse);
      packageCreationRate.add(success);
    },
    () => {
      const pickerId = selectRandom(data.testData.pickerIds);
      const pickerResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
        headers: commonHeaders,
        tags: { endpoint: 'picklists' }
      });
      checkPickListResponse(pickerResponse, 'picklist-picker');
    }
  ];
  
  // Execute 2-3 random operations
  const numOps = Math.floor(Math.random() * 2) + 2;
  for (let i = 0; i < numOps; i++) {
    const operation = selectRandom(operations);
    operation();
    
    if (i < numOps - 1) {
      sleep(sleepWithJitter(0.8, 25));
    }
  }
}

export function teardown(data) {
  console.log('🏁 Load Test completed');
  console.log('📊 Final Results Summary:');
  console.log(`   - Package Creation Success Rate: ${packageCreationRate.rate * 100}%`);
  console.log(`   - Pick Confirmation Success Rate: ${pickConfirmationRate.rate * 100}%`);
  console.log('   - Check detailed metrics above for performance analysis');
}