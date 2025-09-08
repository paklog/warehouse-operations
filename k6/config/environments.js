// Environment configurations for different testing scenarios

export const environments = {
  local: {
    baseUrl: 'http://localhost:8080',
    description: 'Local development environment'
  },
  staging: {
    baseUrl: 'https://warehouse-operations-staging.paklog.com',
    description: 'Staging environment'
  },
  production: {
    baseUrl: 'https://warehouse-operations.paklog.com',
    description: 'Production environment'
  }
};

export const getEnvironment = () => {
  const env = __ENV.ENVIRONMENT || 'local';
  return environments[env] || environments.local;
};

// Common configuration
export const config = {
  // Test data
  testData: {
    pickerIds: ['picker-001', 'picker-002', 'picker-003', 'picker-004', 'picker-005'],
    skuCodes: ['SKU-001', 'SKU-002', 'SKU-003', 'SKU-004', 'SKU-005'],
    binLocations: ['A-01-01', 'A-01-02', 'B-02-01', 'B-02-02', 'C-03-01'],
    orderTypes: ['STANDARD', 'PRIORITY', 'EXPRESS'],
    cities: ['New York', 'Los Angeles', 'Chicago', 'Houston', 'Phoenix'],
    states: ['NY', 'CA', 'IL', 'TX', 'AZ'],
    countries: ['USA']
  },
  
  // Request timeouts and thresholds
  timeouts: {
    request: '30s',
    scenario: '5m'
  },
  
  // Performance thresholds
  thresholds: {
    // HTTP response time thresholds
    http_req_duration: ['p(95)<2000'], // 95% of requests should be below 2s
    http_req_duration_health: ['p(95)<500'], // Health endpoints should be faster
    
    // Success rate thresholds
    http_req_failed: ['rate<0.01'], // Error rate should be less than 1%
    
    // Specific endpoint thresholds
    'http_req_duration{endpoint:packages}': ['p(95)<3000'],
    'http_req_duration{endpoint:picklists}': ['p(95)<2000'],
    'http_req_duration{endpoint:health}': ['p(95)<500'],
    
    // Custom metrics
    package_creation_success: ['rate>0.99'],
    pick_confirmation_success: ['rate>0.99']
  }
};