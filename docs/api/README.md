# API Reference

## 1. OVERVIEW

### Purpose and Primary Functionality
The Warehouse Operations REST API provides endpoints for managing warehouse operations including pick lists, packages, and mobile workflows. The API follows RESTful principles and supports JSON content negotiation.

### When to Use This API vs. Alternatives
Use this API when you need:
- **Real-time warehouse operations** via HTTP/REST
- **Mobile device integration** for warehouse workers
- **Synchronous operations** that require immediate responses
- **Standard web-based integrations**

Use alternatives for:
- **High-throughput async operations** (use Kafka events)
- **Batch processing** (use dedicated batch endpoints)
- **Real-time notifications** (use WebSocket connections)

### Architectural Context
The API serves as the primary integration point for:
- Mobile warehouse applications
- Web-based warehouse management systems
- External order management systems
- Third-party logistics providers

## 2. TECHNICAL SPECIFICATION

### Base Configuration
- **Base URL**: `/api/v1`
- **Content Type**: `application/json`
- **Authentication**: Bearer token (if configured)
- **API Documentation**: `/swagger-ui.html`
- **OpenAPI Spec**: `/api-docs`

### Global Response Format
All API responses follow a consistent structure:

```json
{
  "data": { /* response data */ },
  "timestamp": "2024-01-15T10:30:00Z",
  "status": "success|error",
  "message": "Human readable message"
}
```

### Error Response Format
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": [
      {
        "field": "quantity",
        "message": "Quantity must be positive"
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/picklists/123/confirm-pick"
}
```

### HTTP Status Codes
| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PATCH operations |
| 201 | Created | Successful POST operations |
| 204 | No Content | Successful DELETE or operations with no response body |
| 400 | Bad Request | Invalid request parameters or validation errors |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Business rule violation or conflict |
| 500 | Internal Server Error | Unexpected server errors |

## Pick Lists API

### Get Pick List by ID
Retrieves a specific pick list by its ID.

**Endpoint**: `GET /api/v1/picklists/{pickListId}`

**Parameters**:
- `pickListId` (path, required): The pick list identifier

**Response**: `PickListDto`
```json
{
  "id": "PL-123456",
  "orderId": "ORD-789",
  "status": "ASSIGNED",
  "pickerId": "PICKER-001",
  "instructions": [
    {
      "skuCode": "SKU-001",
      "quantity": 5,
      "binLocation": "A1-B2-C3",
      "completed": false
    }
  ],
  "createdAt": "2024-01-15T08:00:00Z",
  "assignedAt": "2024-01-15T08:15:00Z",
  "completedAt": null
}
```

**Example**:
```bash
curl -X GET "http://localhost:8080/api/v1/picklists/PL-123456" \
  -H "Accept: application/json"
```

### Get Pick Lists by Picker
Retrieves all pick lists assigned to a specific picker.

**Endpoint**: `GET /api/v1/picklists/pickers/{pickerId}`

**Parameters**:
- `pickerId` (path, required): The picker identifier

**Response**: `List<PickListDto>`

**Caching**: Response cached for 2 minutes

### Get Pick Lists by Status
Retrieves pick lists filtered by status.

**Endpoint**: `GET /api/v1/picklists/status/{status}`

**Parameters**:
- `status` (path, required): Pick list status (`PENDING`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`)

### Get Next Pick List for Picker
Retrieves the next available pick list for a picker based on priority and optimization.

**Endpoint**: `GET /api/v1/picklists/picker/{pickerId}/next`

**Response**: `PickListDto` or `204 No Content` if no pick lists available

### Confirm Item Pick
Confirms that an item has been picked from a specific location.

**Endpoint**: `POST /api/v1/picklists/{pickListId}/confirm-pick`

**Request Body**: `ConfirmItemPickRequest`
```json
{
  "skuCode": "SKU-001",
  "quantity": 5,
  "binLocation": "A1-B2-C3"
}
```

**Validation Rules**:
- `skuCode`: Required, non-blank string
- `quantity`: Required, positive integer
- `binLocation`: Required, non-blank string matching location format

**Example**:
```bash
curl -X POST "http://localhost:8080/api/v1/picklists/PL-123456/confirm-pick" \
  -H "Content-Type: application/json" \
  -d '{
    "skuCode": "SKU-001",
    "quantity": 5,
    "binLocation": "A1-B2-C3"
  }'
```

## Packages API

### Create Package
Creates a new package from a fulfillment order.

**Endpoint**: `POST /api/v1/packages`

**Request Body**: `CreatePackageRequest`
```json
{
  "orderType": "STANDARD",
  "street": "123 Main St",
  "city": "Springfield",
  "state": "IL",
  "postalCode": "62701",
  "country": "USA",
  "items": [
    {
      "skuCode": "SKU-001",
      "quantity": 2
    },
    {
      "skuCode": "SKU-002",
      "quantity": 1
    }
  ]
}
```

**Response**: `PackageDto`
```json
{
  "packageId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "packedItems": [
    {
      "skuCode": "SKU-001",
      "quantity": 2
    }
  ],
  "totalQuantity": 3,
  "createdAt": "2024-01-15T10:00:00Z"
}
```

### Confirm Package
Confirms a package is ready for shipping.

**Endpoint**: `PATCH /api/v1/packages/{packageId}/confirm`

**Parameters**:
- `packageId` (path, required): The package identifier

### Get Package by Order ID
Retrieves a package associated with a specific order.

**Endpoint**: `GET /api/v1/packages/order/{orderId}`

**Parameters**:
- `orderId` (path, required): The order identifier

## Mobile API

The mobile API endpoints are designed specifically for mobile device workflows and barcode scanning operations.

### Mobile Work Summary
**Endpoint**: `GET /api/v1/mobile/work/summary/{workerId}`

Provides a summary of assigned work for mobile workers.

### Mobile Barcode Scan
**Endpoint**: `POST /api/v1/mobile/scan`

Processes barcode scans from mobile devices.

**Request Body**: `MobileScanRequest`
```json
{
  "workerId": "WORKER-001",
  "barcode": "SKU001234567890",
  "scanType": "ITEM_SCAN",
  "location": "A1-B2-C3",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Mobile Step Completion
**Endpoint**: `POST /api/v1/mobile/steps/complete`

Marks a workflow step as completed from mobile device.

## 3. IMPLEMENTATION EXAMPLES

### Basic API Integration
```javascript
// Fetch pick list for picker
async function getPickListsForPicker(pickerId) {
  const response = await fetch(`/api/v1/picklists/pickers/${pickerId}`);

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return await response.json();
}

// Confirm item pick
async function confirmItemPick(pickListId, item) {
  const response = await fetch(`/api/v1/picklists/${pickListId}/confirm-pick`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      skuCode: item.skuCode,
      quantity: item.quantity,
      binLocation: item.binLocation
    })
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
}
```

### Mobile Integration Pattern
```typescript
interface MobileWorkflowService {
  async getNextWork(workerId: string): Promise<WorkItem> {
    const response = await this.apiClient.get(`/picklists/picker/${workerId}/next`);
    return response.data;
  }

  async scanBarcode(scanRequest: ScanRequest): Promise<ScanResult> {
    const response = await this.apiClient.post('/mobile/scan', scanRequest);
    return response.data;
  }

  async completeStep(stepId: string, completion: StepCompletion): Promise<void> {
    await this.apiClient.post('/mobile/steps/complete', {
      stepId,
      ...completion
    });
  }
}
```

### Error Handling Pattern
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
    ErrorResponse error = ErrorResponse.builder()
      .code("VALIDATION_ERROR")
      .message(e.getMessage())
      .details(e.getFieldErrors())
      .build();

    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
    ErrorResponse error = ErrorResponse.builder()
      .code("ENTITY_NOT_FOUND")
      .message(e.getMessage())
      .build();

    return ResponseEntity.notFound().build();
  }
}
```

### Caching Configuration
```java
@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(caffeineCacheBuilder());
    return cacheManager;
  }

  Caffeine<Object, Object> caffeineCacheBuilder() {
    return Caffeine.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(5, TimeUnit.MINUTES)
      .recordStats();
  }
}
```

## 4. TROUBLESHOOTING

### Common API Issues

#### 1. 400 Bad Request - Validation Errors
**Problem**: Request validation failures
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "quantity",
        "message": "Quantity must be positive"
      }
    ]
  }
}
```

**Solutions**:
- Validate request data on client side
- Check field constraints and formats
- Ensure required fields are present

#### 2. 404 Not Found - Entity Not Found
**Problem**: Requesting non-existent resources
```json
{
  "error": {
    "code": "ENTITY_NOT_FOUND",
    "message": "PickList with ID 'PL-999' not found"
  }
}
```

**Solutions**:
- Verify resource IDs are correct
- Check if resource was deleted or moved
- Implement proper error handling

#### 3. 409 Conflict - Business Rule Violations
**Problem**: Operations violating business rules
```json
{
  "error": {
    "code": "BUSINESS_RULE_VIOLATION",
    "message": "Cannot pick item - PickList already completed"
  }
}
```

**Solutions**:
- Check entity state before operations
- Implement optimistic locking
- Handle concurrent modifications

### Performance Issues

#### 1. Slow Response Times
**Debugging Steps**:
```bash
# Check application metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Check database performance
curl http://localhost:8080/actuator/metrics/mongodb.driver.pool.size

# Enable SQL logging (if applicable)
logging.level.org.springframework.data.mongodb=DEBUG
```

**Solutions**:
- Implement response caching
- Add database indexes
- Use pagination for large datasets
- Optimize query projections

#### 2. Memory Issues
**Monitoring**:
```bash
# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Heap dump analysis
jcmd <pid> GC.run_finalization
jcmd <pid> VM.gc
```

### API Testing Strategies

#### 1. Integration Testing
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PickListControllerIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void shouldCreateAndRetrievePickList() {
    // Create pick list
    ResponseEntity<PickListDto> createResponse = restTemplate.postForEntity(
      "/api/v1/picklists", createRequest, PickListDto.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Retrieve pick list
    ResponseEntity<PickListDto> getResponse = restTemplate.getForEntity(
      "/api/v1/picklists/" + createResponse.getBody().getId(), PickListDto.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
```

#### 2. Contract Testing
```java
@AutoConfigureJsonTesters
class PickListDtoSerializationTest {

  @Autowired
  private JacksonTester<PickListDto> json;

  @Test
  void shouldSerializePickListDto() throws Exception {
    PickListDto pickList = createTestPickList();

    assertThat(json.write(pickList))
      .hasJsonPathStringValue("$.id")
      .hasJsonPathStringValue("$.status")
      .hasJsonPathArrayValue("$.instructions");
  }
}
```

## 5. RELATED COMPONENTS

### Dependencies
- **Spring Web MVC**: REST endpoint framework
- **Spring Validation**: Request validation
- **Jackson**: JSON serialization/deserialization
- **SpringDoc OpenAPI**: API documentation generation
- **Spring Cache**: Response caching

### Components Commonly Used Alongside
- **API Gateway**: Route and secure external API access
- **Load Balancer**: Distribute API traffic
- **Rate Limiter**: Protect against API abuse
- **Monitoring**: Track API metrics and performance
- **Authentication Service**: Secure API endpoints

### Alternative Approaches
- **GraphQL**: For flexible data fetching
- **gRPC**: For high-performance service communication
- **WebSocket**: For real-time bidirectional communication
- **Server-Sent Events**: For server-to-client streaming
- **Messaging Queues**: For asynchronous operations

### Integration Patterns
- **API Versioning**: Maintain backward compatibility
- **Rate Limiting**: Prevent API abuse
- **Circuit Breaker**: Handle downstream failures
- **Request/Response Logging**: Audit and debugging
- **CORS Configuration**: Cross-origin resource sharing