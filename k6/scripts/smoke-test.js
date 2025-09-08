// Smoke Test - Minimal load to verify basic functionality
import http from 'k6/http';
import { check, sleep } from 'k6';
import { getEnvironment, config } from '../config/environments.js';
import { 
  commonHeaders, 
  checkHealthResponse, 
  checkApiResponse,
  generatePackageData,
  selectRandom
} from './utils.js';

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 1 }, // Ramp up to 1 user
    { duration: '1m', target: 1 },  // Stay at 1 user
    { duration: '30s', target: 0 }, // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'], // 95% of requests should be below 3s
    http_req_failed: ['rate<0.05'],    // Error rate should be less than 5%
  },
  tags: {
    testType: 'smoke',
    environment: __ENV.ENVIRONMENT || 'local'
  }
};

const environment = getEnvironment();
const BASE_URL = environment.baseUrl;

export function setup() {
  console.log(`🚀 Starting Smoke Test on ${environment.description}`);
  console.log(`📍 Base URL: ${BASE_URL}`);
  
  // Verify service is available
  const healthResponse = http.get(`${BASE_URL}/actuator/health`, {
    headers: commonHeaders,
    timeout: '10s'
  });
  
  if (healthResponse.status !== 200) {
    throw new Error(`❌ Service not available. Health check failed with status: ${healthResponse.status}`);
  }
  
  console.log('✅ Service health check passed');
  return { baseUrl: BASE_URL };
}

export default function(data) {
  const testStartTime = Date.now();
  
  // Test 1: Health Check
  testHealthEndpoint();
  sleep(1);
  
  // Test 2: Package Operations
  testPackageOperations();
  sleep(1);
  
  // Test 3: Pick List Operations  
  testPickListOperations();
  sleep(1);
  
  // Test 4: Monitoring Endpoints
  testMonitoringEndpoints();
  
  console.log(`✅ Smoke test cycle completed in ${Date.now() - testStartTime}ms`);
}

function testHealthEndpoint() {
  console.log('🔍 Testing Health Endpoint...');
  
  const response = http.get(`${BASE_URL}/actuator/health`, {
    headers: commonHeaders,
    tags: { endpoint: 'health' }
  });
  
  checkHealthResponse(response);
}

function testPackageOperations() {
  console.log('📦 Testing Package Operations...');
  
  // Create package
  const packageData = generatePackageData(config.testData);
  const createResponse = http.post(
    `${BASE_URL}/api/packages`,
    JSON.stringify(packageData),
    {
      headers: commonHeaders,
      tags: { endpoint: 'packages', operation: 'create' }
    }
  );
  
  const createSuccess = checkApiResponse(createResponse, 'package-creation', 200);
  
  if (createSuccess && createResponse.status === 200) {
    try {
      const packageInfo = JSON.parse(createResponse.body);
      console.log(`✅ Package created with ID: ${packageInfo.packageId}`);
      
      // Try to retrieve the package (this might fail if not implemented)
      sleep(0.5);
      const retrieveResponse = http.get(`${BASE_URL}/api/packages/order/test-order`, {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'retrieve' }
      });
      
      checkApiResponse(retrieveResponse, 'package-retrieval', [200, 404]); // 404 is acceptable
      
    } catch (e) {
      console.log(`⚠️  Could not parse package response: ${e.message}`);
    }
  }
}

function testPickListOperations() {
  console.log('📝 Testing Pick List Operations...');
  
  // Test get pick lists by status
  const statusResponse = http.get(`${BASE_URL}/api/picklists/status/PENDING`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'status' }
  });
  
  checkApiResponse(statusResponse, 'picklist-status', [200, 404]);
  
  // Test get pick lists by picker
  const pickerId = selectRandom(config.testData.pickerIds);
  const pickerResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'picker' }
  });
  
  checkApiResponse(pickerResponse, 'picklist-picker', [200, 404]);
  
  // Test get next pick list for picker
  const nextResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}/next`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'next' }
  });
  
  checkApiResponse(nextResponse, 'picklist-next', [200, 204, 404]);
}

function testMonitoringEndpoints() {
  console.log('📊 Testing Monitoring Endpoints...');
  
  // Test info endpoint
  const infoResponse = http.get(`${BASE_URL}/actuator/info`, {
    headers: commonHeaders,
    tags: { endpoint: 'info' }
  });
  
  checkApiResponse(infoResponse, 'info', [200, 404]);
  
  // Test metrics endpoint
  const metricsResponse = http.get(`${BASE_URL}/actuator/metrics`, {
    headers: commonHeaders,
    tags: { endpoint: 'metrics' }
  });
  
  checkApiResponse(metricsResponse, 'metrics', [200, 404]);
}

export function teardown(data) {
  console.log('🏁 Smoke Test completed');
  console.log('📊 Check the results above for any failures');
}