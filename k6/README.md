# K6 Load Testing for Warehouse Operations

Comprehensive load testing suite for the Paklog Warehouse Operations microservice using K6.

## 🚀 Quick Start

### 1. Setup Environment

```bash
# Navigate to k6 directory
cd k6

# Run the installation script
./install.sh

# Add k6 to PATH for current session
source setup-path.sh
```

### 2. Run Tests

```bash
# Start with smoke test (basic functionality)
k6 run scripts/smoke-test.js

# Run load test (normal expected load)
k6 run scripts/load-test.js

# Run stress test (test breaking points)
k6 run scripts/stress-test.js
```

## 📁 Directory Structure

```
k6/
├── scripts/                    # Test scripts
│   ├── smoke-test.js          # Basic functionality verification
│   ├── load-test.js           # Normal load simulation
│   ├── stress-test.js         # High load/breaking point test
│   ├── spike-test.js          # Sudden load spike test
│   ├── picklist-load-test.js  # Pick list focused test
│   ├── package-load-test.js   # Package operations focused test
│   └── utils.js               # Common utilities
├── config/                    # Configuration files
│   └── environments.js       # Environment settings
├── data/                      # Test data files
├── reports/                   # Generated reports
├── bin/                       # K6 binary (created by install.sh)
├── package.json              # Node.js dependencies
├── .nvmrc                    # Node.js version
├── install.sh               # Setup script
└── README.md               # This file
```

## 🧪 Test Types

### 1. Smoke Test (`smoke-test.js`)
- **Purpose**: Basic functionality verification
- **Load**: 1 user for 2 minutes
- **Goal**: Ensure all endpoints respond correctly
- **Run**: `k6 run scripts/smoke-test.js`

### 2. Load Test (`load-test.js`)
- **Purpose**: Normal expected load simulation
- **Load**: 10-20 users over 16 minutes
- **Goal**: Test system under typical usage
- **Run**: `k6 run scripts/load-test.js`

### 3. Stress Test (`stress-test.js`)
- **Purpose**: Find system breaking points
- **Load**: Up to 200 users over 25 minutes
- **Goal**: Test system limits and recovery
- **Run**: `k6 run scripts/stress-test.js`

### 4. Spike Test (`spike-test.js`)
- **Purpose**: Test sudden load increases
- **Load**: 5→100→5→150→5 users with rapid transitions
- **Goal**: Test system resilience to traffic spikes
- **Run**: `k6 run scripts/spike-test.js`

### 5. Pick List Load Test (`picklist-load-test.js`)
- **Purpose**: Focus on pick list operations
- **Load**: Up to 25 users over 14 minutes
- **Goal**: Test pick list workflows and picker operations
- **Run**: `k6 run scripts/picklist-load-test.js`

### 6. Package Load Test (`package-load-test.js`)
- **Purpose**: Focus on package operations
- **Load**: Up to 35 users over 17 minutes
- **Goal**: Test package creation, retrieval, and confirmation
- **Run**: `k6 run scripts/package-load-test.js`

## 🌍 Environment Configuration

### Available Environments

- **local**: `http://localhost:8080` (default)
- **staging**: `https://warehouse-operations-staging.paklog.com`
- **production**: `https://warehouse-operations.paklog.com`

### Using Different Environments

```bash
# Local (default)
k6 run scripts/load-test.js

# Staging
ENVIRONMENT=staging k6 run scripts/load-test.js

# Production (use with caution!)
ENVIRONMENT=production k6 run scripts/smoke-test.js
```

### Custom Configuration

Edit `config/environments.js` to modify:
- Base URLs
- Test data (SKU codes, picker IDs, etc.)
- Performance thresholds
- Timeout values

## 📊 Performance Thresholds

### Default Thresholds

- **Response Time**: 95% of requests < 2 seconds
- **Error Rate**: < 1% failures
- **Health Endpoint**: 95% < 500ms
- **Package Creation**: > 99% success rate
- **Pick Confirmation**: > 99% success rate

### Custom Thresholds

```bash
# Run with custom virtual users and duration
k6 run --vus 50 --duration 5m scripts/load-test.js

# Override specific thresholds
k6 run --threshold http_req_duration=p(95)<5000 scripts/stress-test.js
```

## 🎯 Test Scenarios

### Package Workflow
1. Create package with realistic order data
2. Retrieve package by order ID
3. Confirm package for shipment
4. Validate response times and success rates

### Pick List Workflow
1. Get next available pick list for picker
2. Query pick lists by status
3. Confirm item picks with bin locations
4. Monitor picker workload distribution

### Mixed Operations
- Health monitoring
- Concurrent package/pick list operations
- Database stress testing
- API endpoint validation

## 📈 Metrics and Monitoring

### Built-in Metrics
- `http_req_duration`: Request response times
- `http_req_failed`: Request failure rate
- `vus`: Current virtual users
- `iterations`: Completed test iterations

### Custom Metrics
- `package_creation_success`: Package creation success rate
- `pick_confirmation_success`: Pick confirmation success rate
- `api_response_time`: API-specific response times
- `concurrent_users`: Peak concurrent users

### Viewing Results

```bash
# Run with detailed output
k6 run --verbose scripts/load-test.js

# Generate JSON report
k6 run --out json=reports/results.json scripts/load-test.js

# Real-time monitoring
k6 run --out influxdb=http://localhost:8086/k6 scripts/load-test.js
```

## 🔧 Prerequisites

Before running tests, ensure:

### Service Dependencies
- ✅ Warehouse Operations service is running
- ✅ MongoDB is accessible and healthy
- ✅ Kafka is running (for event publishing)

### System Requirements
- Node.js 16+ (managed by nvm)
- Sufficient system resources for load generation
- Network connectivity to target environment

### Quick Service Check

```bash
# Check service health
curl http://localhost:8080/actuator/health

# Verify MongoDB
curl http://localhost:8080/actuator/health | jq '.components.mongo'

# Test basic endpoint
curl -X POST http://localhost:8080/api/packages \
  -H "Content-Type: application/json" \
  -d '{"orderType":"TEST","items":[{"skuCode":"TEST-001","quantity":1}]}'
```

## 🚨 Best Practices

### Load Testing Guidelines

1. **Start Small**: Always begin with smoke tests
2. **Gradual Increase**: Incrementally increase load
3. **Monitor Resources**: Watch CPU, memory, and database metrics
4. **Test Isolation**: Run one test type at a time
5. **Environment Parity**: Use production-like data and configurations

### Production Testing

⚠️ **CAUTION**: Never run stress tests against production without approval

```bash
# Safe production verification
ENVIRONMENT=production k6 run scripts/smoke-test.js --vus 1 --duration 30s

# Staging stress testing
ENVIRONMENT=staging k6 run scripts/stress-test.js
```

### Result Interpretation

- **Green Zone**: All thresholds pass ✅
- **Yellow Zone**: Some thresholds fail but system functional ⚠️
- **Red Zone**: High error rates or timeouts ❌

## 🔍 Troubleshooting

### Common Issues

1. **Connection Refused**: Service not running or wrong URL
2. **High Error Rates**: System overloaded or configuration issues
3. **Slow Response Times**: Database bottlenecks or resource constraints
4. **K6 Not Found**: Run `source setup-path.sh` or check installation

### Debug Commands

```bash
# Verbose output
k6 run --verbose scripts/smoke-test.js

# Single user debug
k6 run --vus 1 --duration 30s scripts/load-test.js

# Check K6 version
./bin/k6 version

# Validate script syntax
./bin/k6 run --dry-run scripts/load-test.js
```

### Log Analysis

- Check application logs during tests
- Monitor database performance metrics
- Watch system resource usage (htop, iostat)
- Review K6 output for patterns

## 🎓 Advanced Usage

### Custom Test Scripts

Create new test scripts in the `scripts/` directory following this pattern:

```javascript
import { check, sleep } from 'k6';
import http from 'k6/http';
import { getEnvironment } from '../config/environments.js';

export const options = {
  stages: [
    { duration: '1m', target: 10 },
  ],
};

const environment = getEnvironment();

export default function () {
  const response = http.get(`${environment.baseUrl}/api/endpoint`);
  check(response, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(1);
}
```

### Integration with CI/CD

```yaml
# Example GitHub Actions
- name: Run Load Tests
  run: |
    cd k6
    ./install.sh
    source setup-path.sh
    k6 run --quiet scripts/smoke-test.js
```

### Custom Metrics

```javascript
import { Trend, Rate, Counter } from 'k6/metrics';

const customMetric = new Trend('custom_response_time');
const errorRate = new Rate('custom_errors');
const operations = new Counter('custom_operations');
```

## 📚 Resources

- [K6 Documentation](https://k6.io/docs/)
- [K6 Cloud](https://k6.io/cloud/) - For advanced monitoring
- [Performance Testing Best Practices](https://k6.io/docs/testing-guides/load-testing-best-practices/)
- [K6 Examples](https://github.com/grafana/k6-example-data-generation)

## 🤝 Contributing

To add new test scenarios:

1. Create script in `scripts/` directory
2. Follow existing naming conventions
3. Include proper error handling and metrics
4. Add documentation and examples
5. Test thoroughly before committing

## 📄 License

This K6 testing suite is part of the Paklog Warehouse Operations project.