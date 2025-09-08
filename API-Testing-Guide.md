# Warehouse Operations API Testing Guide

This guide provides instructions for testing the Paklog Warehouse Operations APIs using the provided Postman collection.

## Files Included

- `Warehouse-Operations-API.postman_collection.json` - Main API collection
- `Warehouse-Operations-Local.postman_environment.json` - Local development environment
- `Warehouse-Operations-Staging.postman_environment.json` - Staging environment template

## Setup Instructions

### 1. Import Postman Collection and Environments

1. Open Postman
2. Click "Import" button
3. Import the following files:
   - `Warehouse-Operations-API.postman_collection.json`
   - `Warehouse-Operations-Local.postman_environment.json`
   - `Warehouse-Operations-Staging.postman_environment.json`

### 2. Configure Environment

1. Select the appropriate environment (Local or Staging)
2. Update environment variables as needed:
   - `baseUrl`: Service endpoint URL
   - `mongoUri`: MongoDB connection string
   - `kafkaBootstrapServers`: Kafka bootstrap servers
   - Test data variables (picker IDs, SKU codes, etc.)

### 3. Start the Application

Make sure the Warehouse Operations service is running locally:

```bash
# Start MongoDB (if running locally)
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Start Kafka (if running locally)
docker run -d -p 9092:9092 --name kafka confluentinc/cp-kafka:latest

# Run the Spring Boot application
mvn spring-boot:run
```

## API Endpoints Overview

### Pick List Operations

| Endpoint | Method | Description |
|----------|---------|-------------|
| `/api/picklists/{pickListId}` | GET | Get pick list by ID |
| `/api/picklists/picker/{pickerId}` | GET | Get pick lists for a picker |
| `/api/picklists/status/{status}` | GET | Get pick lists by status |
| `/api/picklists/picker/{pickerId}/next` | GET | Get next pick list for picker |
| `/api/picklists/{pickListId}/confirm-pick` | POST | Confirm item pick |

#### Pick List Status Values
- `PENDING` - Newly created, not yet assigned
- `IN_PROGRESS` - Currently being picked
- `COMPLETED` - All items picked
- `CANCELLED` - Pick list cancelled

### Package Operations

| Endpoint | Method | Description |
|----------|---------|-------------|
| `/api/packages` | POST | Create new package |
| `/api/packages/order/{orderId}` | GET | Get package by order ID |
| `/api/packages/{packageId}/confirm` | PATCH | Confirm package |

#### Package Status Values
- `PENDING` - Package created, not confirmed
- `CONFIRMED` - Package confirmed and ready for shipment

### Health & Monitoring

| Endpoint | Method | Description |
|----------|---------|-------------|
| `/actuator/health` | GET | Service health check |
| `/actuator/info` | GET | Application information |
| `/actuator/metrics` | GET | Application metrics |

## Testing Workflow

### 1. Basic Health Check

Start by testing the health endpoint to ensure the service is running:

```
GET {{baseUrl}}/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP"
    },
    "kafka": {
      "status": "UP"
    }
  }
}
```

### 2. Package Creation Flow

1. **Create Package**: Create a new package with order items
   ```json
   POST {{baseUrl}}/api/packages
   {
     "orderType": "STANDARD",
     "street": "123 Main Street",
     "city": "Springfield",
     "state": "IL",
     "postalCode": "62701",
     "country": "USA",
     "items": [
       {
         "skuCode": "SKU-001",
         "quantity": 2
       }
     ]
   }
   ```

2. **Get Package**: Retrieve the created package
   ```
   GET {{baseUrl}}/api/packages/order/{orderId}
   ```

3. **Confirm Package**: Confirm the package for shipment
   ```
   PATCH {{baseUrl}}/api/packages/{packageId}/confirm
   ```

### 3. Pick List Operations Flow

1. **Get Pick Lists by Status**: Find available pick lists
   ```
   GET {{baseUrl}}/api/picklists/status/PENDING
   ```

2. **Get Next Pick List**: Get next available pick list for a picker
   ```
   GET {{baseUrl}}/api/picklists/picker/picker-001/next
   ```

3. **Confirm Item Pick**: Confirm picking of an item
   ```json
   POST {{baseUrl}}/api/picklists/{pickListId}/confirm-pick
   {
     "skuCode": "SKU-001",
     "quantity": 2,
     "binLocation": "A-01-05"
   }
   ```

## Sample Test Data

### Sample Package Creation Request
```json
{
  "orderType": "STANDARD",
  "street": "123 Warehouse Ave",
  "city": "Logistics City",
  "state": "CA",
  "postalCode": "90210",
  "country": "USA",
  "items": [
    {
      "skuCode": "WIDGET-001",
      "quantity": 5
    },
    {
      "skuCode": "GADGET-002",
      "quantity": 3
    }
  ]
}
```

### Sample Item Pick Confirmation Request
```json
{
  "skuCode": "WIDGET-001",
  "quantity": 5,
  "binLocation": "B-02-10"
}
```

## Error Handling

The API returns standard HTTP status codes:

- `200 OK` - Success
- `204 No Content` - Success with no response body
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

Example error response:
```json
{
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "SKU code is required",
  "path": "/api/picklists/123/confirm-pick"
}
```

## Collection Variables

The collection uses the following variables that you can customize:

- `baseUrl`: Base URL of the service
- `pickListId`: Pick list ID for testing
- `packageId`: Package ID for testing
- `orderId`: Order ID for testing
- `pickerId`: Picker ID for testing

## Test Automation

The collection includes automated tests that verify:

- Response times are reasonable (< 5 seconds)
- Response content types are correct
- Basic response structure validation

You can run the entire collection using Postman's Collection Runner or Newman for CI/CD integration:

```bash
# Install Newman
npm install -g newman

# Run collection
newman run Warehouse-Operations-API.postman_collection.json \
  -e Warehouse-Operations-Local.postman_environment.json
```

## Troubleshooting

### Common Issues

1. **Service not responding**: Check if the application is running on the correct port
2. **Database connection errors**: Verify MongoDB is running and accessible
3. **Kafka connection errors**: Ensure Kafka is running and properly configured
4. **404 errors**: Check if the requested resources exist in the database

### Debugging Tips

1. Check application logs for detailed error messages
2. Verify environment variables are correctly set
3. Use the health endpoints to check service dependencies
4. Test with simple requests first before complex workflows

## Additional Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Postman Testing Documentation](https://learning.postman.com/docs/writing-scripts/test-scripts/)
- [Newman CLI Documentation](https://learning.postman.com/docs/running-collections/using-newman-cli/)