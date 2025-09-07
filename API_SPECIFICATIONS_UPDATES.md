# API Specifications Updates Required

## Overview

Based on the analysis of the user stories in [`stories.md`](stories.md), the current [`openapi.yaml`](openapi.yaml) and [`asyncapi.yaml`](asyncapi.yaml) specifications need significant updates to reflect the complete implementation requirements.

## OpenAPI Specification Updates Required

### Current State Analysis
The current OpenAPI spec only covers:
- [`POST /waves/{wave_id}/release`](openapi.yaml:11) - Wave release endpoint (WO-08)
- [`POST /pick_lists/{pick_list_id}/items/confirm_pick`](openapi.yaml:33) - Pick confirmation (WO-11)

### Missing Endpoints Based on User Stories

#### 1. Wave Management APIs (WO-06, WO-07, WO-08)
```yaml
# Additional endpoints needed:
GET /waves/{wave_id}           # Get wave details
GET /waves                     # List waves with filtering
POST /waves                    # Create wave (for WaveStrategy)
PUT /waves/{wave_id}           # Update wave (add orders)
```

#### 2. PickList Management APIs (WO-09, WO-10)
```yaml
# Additional endpoints needed:
GET /pick_lists/{pick_list_id}           # Get pick list details
GET /pick_lists                          # List pick lists by picker
GET /pick_lists/{pick_list_id}/next      # Get next pick instruction
PUT /pick_lists/{pick_list_id}/assign    # Assign to picker
```

#### 3. Package Management APIs (WO-12)
```yaml
# New endpoints needed:
POST /packages                           # Create package
GET /packages/{package_id}               # Get package details
POST /packages/{package_id}/confirm      # Confirm packing complete
POST /packages/{package_id}/items        # Add items to package
```

#### 4. Workload Orchestrator APIs (WO-03, WO-04, WO-05)
```yaml
# Administrative endpoints:
GET /workload/strategy                    # Get current strategy
PUT /workload/strategy                    # Change strategy
GET /workload/status                      # Get orchestrator status
```

### Missing Schema Definitions

#### 1. Enhanced Wave Schema
```yaml
Wave:
  type: object
  required: [wave_id, status, strategy_type, created_at]
  properties:
    wave_id:
      type: string
      format: uuid
    status:
      type: string
      enum: [planned, released, closed]
    strategy_type:
      type: string
      enum: [continuous, wave]
      description: Strategy used to create this wave
    order_ids:
      type: array
      items:
        type: string
        format: uuid
      description: Orders included in this wave
    carrier_cutoff_time:
      type: string
      format: date-time
      description: Deadline for carrier pickup
    created_at:
      type: string
      format: date-time
    released_at:
      type: string
      format: date-time
      nullable: true
    closed_at:
      type: string
      format: date-time
      nullable: true
    pick_lists:
      type: array
      items:
        $ref: '#/components/schemas/PickListSummary'
```

#### 2. Enhanced PickList Schema
```yaml
PickList:
  type: object
  required: [pick_list_id, status, wave_id, instructions]
  properties:
    pick_list_id:
      type: string
      format: uuid
    status:
      type: string
      enum: [pending, assigned, in_progress, completed]
    wave_id:
      type: string
      format: uuid
      description: Wave this pick list belongs to
    picker_id:
      type: string
      nullable: true
      description: ID of assigned picker
    assigned_at:
      type: string
      format: date-time
      nullable: true
    started_at:
      type: string
      format: date-time
      nullable: true
    completed_at:
      type: string
      format: date-time
      nullable: true
    instructions:
      type: array
      items:
        $ref: '#/components/schemas/PickInstruction'
```

#### 3. Enhanced PickInstruction Schema
```yaml
PickInstruction:
  type: object
  required: [sequence, sku, quantity_to_pick, bin_location, status]
  properties:
    sequence:
      type: integer
      description: Optimized sequence for picker route
    sku:
      type: string
      description: Product SKU to pick
    quantity_to_pick:
      type: integer
      minimum: 1
    quantity_picked:
      type: integer
      minimum: 0
      description: Actual quantity picked (for partial picks)
    bin_location:
      type: string
      description: Physical location in warehouse
    status:
      type: string
      enum: [pending, picked, skipped]
      default: pending
    picked_at:
      type: string
      format: date-time
      nullable: true
```

#### 4. Package Schema (New)
```yaml
Package:
  type: object
  required: [package_id, order_id, status, packed_items]
  properties:
    package_id:
      type: string
      format: uuid
    order_id:
      type: string
      format: uuid
      description: Original order this package fulfills
    status:
      type: string
      enum: [in_progress, completed, shipped]
    packed_items:
      type: array
      items:
        $ref: '#/components/schemas/PackedItem'
    weight:
      $ref: '#/components/schemas/Weight'
    dimensions:
      $ref: '#/components/schemas/Dimensions'
    packed_at:
      type: string
      format: date-time
      nullable: true
    packer_id:
      type: string
      description: ID of person who packed the order

PackedItem:
  type: object
  required: [sku, quantity_packed]
  properties:
    sku:
      type: string
    quantity_packed:
      type: integer
      minimum: 1
    lot_number:
      type: string
      nullable: true
    expiry_date:
      type: string
      format: date
      nullable: true
```

### Error Response Schemas (Missing)

```yaml
ErrorResponse:
  type: object
  required: [error_code, message, timestamp]
  properties:
    error_code:
      type: string
      enum: [VALIDATION_ERROR, BUSINESS_RULE_VIOLATION, RESOURCE_NOT_FOUND, INTERNAL_ERROR]
    message:
      type: string
      description: Human-readable error message
    details:
      type: array
      items:
        type: string
      description: Specific validation errors or details
    timestamp:
      type: string
      format: date-time
    trace_id:
      type: string
      description: Request correlation ID for debugging

ValidationError:
  allOf:
    - $ref: '#/components/schemas/ErrorResponse'
    - type: object
      properties:
        field_errors:
          type: object
          additionalProperties:
            type: array
            items:
              type: string
```

## AsyncAPI Specification Updates Required

### Current State Analysis
The current AsyncAPI spec covers:
- [`FulfillmentOrderValidated`](asyncapi.yaml:33) consumption (WO-13)
- [`PackagePacked`](asyncapi.yaml:41) publishing (WO-14)

### Missing Domain Events Based on User Stories

#### 1. Internal Domain Events (Should be documented)

```yaml
# New message definitions needed:

WaveCreated:
  name: wave_created
  title: Wave Created
  summary: Published when a new wave is created by the WaveStrategy
  contentType: application/cloudevents+json
  payload:
    $ref: "#/components/schemas/WaveCreatedCloudEvent"

WaveReleased:
  name: wave_released
  title: Wave Released  
  summary: Published when a wave is released for picking (WO-08)
  contentType: application/cloudevents+json
  payload:
    $ref: "#/components/schemas/WaveReleasedCloudEvent"

PickListCreated:
  name: pick_list_created
  title: Pick List Created
  summary: Published when a pick list is generated for a picker
  contentType: application/cloudevents+json
  payload:
    $ref: "#/components/schemas/PickListCreatedCloudEvent"

ItemPicked:
  name: item_picked
  title: Item Picked
  summary: Published when a picker confirms an item pick (WO-11)
  contentType: application/cloudevents+json
  payload:
    $ref: "#/components/schemas/ItemPickedCloudEvent"

PickListCompleted:
  name: pick_list_completed
  title: Pick List Completed
  summary: Published when all items in a pick list are picked
  contentType: application/cloudevents+json
  payload:
    $ref: "#/components/schemas/PickListCompletedCloudEvent"
```

#### 2. New Channel Definitions

```yaml
# Internal domain events channel
fulfillment.warehouse.v1.domain_events:
  description: Internal domain events for warehouse operations coordination
  publish:
    summary: Publish internal domain events for service coordination
    message:
      oneOf:
        - $ref: '#/components/messages/WaveCreated'
        - $ref: '#/components/messages/WaveReleased'
        - $ref: '#/components/messages/PickListCreated'
        - $ref: '#/components/messages/ItemPicked'
        - $ref: '#/components/messages/PickListCompleted'
```

#### 3. Enhanced Schema Definitions

```yaml
# Enhanced data schemas needed:

WaveCreatedData:
  type: object
  required: [wave]
  properties:
    wave:
      $ref: '#/components/schemas/Wave'

WaveReleasedData:
  type: object
  required: [wave_id, order_ids, pick_lists_created]
  properties:
    wave_id:
      type: string
      format: uuid
    order_ids:
      type: array
      items:
        type: string
        format: uuid
    pick_lists_created:
      type: array
      items:
        type: string
        format: uuid

ItemPickedData:
  type: object
  required: [pick_list_id, sku, quantity_picked, bin_location, picker_id]
  properties:
    pick_list_id:
      type: string
      format: uuid
    sku:
      type: string
    quantity_picked:
      type: integer
    bin_location:
      type: string
    picker_id:
      type: string
    picked_at:
      type: string
      format: date-time

Wave:
  type: object
  required: [wave_id, status, strategy_type, order_ids]
  properties:
    wave_id:
      type: string
      format: uuid
    status:
      type: string
      enum: [planned, released, closed]
    strategy_type:
      type: string
      enum: [continuous, wave]
    order_ids:
      type: array
      items:
        type: string
        format: uuid
    carrier_cutoff_time:
      type: string
      format: date-time
    created_at:
      type: string
      format: date-time
```

### CloudEvents Compliance Verification

#### Required CloudEvents Headers
All events must include:
- `specversion: "1.0"`
- `type`: Consistent naming pattern
- `source`: Service identification 
- `subject`: Domain entity identifier
- `id`: Unique event identifier
- `time`: Event occurrence timestamp
- `datacontenttype: "application/json"`

#### Event Type Naming Convention
```
com.paklog.fulfillment.warehouse.{aggregate}.{action}

Examples:
- com.paklog.fulfillment.warehouse.wave.created
- com.paklog.fulfillment.warehouse.wave.released  
- com.paklog.fulfillment.warehouse.picklist.created
- com.paklog.fulfillment.warehouse.item.picked
- com.paklog.fulfillment.warehouse.package.packed
```

## Implementation Priority

### Phase 1: Core API Extensions
1. Update [`openapi.yaml`](openapi.yaml) with missing wave and picklist endpoints
2. Add comprehensive schema definitions
3. Include proper error response schemas

### Phase 2: Domain Events Documentation  
1. Update [`asyncapi.yaml`](asyncapi.yaml) with internal domain events
2. Add new channel definitions
3. Ensure CloudEvents compliance

### Phase 3: Validation and Testing
1. Validate specifications against implementation
2. Add usage examples and documentation
3. Ensure consistency between OpenAPI and AsyncAPI

## Key Architectural Considerations

### 1. API Versioning Strategy
- Use URL path versioning: `/v1/`
- Include version in event types
- Maintain backward compatibility

### 2. Error Handling Standards
- Consistent error response format
- Proper HTTP status codes
- Detailed validation messages

### 3. Event Schema Evolution
- Support schema versioning in CloudEvents
- Maintain event compatibility
- Document breaking changes

### 4. Security Considerations
- Authentication/authorization requirements
- Rate limiting specifications
- Input validation rules

## Testing Strategy for API Compliance

### 1. OpenAPI Contract Testing
- Validate request/response schemas
- Test all endpoint combinations
- Verify error response formats

### 2. AsyncAPI Event Testing
- Validate CloudEvents format compliance
- Test event schema compatibility
- Verify message routing

### 3. Integration Testing
- End-to-end workflow validation
- Cross-service event flow testing
- Performance and load testing

This comprehensive update plan ensures the API specifications accurately reflect the complete implementation requirements from all user stories while maintaining proper architectural patterns and standards.