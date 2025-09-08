// Utility functions for K6 tests
import { check } from 'k6';

// Generate random test data
export function generateRandomOrderId() {
  return `ord-${Date.now()}-${Math.random().toString(36).substring(7)}`;
}

export function generateRandomPickListId() {
  return `pl-${Date.now()}-${Math.random().toString(36).substring(7)}`;
}

export function selectRandom(array) {
  return array[Math.floor(Math.random() * array.length)];
}

export function generateRandomQuantity(min = 1, max = 10) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

// Generate realistic test package data
export function generatePackageData(testData) {
  const itemCount = Math.floor(Math.random() * 3) + 1; // 1-3 items
  const items = [];
  
  for (let i = 0; i < itemCount; i++) {
    items.push({
      skuCode: selectRandom(testData.skuCodes),
      quantity: generateRandomQuantity(1, 5)
    });
  }
  
  return {
    orderType: selectRandom(testData.orderTypes),
    street: `${Math.floor(Math.random() * 9999) + 1} Test Street`,
    city: selectRandom(testData.cities),
    state: selectRandom(testData.states),
    postalCode: String(Math.floor(Math.random() * 90000) + 10000),
    country: selectRandom(testData.countries),
    items: items
  };
}

// Generate item pick confirmation data
export function generatePickConfirmationData(testData) {
  return {
    skuCode: selectRandom(testData.skuCodes),
    quantity: generateRandomQuantity(1, 3),
    binLocation: selectRandom(testData.binLocations)
  };
}

// Common HTTP headers
export const commonHeaders = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
  'User-Agent': 'K6-Load-Test/1.0'
};

// Standard checks for API responses
export function checkApiResponse(response, endpoint, expectedStatus = 200) {
  const checks = {};
  checks[`${endpoint} - status is ${expectedStatus}`] = (r) => r.status === expectedStatus;
  checks[`${endpoint} - response time < 5s`] = (r) => r.timings.duration < 5000;
  
  if (expectedStatus === 200 && response.headers['Content-Type']) {
    checks[`${endpoint} - content type is JSON`] = (r) => 
      r.headers['Content-Type'].includes('application/json');
  }
  
  return check(response, checks);
}

// Check health endpoint response
export function checkHealthResponse(response) {
  return check(response, {
    'health - status is 200': (r) => r.status === 200,
    'health - response time < 1s': (r) => r.timings.duration < 1000,
    'health - status is UP': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.status === 'UP';
      } catch (e) {
        return false;
      }
    }
  });
}

// Check package creation response
export function checkPackageCreationResponse(response) {
  const basicCheck = checkApiResponse(response, 'package-creation');
  
  if (response.status === 200) {
    const additionalChecks = check(response, {
      'package-creation - has packageId': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.packageId && body.packageId.length > 0;
        } catch (e) {
          return false;
        }
      },
      'package-creation - has status': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.status && ['PENDING', 'CONFIRMED'].includes(body.status);
        } catch (e) {
          return false;
        }
      }
    });
    return basicCheck && additionalChecks;
  }
  
  return basicCheck;
}

// Check pick list response
export function checkPickListResponse(response, endpoint = 'picklist') {
  const basicCheck = checkApiResponse(response, endpoint);
  
  if (response.status === 200) {
    const additionalChecks = check(response, {
      [`${endpoint} - response is valid`]: (r) => {
        try {
          const body = JSON.parse(r.body);
          return body !== null;
        } catch (e) {
          return false;
        }
      }
    });
    return basicCheck && additionalChecks;
  }
  
  return basicCheck;
}

// Log test results
export function logTestMetrics(testName, startTime) {
  const duration = Date.now() - startTime;
  console.log(`✅ ${testName} completed in ${duration}ms`);
}

// Sleep with random jitter to simulate realistic user behavior
export function sleepWithJitter(baseTime, jitterPercent = 20) {
  const jitter = baseTime * (jitterPercent / 100);
  const randomJitter = (Math.random() - 0.5) * 2 * jitter;
  const sleepTime = baseTime + randomJitter;
  return Math.max(sleepTime, 0.1); // Minimum 100ms
}

// Weighted random selection for more realistic load patterns
export function weightedRandom(options) {
  const weights = options.map(opt => opt.weight || 1);
  const totalWeight = weights.reduce((sum, weight) => sum + weight, 0);
  let random = Math.random() * totalWeight;
  
  for (let i = 0; i < options.length; i++) {
    random -= weights[i];
    if (random <= 0) {
      return options[i];
    }
  }
  
  return options[options.length - 1];
}