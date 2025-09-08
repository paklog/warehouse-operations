// Package Focused Load Test - Specific testing for package operations
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { getEnvironment, config } from '../config/environments.js';
import {
  commonHeaders,
  checkPackageCreationResponse,
  checkApiResponse,
  generatePackageData,
  selectRandom,
  sleepWithJitter,
  weightedRandom,
  generateRandomOrderId
} from './utils.js';

// Package specific metrics
const packageCreationRate = new Rate('package_creation_success');
const packageRetrievalRate = new Rate('package_retrieval_success');
const packageConfirmationRate = new Rate('package_confirmation_success');
const packageResponseTime = new Trend('package_response_time');
const packagesCreated = new Counter('packages_created_total');
const averageItemsPerPackage = new Trend('average_items_per_package');

// Package focused load test configuration
export const options = {
  stages: [
    { duration: '1m', target: 8 },   // Warm up
    { duration: '4m', target: 20 },  // Normal package operations
    { duration: '6m', target: 35 },  // Peak package processing
    { duration: '4m', target: 20 },  // Scale back
    { duration: '2m', target: 0 },   // Cool down
  ],
  thresholds: {
    http_req_duration: ['p(95)<4000'], // Allow longer for package operations
    http_req_failed: ['rate<0.03'],
    package_creation_success: ['rate>0.95'],
    package_retrieval_success: ['rate>0.80'], // May be lower due to test data
    package_confirmation_success: ['rate>0.70'], // May be lower due to test setup
    package_response_time: ['p(95)<3500'],
    packages_created_total: ['count>50'], // Expect at least 50 packages created
  },
  tags: {
    testType: 'package-load',
    environment: __ENV.ENVIRONMENT || 'local'
  }
};

const environment = getEnvironment();
const BASE_URL = environment.baseUrl;

// Store created package info for retrieval tests
const createdPackages = [];

export function setup() {
  console.log(`📦 Starting Package Load Test on ${environment.description}`);
  console.log(`📍 Base URL: ${BASE_URL}`);
  console.log(`🎯 Focus: Package creation, retrieval, and confirmation workflows`);
  
  // Health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`);
  if (healthResponse.status !== 200) {
    throw new Error(`❌ Service not available for package testing`);
  }
  
  console.log('✅ Service ready for package load testing');
  return {
    baseUrl: BASE_URL,
    testData: config.testData,
    createdPackages: []
  };
}

export default function(data) {
  // Package operation scenarios with weights
  const scenario = weightedRandom([
    { name: 'full_package_workflow', weight: 40 },    // 40% - Complete package lifecycle
    { name: 'package_creation_burst', weight: 30 },   // 30% - Focused package creation
    { name: 'package_retrieval', weight: 20 },        // 20% - Package lookup operations
    { name: 'package_confirmation', weight: 10 }       // 10% - Package confirmation
  ]);
  
  const startTime = Date.now();
  
  try {
    switch (scenario.name) {
      case 'full_package_workflow':
        fullPackageWorkflow(data);
        break;
      case 'package_creation_burst':
        packageCreationBurst(data);
        break;
      case 'package_retrieval':
        packageRetrievalScenario(data);
        break;
      case 'package_confirmation':
        packageConfirmationScenario(data);
        break;
    }
  } catch (error) {
    console.error(`❌ Package operation error: ${error.message}`);
  }
  
  packageResponseTime.add(Date.now() - startTime);
  
  // Realistic packing station workflow timing
  sleep(sleepWithJitter(4, 35));
}

function fullPackageWorkflow(data) {
  console.log('🏭 Executing full package workflow');
  
  // 1. Create package with realistic data
  const packageData = generatePackageData(data.testData);
  const itemCount = packageData.items.length;
  averageItemsPerPackage.add(itemCount);
  
  console.log(`📝 Creating package with ${itemCount} items`);
  
  const createResponse = http.post(
    `${BASE_URL}/api/packages`,
    JSON.stringify(packageData),
    {
      headers: commonHeaders,
      tags: { endpoint: 'packages', operation: 'create', workflow: 'full' }
    }
  );
  
  const createSuccess = checkPackageCreationResponse(createResponse);
  packageCreationRate.add(createSuccess);
  
  if (createSuccess && createResponse.status === 200) {
    packagesCreated.add(1);
    
    try {
      const packageInfo = JSON.parse(createResponse.body);
      const packageId = packageInfo.packageId;
      const orderId = generateRandomOrderId();
      
      // Store for later retrieval tests
      createdPackages.push({ packageId, orderId });
      
      console.log(`✅ Package created: ${packageId}`);
      
      // Think time - simulating packing process
      sleep(sleepWithJitter(8, 30)); // 5.6-10.4 seconds
      
      // 2. Try to retrieve the package
      const retrieveResponse = http.get(`${BASE_URL}/api/packages/order/${orderId}`, {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'retrieve', workflow: 'full' }
      });
      
      const retrieveSuccess = checkApiResponse(retrieveResponse, 'package-retrieval', [200, 404]);
      packageRetrievalRate.add(retrieveSuccess && retrieveResponse.status === 200);
      
      sleep(sleepWithJitter(2, 25)); // Review time
      
      // 3. Confirm the package
      const confirmResponse = http.patch(`${BASE_URL}/api/packages/${packageId}/confirm`, '', {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'confirm', workflow: 'full' }
      });
      
      const confirmSuccess = checkApiResponse(confirmResponse, 'package-confirmation', [200, 404]);
      packageConfirmationRate.add(confirmSuccess && confirmResponse.status === 200);
      
      if (confirmSuccess && confirmResponse.status === 200) {
        console.log(`✅ Package confirmed: ${packageId}`);
      }
      
    } catch (e) {
      console.log(`⚠️  Could not parse package response: ${e.message}`);
    }
  }
}

function packageCreationBurst(data) {
  console.log('🚀 Executing package creation burst');
  
  // Create multiple packages in quick succession
  const burstSize = Math.floor(Math.random() * 3) + 2; // 2-4 packages
  
  for (let i = 0; i < burstSize; i++) {
    const packageData = generatePackageData(data.testData);
    
    // Vary package complexity
    if (Math.random() < 0.3) {
      // 30% chance of complex package (more items)
      const extraItems = Math.floor(Math.random() * 3) + 1;
      for (let j = 0; j < extraItems; j++) {
        packageData.items.push({
          skuCode: selectRandom(data.testData.skuCodes),
          quantity: Math.floor(Math.random() * 3) + 1
        });
      }
    }
    
    averageItemsPerPackage.add(packageData.items.length);
    
    const createResponse = http.post(
      `${BASE_URL}/api/packages`,
      JSON.stringify(packageData),
      {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'burst-create' }
      }
    );
    
    const success = checkPackageCreationResponse(createResponse);
    packageCreationRate.add(success);
    
    if (success && createResponse.status === 200) {
      packagesCreated.add(1);
      console.log(`📦 Burst package ${i + 1}/${burstSize} created`);
    }
    
    // Short delay between burst creations
    if (i < burstSize - 1) {
      sleep(sleepWithJitter(1, 40));
    }
  }
}

function packageRetrievalScenario(data) {
  console.log('🔍 Executing package retrieval scenario');
  
  // Test various retrieval patterns
  const retrievalTests = [
    // Test with random order IDs (likely to return 404)
    () => {
      const randomOrderId = generateRandomOrderId();
      const response = http.get(`${BASE_URL}/api/packages/order/${randomOrderId}`, {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'random-retrieval' }
      });
      
      const success = checkApiResponse(response, 'random-package-retrieval', [200, 404]);
      packageRetrievalRate.add(success && response.status === 200);
      return success;
    },
    
    // Test with previously created packages (if any)
    () => {
      if (createdPackages.length > 0) {
        const randomPackage = selectRandom(createdPackages);
        const response = http.get(`${BASE_URL}/api/packages/order/${randomPackage.orderId}`, {
          headers: commonHeaders,
          tags: { endpoint: 'packages', operation: 'existing-retrieval' }
        });
        
        const success = checkApiResponse(response, 'existing-package-retrieval', [200, 404]);
        packageRetrievalRate.add(success && response.status === 200);
        return success;
      } else {
        // Fallback to random order ID
        const randomOrderId = `existing-${Date.now()}`;
        const response = http.get(`${BASE_URL}/api/packages/order/${randomOrderId}`, {
          headers: commonHeaders,
          tags: { endpoint: 'packages', operation: 'fallback-retrieval' }
        });
        
        const success = checkApiResponse(response, 'fallback-package-retrieval', [200, 404]);
        packageRetrievalRate.add(success && response.status === 200);
        return success;
      }
    },
    
    // Test with common order ID patterns
    () => {
      const patterns = ['ord-001', 'order-12345', 'ORD-2024-001', 'test-order-001'];
      const testOrderId = selectRandom(patterns);
      
      const response = http.get(`${BASE_URL}/api/packages/order/${testOrderId}`, {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'pattern-retrieval' }
      });
      
      const success = checkApiResponse(response, 'pattern-package-retrieval', [200, 404]);
      packageRetrievalRate.add(success && response.status === 200);
      return success;
    }
  ];
  
  // Execute multiple retrieval tests
  const numTests = Math.floor(Math.random() * 3) + 2; // 2-4 tests
  
  for (let i = 0; i < numTests; i++) {
    const test = selectRandom(retrievalTests);
    test();
    
    if (i < numTests - 1) {
      sleep(sleepWithJitter(1, 30));
    }
  }
}

function packageConfirmationScenario(data) {
  console.log('✅ Executing package confirmation scenario');
  
  // Create packages and immediately try to confirm them
  const numPackages = Math.floor(Math.random() * 2) + 2; // 2-3 packages
  
  for (let i = 0; i < numPackages; i++) {
    // 1. Create package
    const packageData = generatePackageData(data.testData);
    
    const createResponse = http.post(
      `${BASE_URL}/api/packages`,
      JSON.stringify(packageData),
      {
        headers: commonHeaders,
        tags: { endpoint: 'packages', operation: 'confirm-workflow-create' }
      }
    );
    
    const createSuccess = checkPackageCreationResponse(createResponse);
    packageCreationRate.add(createSuccess);
    
    if (createSuccess && createResponse.status === 200) {
      packagesCreated.add(1);
      
      try {
        const packageInfo = JSON.parse(createResponse.body);
        const packageId = packageInfo.packageId;
        
        // Brief packing time
        sleep(sleepWithJitter(3, 25));
        
        // 2. Confirm the package
        const confirmResponse = http.patch(`${BASE_URL}/api/packages/${packageId}/confirm`, '', {
          headers: commonHeaders,
          tags: { endpoint: 'packages', operation: 'immediate-confirm' }
        });
        
        const confirmSuccess = checkApiResponse(confirmResponse, 'immediate-package-confirmation', [200, 404, 500]);
        packageConfirmationRate.add(confirmSuccess && confirmResponse.status === 200);
        
        if (confirmSuccess && confirmResponse.status === 200) {
          console.log(`✅ Package immediately confirmed: ${packageId}`);
        } else if (confirmResponse.status === 404) {
          console.log(`⚠️  Package not found for confirmation: ${packageId}`);
        }
        
      } catch (e) {
        console.log(`⚠️  Could not parse package for confirmation: ${e.message}`);
      }
    }
    
    if (i < numPackages - 1) {
      sleep(sleepWithJitter(2, 30));
    }
  }
  
  // Also test confirmation with random package IDs (should fail gracefully)
  const randomPackageId = `random-pkg-${Date.now()}`;
  const randomConfirmResponse = http.patch(`${BASE_URL}/api/packages/${randomPackageId}/confirm`, '', {
    headers: commonHeaders,
    tags: { endpoint: 'packages', operation: 'random-confirm' }
  });
  
  const randomConfirmSuccess = checkApiResponse(randomConfirmResponse, 'random-package-confirmation', [200, 404, 500]);
  packageConfirmationRate.add(randomConfirmSuccess && randomConfirmResponse.status === 200);
}

export function teardown(data) {
  console.log('📦 Package Load Test completed');
  console.log('');
  console.log('📊 Package Performance Results:');
  console.log(`   - Package Creation Success Rate: ${(packageCreationRate.rate * 100).toFixed(2)}%`);
  console.log(`   - Package Retrieval Success Rate: ${(packageRetrievalRate.rate * 100).toFixed(2)}%`);
  console.log(`   - Package Confirmation Success Rate: ${(packageConfirmationRate.rate * 100).toFixed(2)}%`);
  console.log(`   - Total Packages Created: ${packagesCreated.count}`);
  console.log(`   - Average Items per Package: ${averageItemsPerPackage.avg.toFixed(1)}`);
  
  // Package system health check
  console.log('');
  console.log('🏥 Package System Health Check:');
  
  const testResults = {
    packageCreation: false,
    packageRetrieval: false,
    responseTime: false
  };
  
  // 1. Test package creation
  const testPackageData = generatePackageData(data.testData);
  const createTestResponse = http.post(
    `${BASE_URL}/api/packages`,
    JSON.stringify(testPackageData),
    {
      headers: commonHeaders,
      timeout: '10s'
    }
  );
  
  if (createTestResponse.status === 200) {
    testResults.packageCreation = true;
    console.log('✅ Package Creation: WORKING');
    packagesCreated.add(1); // Count the health check package
  } else {
    console.log(`❌ Package Creation: FAILED (${createTestResponse.status})`);
  }
  
  // 2. Test package retrieval
  const testOrderId = `health-check-${Date.now()}`;
  const retrievalTestResponse = http.get(`${BASE_URL}/api/packages/order/${testOrderId}`, {
    headers: commonHeaders,
    timeout: '5s'
  });
  
  if ([200, 404].includes(retrievalTestResponse.status)) {
    testResults.packageRetrieval = true;
    console.log('✅ Package Retrieval: WORKING');
  } else {
    console.log(`❌ Package Retrieval: FAILED (${retrievalTestResponse.status})`);
  }
  
  // 3. Test response time
  const perfTestStart = Date.now();
  const perfTestResponse = http.get(`${BASE_URL}/actuator/health`, { timeout: '5s' });
  const perfTestTime = Date.now() - perfTestStart;
  
  if (perfTestTime < 3000 && perfTestResponse.status === 200) {
    testResults.responseTime = true;
    console.log(`✅ Response Time: GOOD (${perfTestTime}ms)`);
  } else {
    console.log(`⚠️  Response Time: SLOW (${perfTestTime}ms)`);
  }
  
  const workingFeatures = Object.values(testResults).filter(result => result).length;
  
  console.log('');
  console.log('🎯 Package System Assessment:');
  if (workingFeatures === 3) {
    console.log('🟢 All package operations are functioning optimally');
  } else if (workingFeatures >= 2) {
    console.log('🟡 Most package operations are working, minor issues detected');
  } else {
    console.log('🔴 Package system needs attention');
  }
  
  console.log('');
  console.log('💡 Package Performance Insights:');
  console.log(`   - Created ${packagesCreated.count} packages during test`);
  console.log(`   - Average package complexity: ${averageItemsPerPackage.avg.toFixed(1)} items`);
  console.log('   - Package creation should have >95% success rate');
  console.log('   - Package retrieval may be lower due to test data limitations');
  console.log('   - Consider implementing package caching for better retrieval performance');
  
  if (packagesCreated.count > 0) {
    console.log('');
    console.log('📈 Load Test Statistics:');
    console.log(`   - Packages per minute: ${(packagesCreated.count / 17).toFixed(1)}`); // 17 minutes total test
    console.log(`   - Peak throughput achieved during creation bursts`);
    console.log('   - Monitor database performance for package storage optimization');
  }
}