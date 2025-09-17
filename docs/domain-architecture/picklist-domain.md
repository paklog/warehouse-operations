# PickList Domain - Domain Model

## Overview
The PickList domain manages pick list operations, pick instructions, and picking workflow. It coordinates with the Work domain and integrates with mobile operations.

## Domain Model Diagram

```mermaid
classDiagram
    %% PickList Aggregate Root
    class PickList {
        -PickListId id
        -PickListStatus status
        -OrderId orderId
        -String pickerId
        -List~PickInstruction~ instructions
        -LocalDateTime createdAt
        -LocalDateTime assignedAt
        -LocalDateTime completedAt
        +assignToPicker(pickerId String) void
        +addInstruction(instruction PickInstruction) void
        +pickItem(skuCode SkuCode, quantity Quantity, location BinLocation) void
        +complete() void
        +getProgress() PickProgress
        +isCompleted() boolean
        +canBeAssigned() boolean
    }

    %% Pick Instruction Entity
    class PickInstruction {
        -SkuCode skuCode
        -Quantity quantity
        -BinLocation location
        -boolean completed
        -LocalDateTime completedAt
        -Quantity pickedQuantity
        +complete(pickedQty Quantity) void
        +isCompleted() boolean
        +getRemainingQuantity() Quantity
        +validate() boolean
    }

    %% Value Objects
    class PickListId {
        -UUID value
        +of(String id) PickListId
        +generate() PickListId
        +getValue() UUID
    }

    class OrderId {
        -UUID value
        +of(String id) OrderId
        +generate() OrderId
        +getValue() UUID
    }

    class PickProgress {
        -int completedInstructions
        -int totalInstructions
        -double percentageComplete
        +calculateProgress() PickProgress
        +isComplete() boolean
    }

    %% Enums
    class PickListStatus {
        <<enumeration>>
        PENDING
        ASSIGNED
        IN_PROGRESS
        COMPLETED
        CANCELLED
    }

    %% Domain Events
    class PickListCreatedEvent {
        -PickListId pickListId
        -OrderId orderId
        -LocalDateTime createdAt
        +PickListCreatedEvent(PickListId, OrderId, LocalDateTime)
    }

    class PickListAssignedEvent {
        -PickListId pickListId
        -String pickerId
        -LocalDateTime assignedAt
        +PickListAssignedEvent(PickListId, String, LocalDateTime)
    }

    class ItemPickedEvent {
        -PickListId pickListId
        -SkuCode skuCode
        -Quantity pickedQuantity
        -BinLocation location
        -LocalDateTime pickedAt
        +ItemPickedEvent(PickListId, SkuCode, Quantity, BinLocation, LocalDateTime)
    }

    class PickListCompletedEvent {
        -PickListId pickListId
        -String pickerId
        -LocalDateTime completedAt
        +PickListCompletedEvent(PickListId, String, LocalDateTime)
    }

    %% Commands
    class ConfirmItemPick {
        -PickListId pickListId
        -SkuCode skuCode
        -Quantity quantity
        -BinLocation binLocation
        +ConfirmItemPick(PickListId, SkuCode, Quantity, BinLocation)
    }

    %% Command Handler
    class ConfirmItemPickHandler {
        -PickListRepository repository
        +handle(command ConfirmItemPick) void
        -validatePick(command ConfirmItemPick, pickList PickList) void
    }

    %% Domain Service
    class PickListDomainService {
        +canPickBeCombined(pickList1 PickList, pickList2 PickList) boolean
        +validatePickListCompletion(pickList PickList) ValidationResult
        +calculateOptimalPickRoute(instructions List~PickInstruction~) List~PickInstruction~
        +estimatePickTime(instructions List~PickInstruction~) Duration
    }

    %% Repository
    class PickListRepository {
        <<interface>>
        +save(pickList PickList) void
        +findById(id PickListId) PickList
        +findByPickerId(pickerId String) List~PickList~
        +findByStatus(status PickListStatus) List~PickList~
        +findByOrderId(orderId OrderId) PickList
        +findNextPickListForPicker(pickerId String) Optional~PickList~
    }

    %% Query Service
    class PickListQueryService {
        -PickListRepository repository
        +findById(id PickListId) PickList
        +findByPickerId(pickerId String) List~PickList~
        +findByStatus(status PickListStatus) List~PickList~
        +findNextPickListForPicker(pickerId String) Optional~PickList~
    }

    %% Shared Value Objects
    class SkuCode {
        -String value
        +of(value String) SkuCode
        +getValue() String
    }

    class Quantity {
        -int value
        +of(value int) Quantity
        +getValue() int
        +subtract(other Quantity) Quantity
    }

    class BinLocation {
        -String aisle
        -String rack
        -String level
        +of(aisle String, rack String, level String) BinLocation
        +toString() String
    }

    %% Relationships
    PickList ||--|| PickListId : "identified by"
    PickList ||--o{ PickInstruction : "contains"
    PickList ||--|| PickListStatus : "has status"
    PickList ||--|| OrderId : "for order"
    
    PickInstruction ||--|| SkuCode : "for item"
    PickInstruction ||--|| Quantity : "with quantity"
    PickInstruction ||--|| BinLocation : "from location"
    
    PickList ..> PickListCreatedEvent : "publishes"
    PickList ..> PickListAssignedEvent : "publishes"
    PickList ..> ItemPickedEvent : "publishes"
    PickList ..> PickListCompletedEvent : "publishes"
    
    ConfirmItemPickHandler ..> PickListRepository : "uses"
    ConfirmItemPickHandler ..> ConfirmItemPick : "handles"
    
    PickListQueryService ..> PickListRepository : "uses"
    
    PickListDomainService ..> PickList : "operates on"
```

## Key Domain Concepts

### PickList Aggregate
- **Root Entity**: `PickList` - Manages picking operations for orders
- **Identity**: `PickListId` - Strongly typed UUID-based identifier
- **Lifecycle**: PENDING → ASSIGNED → IN_PROGRESS → COMPLETED
- **Business Rules**:
  - Must be assigned to picker before picking can start
  - All instructions must be completed for completion
  - Picker can only work on one pick list at a time

### PickInstruction Entity
- **Purpose**: Individual picking instruction within a pick list
- **Identity**: Combination of SKU and location
- **Properties**: Item to pick, quantity, source location
- **Validation**: Confirms correct item and quantity picked

### Domain Events
- **PickListCreatedEvent**: Published when new pick list is created
- **PickListAssignedEvent**: Published when assigned to picker
- **ItemPickedEvent**: Published for each completed pick instruction
- **PickListCompletedEvent**: Published when all instructions completed

### Commands and Handlers
- **ConfirmItemPick**: Command to confirm item pick
- **ConfirmItemPickHandler**: Processes pick confirmations with validation

### Domain Services
- **PickListDomainService**: Orchestrates complex pick list operations
- **Responsibilities**: Route optimization, time estimation, combination rules

## Business Rules

1. **Assignment Rules**:
   - PickList must be in PENDING status to be assigned
   - Only one picker can be assigned at a time
   - Assignment transitions status to ASSIGNED

2. **Picking Rules**:
   - Items must be picked from specified locations
   - Quantity picked must match instruction quantity
   - Each pick generates ItemPickedEvent

3. **Completion Rules**:
   - All instructions must be completed
   - Completion validation checks all items picked
   - Triggers downstream packaging processes

4. **Route Optimization**:
   - Instructions can be reordered for optimal picking route
   - Location-based optimization reduces travel time
   - Priority items can be picked first

## Integration Points

- **Order Domain**: PickList created from Order fulfillment
- **Work Domain**: Pick instructions may generate Work tasks
- **Location Domain**: Pick locations validated against location master
- **Mobile Domain**: Mobile pickers interact with pick lists
- **Packaging Domain**: Completed pick lists trigger packaging
- **Inventory Domain**: Pick confirmations update inventory levels

## Query Patterns

- **By Picker**: Find all pick lists for specific picker
- **By Status**: Find pick lists in specific status
- **By Order**: Find pick list for specific order
- **Next Available**: Find next pick list for picker assignment

## Event-Driven Workflows

1. **Order Processing**: OrderCreatedEvent → PickListCreatedEvent
2. **Pick Assignment**: PickerAvailable → PickListAssignedEvent
3. **Pick Execution**: ItemPicked → ItemPickedEvent
4. **Pick Completion**: AllItemsPicked → PickListCompletedEvent → PackageCreationEvent