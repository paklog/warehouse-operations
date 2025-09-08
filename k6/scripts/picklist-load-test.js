// Pick List Focused Load Test - Specific testing for pick list operations
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { getEnvironment, config } from '../config/environments.js';
import {
  commonHeaders,
  checkPickListResponse,
  generatePickConfirmationData,
  selectRandom,
  sleepWithJitter,
  weightedRandom
} from './utils.js';

// Pick list specific metrics
const pickListRetrievalRate = new Rate('picklist_retrieval_success');
const pickConfirmationRate = new Rate('pick_confirmation_success');
const pickListResponseTime = new Trend('picklist_response_time');
const operationsPerSecond = new Counter('picklist_operations_total');

// Pick list focused load test configuration
export const options = {
  stages: [
    { duration: '1m', target: 5 },   // Warm up
    { duration: '3m', target: 15 },  // Normal pick list load
    { duration: '5m', target: 25 },  // Peak pick list operations
    { duration: '3m', target: 15 },  // Scale back
    { duration: '2m', target: 0 },   // Cool down
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.05'],
    picklist_retrieval_success: ['rate>0.90'],
    pick_confirmation_success: ['rate>0.85'], // May be lower due to missing pick lists
    picklist_response_time: ['p(95)<2500'],
  },
  tags: {
    testType: 'picklist-load',
    environment: __ENV.ENVIRONMENT || 'local'
  }
};

const environment = getEnvironment();
const BASE_URL = environment.baseUrl;

export function setup() {
  console.log(`📝 Starting Pick List Load Test on ${environment.description}`);
  console.log(`📍 Base URL: ${BASE_URL}`);
  console.log(`🎯 Focus: Pick list operations and picker workflows`);
  
  // Health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`);
  if (healthResponse.status !== 200) {
    throw new Error(`❌ Service not available for pick list testing`);
  }
  
  console.log('✅ Service ready for pick list load testing');
  return {
    baseUrl: BASE_URL,
    testData: config.testData
  };
}

export default function(data) {
  operationsPerSecond.add(1);
  
  // Pick list operation scenarios with weights
  const scenario = weightedRandom([
    { name: 'picker_workflow', weight: 35 },        // 35% - Complete picker workflow
    { name: 'status_monitoring', weight: 25 },      // 25% - Status monitoring operations
    { name: 'pick_confirmation', weight: 20 },      // 20% - Pick confirmation operations
    { name: 'next_task_polling', weight: 15 },      // 15% - Next task polling
    { name: 'bulk_queries', weight: 5 }             // 5% - Bulk query operations
  ]);
  
  const startTime = Date.now();
  
  try {
    switch (scenario.name) {
      case 'picker_workflow':
        completePickerWorkflow(data);
        break;
      case 'status_monitoring':
        statusMonitoringScenario(data);
        break;
      case 'pick_confirmation':
        pickConfirmationScenario(data);
        break;
      case 'next_task_polling':
        nextTaskPollingScenario(data);
        break;
      case 'bulk_queries':
        bulkQueriesScenario(data);
        break;
    }
  } catch (error) {
    console.error(`❌ Pick list operation error: ${error.message}`);
  }
  
  pickListResponseTime.add(Date.now() - startTime);
  
  // Realistic picker think time
  sleep(sleepWithJitter(3, 40));
}

function completePickerWorkflow(data) {
  console.log('👨‍💼 Executing complete picker workflow');
  
  const pickerId = selectRandom(data.testData.pickerIds);
  
  // 1. Picker logs in and gets next task
  const nextTaskResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}/next`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'next', workflow: 'complete' }
  });
  
  const nextTaskSuccess = checkPickListResponse(nextTaskResponse, 'next-task');
  pickListRetrievalRate.add(nextTaskSuccess);
  
  sleep(sleepWithJitter(2, 30)); // Think time - reviewing task
  
  // 2. Get all assigned pick lists for context
  const assignedResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'assigned', workflow: 'complete' }
  });
  
  const assignedSuccess = checkPickListResponse(assignedResponse, 'assigned-picklists');
  pickListRetrievalRate.add(assignedSuccess);
  
  sleep(sleepWithJitter(5, 25)); // Think time - walking to location and picking
  
  // 3. Confirm pick(s) - simulate multiple items
  const numPicks = Math.floor(Math.random() * 3) + 1; // 1-3 picks
  
  for (let i = 0; i < numPicks; i++) {
    const pickListId = `workflow-test-${Date.now()}-${i}`;
    const pickData = generatePickConfirmationData(data.testData);
    
    const confirmResponse = http.post(
      `${BASE_URL}/api/picklists/${pickListId}/confirm-pick`,
      JSON.stringify(pickData),
      {
        headers: commonHeaders,
        tags: { endpoint: 'picklists', operation: 'confirm', workflow: 'complete' }
      }
    );
    
    const confirmSuccess = checkPickListResponse(confirmResponse, 'pick-confirmation');
    pickConfirmationRate.add(confirmSuccess);
    
    // Short delay between picks
    if (i < numPicks - 1) {
      sleep(sleepWithJitter(1.5, 30));
    }
  }
  
  sleep(sleepWithJitter(1, 20)); // Brief pause before next cycle
  
  // 4. Check for next task again
  const nextResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}/next`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'next-check', workflow: 'complete' }
  });
  
  const nextSuccess = checkPickListResponse(nextResponse, 'next-task-check');
  pickListRetrievalRate.add(nextSuccess);
}

function statusMonitoringScenario(data) {
  console.log('📊 Executing status monitoring scenario');
  
  // Monitor different pick list statuses
  const statuses = ['PENDING', 'IN_PROGRESS', 'COMPLETED'];
  
  // Query multiple statuses to simulate dashboard/monitoring
  for (const status of statuses) {
    const statusResponse = http.get(`${BASE_URL}/api/picklists/status/${status}`, {
      headers: commonHeaders,
      tags: { endpoint: 'picklists', operation: 'status-monitoring', status: status }
    });
    
    const success = checkPickListResponse(statusResponse, `status-${status.toLowerCase()}`);
    pickListRetrievalRate.add(success);
    
    sleep(sleepWithJitter(0.5, 25)); // Quick succession for monitoring
  }
  
  // Additional monitoring - check specific picker loads
  const monitoredPickers = data.testData.pickerIds.slice(0, 3); // Monitor first 3 pickers
  
  for (const pickerId of monitoredPickers) {
    const pickerResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
      headers: commonHeaders,
      tags: { endpoint: 'picklists', operation: 'picker-monitoring', picker: pickerId }
    });
    
    const success = checkPickListResponse(pickerResponse, `monitor-picker-${pickerId}`);
    pickListRetrievalRate.add(success);
    
    sleep(sleepWithJitter(0.3, 20));
  }
}

function pickConfirmationScenario(data) {
  console.log('✅ Executing pick confirmation scenario');
  
  // Simulate intensive pick confirmation period
  const numConfirmations = Math.floor(Math.random() * 4) + 2; // 2-5 confirmations
  
  for (let i = 0; i < numConfirmations; i++) {
    const pickListId = `confirmation-test-${Date.now()}-${i}`;
    const pickData = generatePickConfirmationData(data.testData);
    
    // Add some variability to pick confirmations
    if (Math.random() < 0.1) {
      // 10% chance of partial pick
      pickData.quantity = Math.max(1, pickData.quantity - 1);
    }
    
    const confirmResponse = http.post(
      `${BASE_URL}/api/picklists/${pickListId}/confirm-pick`,
      JSON.stringify(pickData),
      {
        headers: commonHeaders,
        tags: { endpoint: 'picklists', operation: 'bulk-confirm' }
      }
    );
    
    const success = checkPickListResponse(confirmResponse, 'bulk-pick-confirmation');
    pickConfirmationRate.add(success);
    
    // Variable delay between confirmations
    if (i < numConfirmations - 1) {
      sleep(sleepWithJitter(2, 40)); // 1.2-2.8 seconds between picks
    }
  }
}

function nextTaskPollingScenario(data) {
  console.log('🔄 Executing next task polling scenario');
  
  const pickerId = selectRandom(data.testData.pickerIds);
  
  // Simulate a picker frequently checking for new tasks
  const numPolls = Math.floor(Math.random() * 3) + 3; // 3-5 polls
  
  for (let i = 0; i < numPolls; i++) {
    const pollResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}/next`, {
      headers: commonHeaders,
      tags: { endpoint: 'picklists', operation: 'frequent-polling' }
    });
    
    const success = checkPickListResponse(pollResponse, 'frequent-poll');
    pickListRetrievalRate.add(success);
    
    // Check response for any available tasks
    if (pollResponse.status === 200 && pollResponse.body) {
      console.log(`📋 Picker ${pickerId} found available task`);
      
      // If task found, also get picker's current workload
      sleep(sleepWithJitter(0.5, 20));
      
      const workloadResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
        headers: commonHeaders,
        tags: { endpoint: 'picklists', operation: 'workload-check' }
      });
      
      const workloadSuccess = checkPickListResponse(workloadResponse, 'workload-check');
      pickListRetrievalRate.add(workloadSuccess);
    }
    
    if (i < numPolls - 1) {
      sleep(sleepWithJitter(4, 30)); // Polling interval
    }
  }
}

function bulkQueriesScenario(data) {
  console.log('📈 Executing bulk queries scenario');
  
  // Simulate management dashboard or reporting system
  const startTime = Date.now();
  
  // 1. Get all pending pick lists
  const pendingResponse = http.get(`${BASE_URL}/api/picklists/status/PENDING`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'bulk-pending' }
  });
  
  const pendingSuccess = checkPickListResponse(pendingResponse, 'bulk-pending');
  pickListRetrievalRate.add(pendingSuccess);
  
  sleep(sleepWithJitter(0.2, 15));
  
  // 2. Get all in-progress pick lists
  const inProgressResponse = http.get(`${BASE_URL}/api/picklists/status/IN_PROGRESS`, {
    headers: commonHeaders,
    tags: { endpoint: 'picklists', operation: 'bulk-in-progress' }
  });
  
  const inProgressSuccess = checkPickListResponse(inProgressResponse, 'bulk-in-progress');
  pickListRetrievalRate.add(inProgressSuccess);
  
  sleep(sleepWithJitter(0.2, 15));
  
  // 3. Query all pickers' current assignments
  for (const pickerId of data.testData.pickerIds) {
    const pickerResponse = http.get(`${BASE_URL}/api/picklists/picker/${pickerId}`, {
      headers: commonHeaders,
      tags: { endpoint: 'picklists', operation: 'bulk-picker-query' }
    });
    
    const success = checkPickListResponse(pickerResponse, `bulk-picker-${pickerId}`);
    pickListRetrievalRate.add(success);
    
    sleep(0.1); // Very short delay between picker queries
  }
  
  const bulkQueryTime = Date.now() - startTime;
  console.log(`📊 Bulk query completed in ${bulkQueryTime}ms`);
}

export function teardown(data) {
  console.log('📝 Pick List Load Test completed');
  console.log('');
  console.log('📊 Pick List Performance Results:');
  console.log(`   - Pick List Retrieval Success Rate: ${(pickListRetrievalRate.rate * 100).toFixed(2)}%`);
  console.log(`   - Pick Confirmation Success Rate: ${(pickConfirmationRate.rate * 100).toFixed(2)}%`);
  console.log(`   - Total Pick List Operations: ${operationsPerSecond.count}`);
  
  // Pick list specific health check
  console.log('');
  console.log('🏥 Pick List System Health Check:');
  
  const testResults = {
    statusQueries: false,
    pickerQueries: false,
    nextTaskQueries: false
  };
  
  // Test different types of pick list queries
  const testPickerId = data.testData.pickerIds[0];
  
  // 1. Status query test
  const statusResponse = http.get(`${BASE_URL}/api/picklists/status/PENDING`, { timeout: '5s' });
  if (statusResponse.status === 200 || statusResponse.status === 404) {
    testResults.statusQueries = true;
    console.log('✅ Status Queries: WORKING');
  } else {
    console.log(`❌ Status Queries: FAILED (${statusResponse.status})`);
  }
  
  // 2. Picker query test
  const pickerResponse = http.get(`${BASE_URL}/api/picklists/picker/${testPickerId}`, { timeout: '5s' });
  if (pickerResponse.status === 200 || pickerResponse.status === 404) {
    testResults.pickerQueries = true;
    console.log('✅ Picker Queries: WORKING');
  } else {
    console.log(`❌ Picker Queries: FAILED (${pickerResponse.status})`);
  }
  
  // 3. Next task query test
  const nextResponse = http.get(`${BASE_URL}/api/picklists/picker/${testPickerId}/next`, { timeout: '5s' });
  if ([200, 204, 404].includes(nextResponse.status)) {
    testResults.nextTaskQueries = true;
    console.log('✅ Next Task Queries: WORKING');
  } else {
    console.log(`❌ Next Task Queries: FAILED (${nextResponse.status})`);
  }
  
  const workingFeatures = Object.values(testResults).filter(result => result).length;
  
  console.log('');
  console.log('🎯 Pick List System Assessment:');
  if (workingFeatures === 3) {
    console.log('🟢 All pick list operations are functioning normally');
  } else if (workingFeatures >= 2) {
    console.log('🟡 Most pick list operations are working, some issues detected');
  } else {
    console.log('🔴 Pick list system may need attention');
  }
  
  console.log('');
  console.log('💡 Pick List Performance Tips:');
  console.log('   - Monitor pick confirmation success rate closely');
  console.log('   - Status queries should be the fastest operations');
  console.log('   - Consider caching for frequently accessed picker data');
  console.log('   - Next task polling should be optimized for real-time updates');
}