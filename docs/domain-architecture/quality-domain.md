# Quality Domain - Domain Model

## Overview
The Quality domain manages quality inspections, holds, samples, and corrective actions for warehouse operations. It ensures product quality compliance through systematic inspection processes, sampling plans, and quality control measures.

## Domain Model Diagram

```mermaid
classDiagram
    %% Quality Inspection Aggregate Root
    class QualityInspection {
        -QualityInspectionId inspectionId
        -QualityInspectionType inspectionType
        -QualityInspectionStatus status
        -SkuCode item
        -Quantity inspectionQuantity
        -BinLocation location
        -String lotNumber
        -String serialNumber
        -String supplierReference
        -QualityInspectionPlan inspectionPlan
        -List~QualityInspectionStep~ inspectionSteps
        -List~QualityTestResult~ testResults
        -List~QualityNonConformance~ nonConformances
        -QualityDecision finalDecision
        -String inspectorId
        -String supervisorId
        -Instant scheduledDate
        -Instant startedAt
        -Instant completedAt
        +startInspection(inspectorId String) void
        +completeInspectionStep(stepNumber int, result QualityTestResult, notes String) void
        +completeInspection(decision QualityDecision, supervisorId String, comments String) void
        +putOnHold(reason QualityHoldReason, notes String, heldBy String) void
        +releaseFromHold(notes String, releasedBy String) void
        +cancel(reason String, cancelledBy String) void
        +addNonConformance(type QualityNonConformanceType, description String, severity QualitySeverity, identifiedBy String) void
        +assignSupervisor(supervisorId String) void
        +isCompleted() boolean
        +hasNonConformances() boolean
        +getCompletionPercentage() double
    }

    %% Quality Inspection Step Entity
    class QualityInspectionStep {
        -int stepNumber
        -String name
        -String description
        -QualityTestType testType
        -boolean mandatory
        -String expectedValue
        -QualityToleranceRange toleranceRange
        -String unit
        -boolean completed
        -QualityTestResult testResult
        -String notes
        -Instant completedAt
        +complete(testResult QualityTestResult, notes String) void
        +isCompleted() boolean
        +isMandatory() boolean
        +validate() boolean
    }

    %% Quality Hold Aggregate Root
    class QualityHold {
        -QualityHoldId holdId
        -SkuCode item
        -String batchNumber
        -int quantity
        -QualityHoldReason reason
        -String heldBy
        -Instant heldAt
        -QualityHoldStatus status
        -String releasedBy
        -Instant releasedAt
        -List~QualityHoldNote~ notes
        -QualityHoldPriority priority
        +release(releasedBy String, notes String) void
        +escalate(newReason QualityHoldReason, escalatedBy String) void
        +addNote(note String, addedBy String) void
        +isActive() boolean
    }

    %% Quality Sample Aggregate Root
    class QualitySample {
        -QualitySampleId sampleId
        -QualitySamplingPlanId samplingPlanId
        -String batchNumber
        -SkuCode item
        -int sampleSize
        -List~QualityTestType~ requiredTests
        -Instant collectedAt
        -String collectedBy
        -QualitySampleStatus status
        -List~QualityTestResult~ testResults
        -QualitySampleVerdict verdict
        -String notes
        +startTesting() void
        +addTestResult(testResult QualityTestResult) void
        +completeTesting() void
        +reject(reason String) void
        +isComplete() boolean
    }

    %% Quality Corrective Action Aggregate Root
    class QualityCorrectiveAction {
        -QualityCorrectiveActionId actionId
        -QualityNonConformanceId nonConformanceId
        -QualityCorrectiveActionType actionType
        -QualityCorrectiveActionStatus status
        -String description
        -String assignedTo
        -QualityCorrectiveActionPriority priority
        -Instant dueDate
        -List~QualityCorrectiveActionStep~ actionSteps
        -String createdBy
        -Instant createdAt
        -Instant completedAt
        +start(assignee String) void
        +addStep(description String, assignee String, dueDate Instant) void
        +completeStep(stepNumber int, notes String) void
        +complete(completedBy String, effectivenessNotes String) void
        +cancel(reason String, cancelledBy String) void
        +verifyEffectiveness(effectiveness QualityCorrectiveActionEffectiveness, notes String) void
        +isCompleted() boolean
        +getProgress() double
    }

    %% Value Objects
    class QualityInspectionId {
        -UUID value
        +generate() QualityInspectionId
        +of(id String) QualityInspectionId
        +getValue() UUID
    }

    class QualityHoldId {
        -UUID value
        +generate() QualityHoldId
        +of(id String) QualityHoldId
        +getValue() UUID
    }

    class QualitySampleId {
        -UUID value
        +generate() QualitySampleId
        +of(id String) QualitySampleId
        +getValue() UUID
    }

    class QualityCorrectiveActionId {
        -UUID value
        +generate() QualityCorrectiveActionId
        +of(id String) QualityCorrectiveActionId
        +getValue() UUID
    }

    class QualityNonConformanceId {
        -UUID value
        +generate() QualityNonConformanceId
        +of(id String) QualityNonConformanceId
        +getValue() UUID
    }

    class QualityTestResult {
        -QualityTestType testType
        -String actualValue
        -String expectedValue
        -boolean passed
        -String unit
        -String notes
        -Instant testedAt
        +isPassed() boolean
        +getDeviation() double
        +isWithinTolerance(toleranceRange QualityToleranceRange) boolean
    }

    class QualityNonConformance {
        -QualityNonConformanceId id
        -QualityNonConformanceType type
        -String description
        -QualitySeverity severity
        -QualityNonConformanceStatus status
        -String identifiedBy
        -Instant identifiedAt
        +close(resolution String, closedBy String) void
        +isOpen() boolean
    }

    %% Enums
    class QualityInspectionStatus {
        <<enumeration>>
        SCHEDULED
        IN_PROGRESS
        ON_HOLD
        COMPLETED
        CANCELLED
    }

    class QualityInspectionType {
        <<enumeration>>
        INCOMING_INSPECTION
        IN_PROCESS_INSPECTION
        FINAL_INSPECTION
        CUSTOMER_INSPECTION
        AUDIT_INSPECTION
    }

    class QualityHoldStatus {
        <<enumeration>>
        ACTIVE
        RELEASED
        EXPIRED
    }

    class QualityHoldReason {
        <<enumeration>>
        PENDING_TEST_RESULTS
        INSPECTOR_UNAVAILABLE
        EQUIPMENT_MALFUNCTION
        DOCUMENTATION_MISSING
        CUSTOMER_REQUEST
        SUPPLIER_NOTIFICATION
        CORRECTIVE_ACTION_PENDING
        SAMPLE_PREPARATION
    }

    class QualitySampleStatus {
        <<enumeration>>
        COLLECTED
        TESTING_IN_PROGRESS
        TESTING_COMPLETED
        REJECTED
    }

    class QualityDecision {
        <<enumeration>>
        ACCEPT
        REJECT
        CONDITIONAL_ACCEPT
        REWORK_REQUIRED
    }

    class QualitySeverity {
        <<enumeration>>
        LOW
        MEDIUM
        HIGH
        CRITICAL
    }

    %% Domain Events
    class QualityInspectionScheduledEvent {
        -QualityInspectionId inspectionId
        -QualityInspectionType inspectionType
        -SkuCode item
        -String inspectorId
        -Instant scheduledDate
        +QualityInspectionScheduledEvent(QualityInspectionId, QualityInspectionType, SkuCode, String, Instant)
    }

    class QualityInspectionCompletedEvent {
        -QualityInspectionId inspectionId
        -QualityDecision finalDecision
        -String supervisorId
        -Instant completedAt
        -boolean hasNonConformances
        +QualityInspectionCompletedEvent(QualityInspectionId, QualityDecision, String, Instant, boolean)
    }

    class QualityHoldCreatedEvent {
        -QualityHoldId holdId
        -SkuCode item
        -String batchNumber
        -int quantity
        -QualityHoldReason reason
        -String heldBy
        -Instant heldAt
        +QualityHoldCreatedEvent(QualityHoldId, SkuCode, String, int, QualityHoldReason, String, Instant, String)
    }

    class QualitySampleCollectedEvent {
        -QualitySampleId sampleId
        -QualitySamplingPlanId samplingPlanId
        -String batchNumber
        -SkuCode item
        -int sampleSize
        -Instant collectedAt
        +QualitySampleCollectedEvent(QualitySampleId, QualitySamplingPlanId, String, SkuCode, int, Instant)
    }

    class QualityCorrectiveActionCreatedEvent {
        -QualityCorrectiveActionId actionId
        -QualityNonConformanceId nonConformanceId
        -String assignedTo
        -QualityCorrectiveActionPriority priority
        -Instant createdAt
        +QualityCorrectiveActionCreatedEvent(QualityCorrectiveActionId, QualityNonConformanceId, String, QualityCorrectiveActionPriority, Instant)
    }

    %% Domain Services
    class QualityInspectionService {
        +scheduleInspection(item SkuCode, inspectionType QualityInspectionType, location BinLocation) QualityInspection
        +assignInspector(inspectionId QualityInspectionId, inspectorId String) void
        +validateInspectionCompletion(inspection QualityInspection) boolean
        +determineInspectionPlan(item SkuCode, inspectionType QualityInspectionType) QualityInspectionPlan
    }

    class QualityHoldService {
        +createHold(item SkuCode, batchNumber String, quantity int, reason QualityHoldReason, heldBy String) QualityHold
        +evaluateHoldRelease(holdId QualityHoldId) boolean
        +escalateHold(holdId QualityHoldId, newReason QualityHoldReason, escalatedBy String) void
    }

    class QualityWorkIntegrationService {
        +createQualityWorkTask(inspectionId QualityInspectionId) WorkId
        +updateWorkFromInspection(inspectionId QualityInspectionId, workId WorkId) void
    }

    %% Application Services
    class QualityInspectionApplicationService {
        -QualityInspectionRepository inspectionRepository
        -QualityInspectionService inspectionService
        +scheduleInspection(command ScheduleInspectionCommand) QualityInspectionId
        +startInspection(command StartInspectionCommand) void
        +completeInspectionStep(command CompleteInspectionStepCommand) void
        +completeInspection(command CompleteInspectionCommand) void
    }

    class QualityHoldApplicationService {
        -QualityHoldRepository holdRepository
        -QualityHoldService holdService
        +createHold(command CreateQualityHoldCommand) QualityHoldId
        +releaseHold(command ReleaseHoldCommand) void
        +escalateHold(command EscalateHoldCommand) void
    }

    %% Repositories
    class QualityInspectionRepository {
        <<interface>>
        +save(inspection QualityInspection) void
        +findById(id QualityInspectionId) QualityInspection
        +findByItem(item SkuCode) List~QualityInspection~
        +findByStatus(status QualityInspectionStatus) List~QualityInspection~
        +findByInspector(inspectorId String) List~QualityInspection~
        +findOverdue() List~QualityInspection~
    }

    class QualityHoldRepository {
        <<interface>>
        +save(hold QualityHold) void
        +findById(id QualityHoldId) QualityHold
        +findByItem(item SkuCode) List~QualityHold~
        +findActiveHolds() List~QualityHold~
        +findByReason(reason QualityHoldReason) List~QualityHold~
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
    }

    class BinLocation {
        -String aisle
        -String rack
        -String level
        +of(aisle String, rack String, level String) BinLocation
        +toString() String
    }

    %% Relationships
    QualityInspection ||--|| QualityInspectionId : "identified by"
    QualityInspection ||--o{ QualityInspectionStep : "contains"
    QualityInspection ||--|| QualityInspectionStatus : "has status"
    QualityInspection ||--|| QualityInspectionType : "has type"
    QualityInspection ||--o{ QualityTestResult : "produces"
    QualityInspection ||--o{ QualityNonConformance : "identifies"
    
    QualityHold ||--|| QualityHoldId : "identified by"
    QualityHold ||--|| QualityHoldStatus : "has status"
    QualityHold ||--|| QualityHoldReason : "has reason"
    
    QualitySample ||--|| QualitySampleId : "identified by"
    QualitySample ||--|| QualitySampleStatus : "has status"
    QualitySample ||--o{ QualityTestResult : "produces"
    
    QualityCorrectiveAction ||--|| QualityCorrectiveActionId : "identified by"
    QualityCorrectiveAction ||--|| QualityNonConformanceId : "addresses"
    
    QualityInspection ..> QualityInspectionScheduledEvent : "publishes"
    QualityInspection ..> QualityInspectionCompletedEvent : "publishes"
    QualityHold ..> QualityHoldCreatedEvent : "publishes"
    QualitySample ..> QualitySampleCollectedEvent : "publishes"
    QualityCorrectiveAction ..> QualityCorrectiveActionCreatedEvent : "publishes"
    
    QualityInspectionService ..> QualityInspectionRepository : "uses"
    QualityHoldService ..> QualityHoldRepository : "uses"
    
    QualityInspectionApplicationService ..> QualityInspectionService : "uses"
    QualityHoldApplicationService ..> QualityHoldService : "uses"
```

## Key Domain Concepts

### Quality Inspection Aggregate
- **Root Entity**: `QualityInspection` - Manages systematic quality inspection processes
- **Identity**: `QualityInspectionId` - Strongly typed UUID-based identifier
- **Lifecycle**: SCHEDULED → IN_PROGRESS → COMPLETED/CANCELLED
- **Business Rules**:
  - All mandatory steps must be completed before inspection completion
  - Non-conformances trigger corrective action workflows
  - Supervisor approval required for critical decisions

### Quality Hold Aggregate
- **Root Entity**: `QualityHold` - Manages quality-related inventory holds
- **Identity**: `QualityHoldId` - Strongly typed UUID-based identifier
- **Lifecycle**: ACTIVE → RELEASED/EXPIRED
- **Business Rules**:
  - Holds prevent inventory movement until released
  - Priority escalation based on hold reasons
  - Release requires proper authorization

### Quality Sample Aggregate
- **Root Entity**: `QualitySample` - Manages sampling and testing processes
- **Identity**: `QualitySampleId` - Strongly typed UUID-based identifier
- **Lifecycle**: COLLECTED → TESTING_IN_PROGRESS → TESTING_COMPLETED/REJECTED
- **Business Rules**:
  - All required tests must be completed
  - Sample verdict determines batch acceptance
  - Failed samples trigger quality holds

### Quality Corrective Action Aggregate
- **Root Entity**: `QualityCorrectiveAction` - Manages corrective and preventive actions
- **Identity**: `QualityCorrectiveActionId` - Strongly typed UUID-based identifier
- **Business Rules**:
  - Created automatically from critical non-conformances
  - Action steps must be completed in sequence
  - Effectiveness verification required for closure

### Domain Events
- **QualityInspectionScheduledEvent**: Published when inspection is scheduled
- **QualityInspectionCompletedEvent**: Published when inspection is completed
- **QualityHoldCreatedEvent**: Published when quality hold is created
- **QualitySampleCollectedEvent**: Published when sample is collected
- **QualityCorrectiveActionCreatedEvent**: Published when corrective action is initiated

### Domain Services
- **QualityInspectionService**: Orchestrates inspection scheduling and execution
- **QualityHoldService**: Manages quality hold lifecycle and escalation
- **QualityWorkIntegrationService**: Integrates quality processes with work management

## Business Rules

1. **Inspection Rules**:
   - Inspections must follow predefined inspection plans
   - All mandatory inspection steps must be completed
   - Non-conformances automatically trigger corrective actions
   - Supervisor approval required for final quality decisions

2. **Hold Management**:
   - Quality holds prevent inventory movement
   - Hold priority determines escalation timeframes
   - Release requires proper authorization and documentation
   - Expired holds trigger automatic notifications

3. **Sampling Rules**:
   - Sample size determined by sampling plan
   - All required tests must be completed before verdict
   - Failed samples trigger batch holds
   - Sample results influence inspection frequency

4. **Corrective Action Rules**:
   - Critical non-conformances automatically create corrective actions
   - Action steps must be completed within due dates
   - Effectiveness verification required for closure
   - Root cause analysis mandatory for recurring issues

## Integration Points

- **Work Domain**: Quality inspections generate work tasks
- **Inventory Domain**: Quality holds prevent inventory transactions
- **PickList Domain**: Quality flags influence picking priorities
- **Location Domain**: Quality zones segregate held inventory
- **Mobile Domain**: Mobile quality inspections and sampling
- **Notification Domain**: Quality alerts and escalation notifications

## Query Patterns

- **By Status**: Find inspections/holds by current status
- **By Item**: Find quality history for specific SKU
- **By Inspector**: Find inspections assigned to inspector
- **Overdue**: Find overdue inspections and corrective actions
- **By Severity**: Find critical non-conformances requiring attention
- **Active Holds**: Find all active quality holds

## Event-Driven Workflows

1. **Inspection Workflow**: ItemReceived → InspectionScheduled → InspectionStarted → StepsCompleted → InspectionCompleted
2. **Hold Workflow**: NonConformanceDetected → QualityHoldCreated → HoldReleaseEvaluated → HoldReleased
3. **Sampling Workflow**: BatchReceived → SampleCollected → TestingStarted → TestsCompleted → VerdictDetermined
4. **Corrective Action Workflow**: CriticalNonConformance → CorrectiveActionCreated → StepsCompleted → EffectivenessVerified