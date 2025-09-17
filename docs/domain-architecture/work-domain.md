# Work Domain - Domain Model

## Overview
The Work domain represents the core aggregate for managing warehouse work tasks and operations. It handles work assignment, execution, quality inspection, and lifecycle management.

## Domain Model Diagram

```mermaid
classDiagram
    %% Work Aggregate Root
    class Work {
        -WorkId id
        -WorkTemplateId templateId
        -WorkStatus status
        -BinLocation location
        -SkuCode skuCode
        -Quantity quantity
        -String assignedTo
        -List~WorkStep~ steps
        -boolean requiresQualityInspection
        -LocalDateTime createdAt
        -LocalDateTime assignedAt
        -LocalDateTime startedAt
        -LocalDateTime completedAt
        +assignTo(workerId String) void
        +start() void
        +completeCurrentStep() void
        +complete() void
        +cancel() void
        +release() void
        +getCurrentStep() WorkStep
        +getProgress() WorkProgress
    }

    %% Work Entity
    class WorkStep {
        -int sequence
        -WorkStepType stepType
        -String action
        -String description
        -Map~String,Object~ parameters
        -boolean mandatory
        -boolean completed
        -LocalDateTime completedAt
        -String completedBy
        +complete(workerId String) void
        +validate() boolean
        +isOptional() boolean
    }

    %% Value Objects
    class WorkId {
        -UUID value
        +of(String id) WorkId
        +generate() WorkId
        +getValue() UUID
    }

    class WorkTemplateId {
        -UUID value
        +of(String id) WorkTemplateId
        +generate() WorkTemplateId
    }

    class WorkProgress {
        -int completedSteps
        -int totalSteps
        -double percentageComplete
        +calculateProgress() WorkProgress
        +isComplete() boolean
    }

    %% Enums
    class WorkStatus {
        <<enumeration>>
        CREATED
        ASSIGNED
        IN_PROGRESS
        COMPLETED
        CANCELLED
        RELEASED
    }

    class WorkStepType {
        <<enumeration>>
        PICK
        PUT
        COUNT
        PACK
        VALIDATE
        QUALITY_CHECK
    }

    %% Domain Events
    class WorkAssignedEvent {
        -WorkId workId
        -String assignedTo
        -LocalDateTime assignedAt
        +WorkAssignedEvent(WorkId, String, LocalDateTime)
    }

    class WorkStartedEvent {
        -WorkId workId
        -String startedBy
        -LocalDateTime startedAt
        +WorkStartedEvent(WorkId, String, LocalDateTime)
    }

    class WorkCompletedEvent {
        -WorkId workId
        -String completedBy
        -LocalDateTime completedAt
        +WorkCompletedEvent(WorkId, String, LocalDateTime)
    }

    class WorkCancelledEvent {
        -WorkId workId
        -String reason
        -LocalDateTime cancelledAt
        +WorkCancelledEvent(WorkId, String, LocalDateTime)
    }

    %% Domain Service
    class WorkCreationService {
        -WorkTemplateRepository templateRepo
        +createWork(request WorkCreationRequest) Work
        +createBatchWork(requests List~WorkCreationRequest~) List~Work~
        +validateWorkCreation(request WorkCreationRequest) ValidationResult
        -selectTemplate(criteria WorkCriteria) WorkTemplate
        -applyBusinessRules(work Work) void
    }

    %% Repository
    class WorkRepository {
        <<interface>>
        +save(work Work) void
        +findById(id WorkId) Work
        +findByAssignee(assignee String) List~Work~
        +findByStatus(status WorkStatus) List~Work~
        +findByLocation(location BinLocation) List~Work~
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
    }

    %% Relationships
    Work ||--|| WorkId : "identified by"
    Work ||--o{ WorkStep : "contains"
    Work ||--|| WorkStatus : "has"
    Work ||--|| BinLocation : "assigned to"
    Work ||--|| SkuCode : "for item"
    Work ||--|| Quantity : "with quantity"
    Work ||--|| WorkTemplateId : "created from"
    
    WorkStep ||--|| WorkStepType : "has type"
    
    Work ..> WorkAssignedEvent : "publishes"
    Work ..> WorkStartedEvent : "publishes" 
    Work ..> WorkCompletedEvent : "publishes"
    Work ..> WorkCancelledEvent : "publishes"
    
    WorkCreationService ..> WorkRepository : "uses"
    WorkCreationService ..> Work : "creates"
```

## Key Domain Concepts

### Work Aggregate
- **Root Entity**: `Work` - Central aggregate managing warehouse tasks
- **Identity**: `WorkId` - Strongly typed UUID-based identifier
- **Lifecycle**: CREATED → ASSIGNED → IN_PROGRESS → COMPLETED/CANCELLED
- **Business Rules**:
  - Work cannot be started without assignment
  - Quality inspection required for specific work types
  - All mandatory steps must be completed

### Work Step Entity
- **Purpose**: Represents individual steps within work execution
- **Identity**: Sequence number within the work aggregate
- **Types**: PICK, PUT, COUNT, PACK, VALIDATE, QUALITY_CHECK
- **Validation**: Each step can validate its completion criteria

### Domain Events
- **WorkAssignedEvent**: Published when work is assigned to worker
- **WorkStartedEvent**: Published when work execution begins
- **WorkCompletedEvent**: Published when all steps are completed
- **WorkCancelledEvent**: Published when work is cancelled

### Domain Service
- **WorkCreationService**: Orchestrates work creation using templates and business rules
- **Responsibilities**: Template selection, validation, batch creation

## Business Rules

1. **Assignment Rules**:
   - Work must be assigned before it can be started
   - Only one worker can be assigned at a time
   - Assignment creates WorkAssignedEvent

2. **Execution Rules**:
   - Steps must be completed in sequence order
   - Mandatory steps cannot be skipped
   - Quality inspection triggered for specific criteria

3. **Completion Rules**:
   - All mandatory steps must be completed
   - Quality inspection must pass if required
   - Completion triggers downstream processes

4. **Cancellation Rules**:
   - Work can be cancelled at any stage
   - Cancellation requires reason
   - Resources are released for reallocation

## Integration Points

- **Quality Domain**: Work may trigger quality inspections
- **PickList Domain**: Pick work types integrate with pick lists
- **Location Domain**: Work location assignment uses location directives
- **Mobile Domain**: Mobile workers interact with work assignments