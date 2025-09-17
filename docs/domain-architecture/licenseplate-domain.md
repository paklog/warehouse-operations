# LicensePlate Domain - Domain Model

## Overview
The LicensePlate domain manages containerization and tracking of inventory units through their lifecycle in warehouse operations. It provides hierarchical nesting capabilities, inventory tracking, and status management for physical containers (pallets, totes, boxes) and logical groupings of items.

## Domain Model Diagram

```mermaid
classDiagram
    %% License Plate Aggregate Root
    class LicensePlate {
        -LicensePlateId licensePlateId
        -LicensePlateStatus status
        -LicensePlateType type
        -BinLocation currentLocation
        -String parentLicensePlateId
        -Map~SkuCode,Quantity~ inventory
        -Set~String~ childLicensePlates
        -String receivingReference
        -String shipmentReference
        -Instant createdAt
        -Instant receivedAt
        -Instant shippedAt
        -String createdBy
        -String lastMovedBy
        -Instant lastMovedAt
        -Map~String,Object~ attributes
        -int version
        +receive(location BinLocation, receivedBy String, receivingReference String) void
        +moveTo(newLocation BinLocation, movedBy String) void
        +addInventory(item SkuCode, quantity Quantity) void
        +removeInventory(item SkuCode, quantity Quantity) void
        +addChildLicensePlate(childLicensePlateId String) void
        +removeChildLicensePlate(childLicensePlateId String) void
        +setParentLicensePlate(parentLicensePlateId String) void
        +ship(shipmentReference String, shippedBy String) void
        +pick(pickedBy String) void
        +stage(stagingLocation BinLocation, stagedBy String) void
        +makeAvailable() void
        +cancel(reason String, cancelledBy String) void
        +isEmpty() boolean
        +hasInventory(item SkuCode) boolean
        +getInventoryQuantity(item SkuCode) Quantity
        +getTotalQuantity() int
        +hasChildren() boolean
        +hasParent() boolean
        +isPickable() boolean
        +isMovable() boolean
        +canReceiveInventory() boolean
    }

    %% Value Objects
    class LicensePlateId {
        -UUID value
        +generate() LicensePlateId
        +of(id String) LicensePlateId
        +getValue() UUID
        +toString() String
    }

    %% Enums
    class LicensePlateStatus {
        <<enumeration>>
        CREATED
        IN_TRANSIT
        RECEIVED
        AVAILABLE
        PICKED
        STAGED
        SHIPPED
        CANCELLED
    }

    class LicensePlateType {
        <<enumeration>>
        PALLET
        TOTE
        CARTON
        CASE
        CONTAINER
        VIRTUAL
        MIXED
    }

    %% Domain Events
    class LicensePlateCreatedEvent {
        -LicensePlateId licensePlateId
        -LicensePlateType type
        -String createdBy
        -Instant createdAt
        +LicensePlateCreatedEvent(LicensePlateId, LicensePlateType, String, Instant)
    }

    class LicensePlateReceivedEvent {
        -LicensePlateId licensePlateId
        -BinLocation location
        -String receivedBy
        -Instant receivedAt
        -String receivingReference
        +LicensePlateReceivedEvent(LicensePlateId, BinLocation, String, Instant, String)
    }

    class LicensePlateMovedEvent {
        -LicensePlateId licensePlateId
        -BinLocation fromLocation
        -BinLocation toLocation
        -String movedBy
        -Instant movedAt
        +LicensePlateMovedEvent(LicensePlateId, BinLocation, BinLocation, String, Instant)
    }

    class LicensePlateInventoryAddedEvent {
        -LicensePlateId licensePlateId
        -SkuCode item
        -Quantity addedQuantity
        -Quantity totalQuantity
        +LicensePlateInventoryAddedEvent(LicensePlateId, SkuCode, Quantity, Quantity)
    }

    class LicensePlateInventoryRemovedEvent {
        -LicensePlateId licensePlateId
        -SkuCode item
        -Quantity removedQuantity
        -Quantity remainingQuantity
        +LicensePlateInventoryRemovedEvent(LicensePlateId, SkuCode, Quantity, Quantity)
    }

    class LicensePlateNestingChangedEvent {
        -LicensePlateId parentLicensePlateId
        -String childLicensePlateId
        -boolean added
        +LicensePlateNestingChangedEvent(LicensePlateId, String, boolean)
    }

    class LicensePlateShippedEvent {
        -LicensePlateId licensePlateId
        -String shipmentReference
        -String shippedBy
        -Instant shippedAt
        +LicensePlateShippedEvent(LicensePlateId, String, String, Instant)
    }

    class LicensePlatePickedEvent {
        -LicensePlateId licensePlateId
        -String pickedBy
        -Instant pickedAt
        +LicensePlatePickedEvent(LicensePlateId, String, Instant)
    }

    class LicensePlateStagedEvent {
        -LicensePlateId licensePlateId
        -BinLocation fromLocation
        -BinLocation stagingLocation
        -String stagedBy
        -Instant stagedAt
        +LicensePlateStagedEvent(LicensePlateId, BinLocation, BinLocation, String, Instant)
    }

    class LicensePlateStatusChangedEvent {
        -LicensePlateId licensePlateId
        -LicensePlateStatus fromStatus
        -LicensePlateStatus toStatus
        +LicensePlateStatusChangedEvent(LicensePlateId, LicensePlateStatus, LicensePlateStatus)
    }

    class LicensePlateCancelledEvent {
        -LicensePlateId licensePlateId
        -LicensePlateStatus previousStatus
        -String reason
        -String cancelledBy
        -Instant cancelledAt
        +LicensePlateCancelledEvent(LicensePlateId, LicensePlateStatus, String, String, Instant)
    }

    %% Domain Services
    class LicensePlateService {
        -LicensePlateRepository repository
        -LicensePlateGenerator generator
        +createLicensePlate(type LicensePlateType, createdBy String) LicensePlate
        +createLicensePlateWithId(licensePlateId LicensePlateId, type LicensePlateType, createdBy String) LicensePlate
        +receiveLicensePlate(licensePlateId LicensePlateId, location BinLocation, receivedBy String, receivingReference String) LicensePlate
        +moveLicensePlate(licensePlateId LicensePlateId, newLocation BinLocation, movedBy String) LicensePlate
        +addInventoryToLicensePlate(licensePlateId LicensePlateId, item SkuCode, quantity Quantity) LicensePlate
        +removeInventoryFromLicensePlate(licensePlateId LicensePlateId, item SkuCode, quantity Quantity) LicensePlate
        +pickLicensePlate(licensePlateId LicensePlateId, pickedBy String) LicensePlate
        +stageLicensePlate(licensePlateId LicensePlateId, stagingLocation BinLocation, stagedBy String) LicensePlate
        +shipLicensePlate(licensePlateId LicensePlateId, shipmentReference String, shippedBy String) LicensePlate
        +nestLicensePlates(parentId LicensePlateId, childIds List~LicensePlateId~) LicensePlate
        +unnestLicensePlates(parentId LicensePlateId, childIds List~LicensePlateId~) LicensePlate
        +findAvailableForPicking() List~LicensePlate~
        +findAvailableForShipping() List~LicensePlate~
        +findByLocation(location BinLocation) List~LicensePlate~
        +findByItem(item SkuCode) List~LicensePlate~
        +getLicensePlateHierarchy(licensePlateId LicensePlateId) LicensePlateHierarchy
        +getInventorySummary(licensePlateId LicensePlateId) LicensePlateInventorySummary
    }

    class LicensePlateGenerator {
        <<interface>>
        +generateId() LicensePlateId
        +generateBarcode(licensePlateId LicensePlateId) String
        +validateFormat(licensePlateId String) boolean
    }

    class DefaultLicensePlateGenerator {
        -String prefix
        -int sequence
        +generateId() LicensePlateId
        +generateBarcode(licensePlateId LicensePlateId) String
        +validateFormat(licensePlateId String) boolean
        -generateSequentialId() String
        -formatWithChecksum(id String) String
    }

    %% Domain Service Results
    class LicensePlateHierarchy {
        -LicensePlate root
        -List~LicensePlate~ children
        -List~LicensePlate~ descendants
        -int totalDepth
        -int totalItems
        +getRoot() LicensePlate
        +getDirectChildren() List~LicensePlate~
        +getAllDescendants() List~LicensePlate~
        +getTotalDepth() int
        +getTotalItems() int
        +containsLicensePlate(licensePlateId LicensePlateId) boolean
    }

    class LicensePlateInventorySummary {
        -LicensePlateId licensePlateId
        -Map~SkuCode,Quantity~ directInventory
        -Map~SkuCode,Quantity~ totalInventory
        -int totalItems
        -int totalQuantity
        +getDirectInventory() Map~SkuCode,Quantity~
        +getTotalInventory() Map~SkuCode,Quantity~
        +getTotalItems() int
        +getTotalQuantity() int
        +hasItem(item SkuCode) boolean
        +getItemQuantity(item SkuCode) Quantity
    }

    %% Application Services
    class LicensePlateApplicationService {
        -LicensePlateService licensePlateService
        -LicensePlateRepository repository
        +createLicensePlate(command CreateLicensePlateCommand) LicensePlateId
        +receiveLicensePlate(command ReceiveLicensePlateCommand) void
        +moveLicensePlate(command MoveLicensePlateCommand) void
        +addInventory(command AddInventoryCommand) void
        +removeInventory(command RemoveInventoryCommand) void
        +pickLicensePlate(command PickLicensePlateCommand) void
        +shipLicensePlate(command ShipLicensePlateCommand) void
        +nestLicensePlates(command NestLicensePlatesCommand) void
    }

    class LicensePlateQueryService {
        -LicensePlateRepository repository
        +findById(licensePlateId LicensePlateId) LicensePlate
        +findByStatus(status LicensePlateStatus) List~LicensePlate~
        +findByType(type LicensePlateType) List~LicensePlate~
        +findByLocation(location BinLocation) List~LicensePlate~
        +findByItem(item SkuCode) List~LicensePlate~
        +findByParent(parentId LicensePlateId) List~LicensePlate~
        +findRootLicensePlates() List~LicensePlate~
    }

    %% Repository
    class LicensePlateRepository {
        <<interface>>
        +save(licensePlate LicensePlate) LicensePlate
        +findById(licensePlateId LicensePlateId) Optional~LicensePlate~
        +existsById(licensePlateId LicensePlateId) boolean
        +findByStatus(status LicensePlateStatus) List~LicensePlate~
        +findByType(type LicensePlateType) List~LicensePlate~
        +findByLocation(location BinLocation) List~LicensePlate~
        +findByItem(item SkuCode) List~LicensePlate~
        +findByParentLicensePlateId(parentId String) List~LicensePlate~
        +findAvailableForPicking() List~LicensePlate~
        +findAvailableForShipping() List~LicensePlate~
        +findEmptyLicensePlates() List~LicensePlate~
        +delete(licensePlate LicensePlate) void
    }

    %% Shared Value Objects
    class BinLocation {
        -String aisle
        -String rack
        -String level
        +of(aisle String, rack String, level String) BinLocation
        +toString() String
    }

    class SkuCode {
        -String value
        +of(value String) SkuCode
        +getValue() String
    }

    class Quantity {
        -int value
        +of(value int) Quantity
        +getValue() int
        +add(other Quantity) Quantity
        +subtract(other Quantity) Quantity
    }

    %% Relationships
    LicensePlate ||--|| LicensePlateId : "identified by"
    LicensePlate ||--|| LicensePlateStatus : "has status"
    LicensePlate ||--|| LicensePlateType : "has type"
    LicensePlate ||--o{ SkuCode : "contains inventory"
    LicensePlate ||--|| BinLocation : "located at"
    
    LicensePlate ..> LicensePlateCreatedEvent : "publishes"
    LicensePlate ..> LicensePlateReceivedEvent : "publishes"
    LicensePlate ..> LicensePlateMovedEvent : "publishes"
    LicensePlate ..> LicensePlateInventoryAddedEvent : "publishes"
    LicensePlate ..> LicensePlateInventoryRemovedEvent : "publishes"
    LicensePlate ..> LicensePlateNestingChangedEvent : "publishes"
    LicensePlate ..> LicensePlateShippedEvent : "publishes"
    LicensePlate ..> LicensePlatePickedEvent : "publishes"
    LicensePlate ..> LicensePlateStagedEvent : "publishes"
    LicensePlate ..> LicensePlateStatusChangedEvent : "publishes"
    LicensePlate ..> LicensePlateCancelledEvent : "publishes"
    
    LicensePlateGenerator <|-- DefaultLicensePlateGenerator : "implements"
    
    LicensePlateService ..> LicensePlateRepository : "uses"
    LicensePlateService ..> LicensePlateGenerator : "uses"
    LicensePlateService ..> LicensePlateHierarchy : "produces"
    LicensePlateService ..> LicensePlateInventorySummary : "produces"
    
    LicensePlateApplicationService ..> LicensePlateService : "uses"
    LicensePlateQueryService ..> LicensePlateRepository : "uses"
```

## Key Domain Concepts

### License Plate Aggregate
- **Root Entity**: `LicensePlate` - Manages containerization and inventory tracking
- **Identity**: `LicensePlateId` - Strongly typed UUID-based identifier with barcode support
- **Lifecycle**: CREATED → RECEIVED → AVAILABLE → PICKED → STAGED → SHIPPED
- **Business Rules**:
  - Hierarchical nesting support (parent-child relationships)
  - Inventory tracking at container level
  - Status-based operation restrictions
  - Location-based movement tracking

### License Plate Types
- **Physical Containers**:
  - `PALLET`: Large shipping containers
  - `TOTE`: Reusable storage containers
  - `CARTON`: Individual shipping boxes
  - `CASE`: Product cases or multi-packs
  - `CONTAINER`: Large freight containers

- **Logical Containers**:
  - `VIRTUAL`: Software-defined groupings
  - `MIXED`: Multi-type container combinations

### Domain Events
- **LicensePlateCreatedEvent**: Published when license plate is created
- **LicensePlateReceivedEvent**: Published when received at warehouse
- **LicensePlateMovedEvent**: Published for location changes
- **LicensePlateInventoryAddedEvent**: Published when inventory added
- **LicensePlateInventoryRemovedEvent**: Published when inventory removed
- **LicensePlateNestingChangedEvent**: Published for hierarchy changes
- **LicensePlateShippedEvent**: Published when shipped from warehouse

### Domain Services
- **LicensePlateService**: Orchestrates license plate operations and lifecycle
- **LicensePlateGenerator**: Generates unique identifiers and barcodes
- **Hierarchy Management**: Manages parent-child relationships and nesting

## Business Rules

1. **Creation Rules**:
   - License plates must have unique identifiers
   - Type determines container characteristics and capabilities
   - Creation generates unique barcode for physical scanning

2. **Inventory Rules**:
   - Only containers in receivable status can accept inventory
   - Inventory removal requires sufficient available quantity
   - Empty containers can be reused or returned to pool

3. **Movement Rules**:
   - License plates in SHIPPED or CANCELLED status cannot be moved
   - Movement tracking maintains location history
   - Staged license plates require shipping confirmation

4. **Nesting Rules**:
   - Containers can contain child license plates
   - Circular nesting relationships are prevented
   - Hierarchy depth may be limited by business policy

5. **Status Transition Rules**:
   - CREATED → RECEIVED (via receiving process)
   - RECEIVED → AVAILABLE (via quality/put-away completion)
   - AVAILABLE → PICKED (via picking process)
   - PICKED → STAGED (via staging process)
   - STAGED → SHIPPED (via shipping process)

## License Plate Lifecycle

1. **Creation Phase**:
   - Generate unique license plate ID
   - Assign container type and initial status
   - Create barcode for physical identification

2. **Receiving Phase**:
   - Receive at dock or staging area
   - Update location and receiving reference
   - Change status to RECEIVED

3. **Inventory Management**:
   - Add/remove inventory items
   - Track quantity changes and movements
   - Maintain inventory accuracy

4. **Picking Phase**:
   - Select for order fulfillment
   - Update status to PICKED
   - Track picker identity and timing

5. **Staging Phase**:
   - Move to shipping staging area
   - Prepare for outbound shipment
   - Group with other containers if needed

6. **Shipping Phase**:
   - Load onto transport vehicle
   - Update shipment reference
   - Final status change to SHIPPED

## Hierarchy Management

### Nesting Capabilities
- **Container-in-Container**: Physical nesting support
- **Multi-Level Hierarchies**: Unlimited depth support
- **Inventory Aggregation**: Roll-up inventory from children
- **Bulk Operations**: Operations on container families

### Hierarchy Operations
- **Nest**: Add child license plates to parent
- **Unnest**: Remove child license plates from parent
- **Move Hierarchy**: Move parent and all children together
- **Split**: Separate child license plates to different parents
- **Merge**: Combine multiple license plates under single parent

## Integration Points

- **Receiving Domain**: License plate creation and receiving workflows
- **Picking Domain**: License plate selection and picking operations
- **Shipping Domain**: License plate staging and shipping processes
- **Inventory Domain**: Real-time inventory tracking and updates
- **Location Domain**: Location-based license plate management
- **Barcode System**: Physical scanning and identification
- **Mobile Operations**: Mobile license plate operations and tracking

## Query Patterns

- **By Status**: Find license plates in specific status
- **By Location**: Find license plates at specific locations
- **By Item**: Find license plates containing specific items
- **By Type**: Find license plates of specific container types
- **By Parent**: Find child license plates of specific parent
- **Root Plates**: Find top-level license plates without parents
- **Available for Picking**: Find license plates ready for order fulfillment
- **Available for Shipping**: Find license plates ready for outbound

## Inventory Tracking

### Direct Inventory
- Items physically contained within the license plate
- Quantity tracking at SKU level
- Real-time addition and removal operations

### Aggregated Inventory
- Combined inventory from license plate and all children
- Hierarchical roll-up of quantities
- Used for order fulfillment and allocation decisions

### Inventory Operations
- **Add**: Increase item quantity in container
- **Remove**: Decrease item quantity from container
- **Transfer**: Move items between license plates
- **Consolidate**: Combine inventory from multiple containers
- **Split**: Divide inventory across multiple containers

## Event-Driven Workflows

1. **Receiving Workflow**: ContainerArrived → LicensePlateCreated → InventoryAdded → LicensePlateReceived → MadeAvailable
2. **Picking Workflow**: OrderAllocated → LicensePlateSelected → LicensePlatePicked → InventoryRemoved → OrderFulfilled
3. **Shipping Workflow**: OrderReady → LicensePlateStaged → ShipmentCreated → LicensePlateShipped → ContainerDispatched
4. **Nesting Workflow**: NestingRequested → HierarchyValidated → ChildrenNested → HierarchyUpdated → InventoryAggregated