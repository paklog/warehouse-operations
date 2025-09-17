# Wave Domain - Domain Model

## Overview
The Wave domain manages the grouping and batching of orders for efficient warehouse processing. It orchestrates the release and execution of order fulfillment work, optimizing picking operations through strategic order grouping and timing control.

## Domain Model Diagram

```mermaid
classDiagram
    %% Wave Aggregate Root
    class Wave {
        -WaveId id
        -WaveStatus status
        -List~OrderId~ orderIds
        -Instant plannedDate
        -Instant releaseDate
        -Instant closedDate
        -String carrier
        -Instant cutoffTime
        -String shippingSpeedCategory
        -int maxOrders
        -long version
        +release() void
        +close() void
        +cancel() void
        +setCarrier(carrier String) void
        +setCutoffTime(cutoffTime Instant) void
        +getOrderCount() int
        +isReleased() boolean
        +isClosed() boolean
        +isCancelled() boolean
        +canAddOrder(orderId OrderId) boolean
        +addOrder(orderId OrderId) void
        +removeOrder(orderId OrderId) void
        +getEstimatedDuration() Duration
        +getProgress() WaveProgress
    }

    %% Value Objects
    class WaveId {
        -UUID value
        +generate() WaveId
        +of(id String) WaveId
        +getValue() UUID
        +toString() String
    }

    class WaveProgress {
        -int totalOrders
        -int completedOrders
        -int inProgressOrders
        -double percentageComplete
        +calculateProgress(orderStatuses Map~OrderId,OrderStatus~) WaveProgress
        +isComplete() boolean
        +getCompletionPercentage() double
        +getRemainingOrders() int
    }

    class WavePlanningCriteria {
        -int maxOrders
        -Duration maxDuration
        -String carrier
        -String shippingSpeedCategory
        -Instant cutoffTime
        -Priority priority
        -List~String~ zones
        -List~SkuCode~ items
        +matches(order Order) boolean
        +canAccommodate(order Order) boolean
        +getCapacityRemaining() int
    }

    class WaveMetrics {
        -Duration plannedDuration
        -Duration actualDuration
        -int totalItems
        -int totalQuantity
        -double efficiency
        -List~String~ zones
        -Map~SkuCode,Integer~ itemFrequency
        +calculateEfficiency() double
        +getAverageOrderSize() double
        +getDensityScore() double
    }

    %% Enums
    class WaveStatus {
        <<enumeration>>
        PLANNED
        RELEASED
        IN_PROGRESS
        COMPLETED
        CLOSED
        CANCELLED
    }

    class WavePlanningStrategy {
        <<enumeration>>
        TIME_BASED
        CARRIER_BASED
        ZONE_BASED
        PRIORITY_BASED
        CAPACITY_BASED
        ITEM_AFFINITY
        MIXED_STRATEGY
    }

    class WaveType {
        <<enumeration>>
        STANDARD
        EXPRESS
        BULK
        CROSS_DOCK
        REPLENISHMENT
        RETURNS
        EMERGENCY
    }

    %% Domain Events
    class WaveCreatedEvent {
        -UUID eventId
        -Instant occurredAt
        -WaveId waveId
        -List~OrderId~ orderIds
        -Instant plannedDate
        -WavePlanningCriteria criteria
        +WaveCreatedEvent(WaveId, List~OrderId~, Instant, WavePlanningCriteria)
    }

    class WaveReleasedEvent {
        -UUID eventId
        -Instant occurredAt
        -WaveId waveId
        -List~OrderId~ orderIds
        -Instant releaseDate
        +WaveReleasedEvent(WaveId, List~OrderId~, Instant)
        +getOrderCount() int
    }

    class WaveClosedEvent {
        -UUID eventId
        -Instant occurredAt
        -WaveId waveId
        -Instant closedDate
        -WaveMetrics metrics
        +WaveClosedEvent(WaveId, Instant, WaveMetrics)
    }

    class WaveCancelledEvent {
        -UUID eventId
        -Instant occurredAt
        -WaveId waveId
        -Instant cancelledDate
        -String reason
        +WaveCancelledEvent(WaveId, Instant, String)
    }

    class WaveProgressUpdatedEvent {
        -UUID eventId
        -Instant occurredAt
        -WaveId waveId
        -WaveProgress progress
        -OrderId completedOrderId
        +WaveProgressUpdatedEvent(WaveId, WaveProgress, OrderId)
    }

    %% Domain Services
    class WavePlanningService {
        -WaveRepository waveRepository
        -OrderRepository orderRepository
        -WavePlanningEngine planningEngine
        +createWave(criteria WavePlanningCriteria) Wave
        +planWaves(orders List~Order~, strategies List~WavePlanningStrategy~) List~Wave~
        +optimizeWave(waveId WaveId) Wave
        +canCombineWaves(wave1 WaveId, wave2 WaveId) boolean
        +combineWaves(wave1 WaveId, wave2 WaveId) Wave
        +splitWave(waveId WaveId, criteria SplitCriteria) List~Wave~
        +validateWavePlan(wave Wave) WavePlanValidationResult
        +estimateWaveDuration(wave Wave) Duration
        +calculateWaveMetrics(wave Wave) WaveMetrics
    }

    class WaveExecutionService {
        -WaveRepository waveRepository
        -PickListService pickListService
        -WorkService workService
        +releaseWave(waveId WaveId) void
        +trackWaveProgress(waveId WaveId) WaveProgress
        +completeWave(waveId WaveId) void
        +handleWaveException(waveId WaveId, exception Exception) void
        +pauseWave(waveId WaveId, reason String) void
        +resumeWave(waveId WaveId) void
        +escalateWave(waveId WaveId, reason String) void
    }

    class WavePlanningEngine {
        -List~WavePlanningStrategy~ strategies
        -WaveOptimizer optimizer
        +generateWavePlan(orders List~Order~) List~Wave~
        +applyStrategy(strategy WavePlanningStrategy, orders List~Order~) List~Wave~
        +optimizeWaveSize(wave Wave) Wave
        +balanceWaveLoad(waves List~Wave~) List~Wave~
        +validateConstraints(wave Wave) boolean
    }

    %% Planning Strategies (Strategy Pattern)
    class WavePlanningStrategy {
        <<interface>>
        +planWaves(orders List~Order~) List~Wave~
        +canAccommodate(wave Wave, order Order) boolean
        +getOptimalWaveSize() int
        +getPriority() int
    }

    class TimeBasedWavePlanning {
        -Duration waveInterval
        -Instant cutoffTime
        +planWaves(orders List~Order~) List~Wave~
        +groupByTimeWindow(orders List~Order~) Map~Instant,List~Order~~
        -createTimeWindows() List~Instant~
    }

    class CarrierBasedWavePlanning {
        -Map~String,WaveConfig~ carrierConfigs
        +planWaves(orders List~Order~) List~Wave~
        +groupByCarrier(orders List~Order~) Map~String,List~Order~~
        -getCarrierConfig(carrier String) WaveConfig
    }

    class ZoneBasedWavePlanning {
        -List~String~ zones
        -ZoneAffinityMatrix affinityMatrix
        +planWaves(orders List~Order~) List~Wave~
        +groupByZoneAffinity(orders List~Order~) Map~List~String~,List~Order~~
        -calculateZoneEfficiency(zones List~String~) double
    }

    class PriorityBasedWavePlanning {
        -PriorityMatrix priorityMatrix
        +planWaves(orders List~Order~) List~Wave~
        +groupByPriority(orders List~Order~) Map~Priority,List~Order~~
        -balancePriorityMix(waves List~Wave~) List~Wave~
    }

    %% Application Services
    class WaveApplicationService {
        -WavePlanningService planningService
        -WaveExecutionService executionService
        -WaveRepository repository
        +createWave(command CreateWaveCommand) WaveId
        +planWaves(command PlanWavesCommand) List~WaveId~
        +releaseWave(command ReleaseWaveCommand) void
        +closeWave(command CloseWaveCommand) void
        +cancelWave(command CancelWaveCommand) void
        +optimizeWave(command OptimizeWaveCommand) void
        +trackWaveProgress(waveId WaveId) WaveProgress
    }

    class WaveQueryService {
        -WaveRepository repository
        +findById(waveId WaveId) Wave
        +findByStatus(status WaveStatus) List~Wave~
        +findByCarrier(carrier String) List~Wave~
        +findByDateRange(startDate Instant, endDate Instant) List~Wave~
        +findActiveWaves() List~Wave~
        +findOverdueWaves() List~Wave~
        +getWaveMetrics(waveId WaveId) WaveMetrics
        +getWaveProgress(waveId WaveId) WaveProgress
    }

    %% Repository
    class WaveRepository {
        <<interface>>
        +save(wave Wave) Wave
        +findById(waveId WaveId) Optional~Wave~
        +findByStatus(status WaveStatus) List~Wave~
        +findByCarrier(carrier String) List~Wave~
        +findByDateRange(startDate Instant, endDate Instant) List~Wave~
        +findByOrderId(orderId OrderId) Optional~Wave~
        +findActiveWaves() List~Wave~
        +findPlannedWaves() List~Wave~
        +delete(wave Wave) void
        +existsByOrderId(orderId OrderId) boolean
    }

    %% Supporting Classes
    class WavePlanValidationResult {
        -boolean valid
        -List~String~ violations
        -List~String~ warnings
        -WaveMetrics estimatedMetrics
        +isValid() boolean
        +hasWarnings() boolean
        +getViolations() List~String~
        +getWarnings() List~String~
    }

    class SplitCriteria {
        -int maxOrdersPerWave
        -Duration maxDurationPerWave
        -List~String~ splitByZones
        -boolean splitByPriority
        -boolean splitByCarrier
        +shouldSplit(wave Wave) boolean
        +getSplitPoints(wave Wave) List~Integer~
    }

    class WaveOptimizer {
        +optimizeWaveComposition(wave Wave) Wave
        +balanceWorkload(waves List~Wave~) List~Wave~
        +minimizeTravelDistance(wave Wave) Wave
        +maximizePickingEfficiency(wave Wave) Wave
        +consolidateSimilarItems(wave Wave) Wave
    }

    %% Shared Value Objects
    class OrderId {
        -UUID value
        +generate() OrderId
        +of(id String) OrderId
        +getValue() UUID
    }

    class SkuCode {
        -String value
        +of(value String) SkuCode
        +getValue() String
    }

    class Priority {
        -int level
        -String name
        +of(level int, name String) Priority
        +getLevel() int
        +isHigherThan(other Priority) boolean
    }

    %% Relationships
    Wave ||--|| WaveId : "identified by"
    Wave ||--|| WaveStatus : "has status"
    Wave ||--o{ OrderId : "contains orders"
    
    Wave ..> WaveCreatedEvent : "publishes"
    Wave ..> WaveReleasedEvent : "publishes"
    Wave ..> WaveClosedEvent : "publishes"
    Wave ..> WaveCancelledEvent : "publishes"
    Wave ..> WaveProgressUpdatedEvent : "publishes"
    
    WavePlanningStrategy <|-- TimeBasedWavePlanning : "implements"
    WavePlanningStrategy <|-- CarrierBasedWavePlanning : "implements"
    WavePlanningStrategy <|-- ZoneBasedWavePlanning : "implements"
    WavePlanningStrategy <|-- PriorityBasedWavePlanning : "implements"
    
    WavePlanningService ..> WavePlanningEngine : "uses"
    WavePlanningService ..> WaveRepository : "uses"
    WavePlanningEngine ..> WavePlanningStrategy : "uses"
    WavePlanningEngine ..> WaveOptimizer : "uses"
    
    WaveExecutionService ..> WaveRepository : "uses"
    WaveApplicationService ..> WavePlanningService : "uses"
    WaveApplicationService ..> WaveExecutionService : "uses"
    
    WaveQueryService ..> WaveRepository : "uses"
```

## Key Domain Concepts

### Wave Aggregate
- **Root Entity**: `Wave` - Manages order grouping and batch processing
- **Identity**: `WaveId` - Strongly typed UUID-based identifier
- **Lifecycle**: PLANNED → RELEASED → IN_PROGRESS → COMPLETED → CLOSED
- **Business Rules**:
  - Must contain at least one order
  - Orders cannot belong to multiple active waves
  - Release triggers downstream picking processes
  - Closure requires all orders to be completed

### Wave Planning Strategies
- **Time-Based**: Group orders by cutoff times and delivery windows
- **Carrier-Based**: Group orders by shipping carrier and service level
- **Zone-Based**: Group orders by warehouse zones for picking efficiency
- **Priority-Based**: Group orders by priority levels and urgency
- **Capacity-Based**: Group orders by resource capacity and constraints
- **Item Affinity**: Group orders with similar items to minimize travel

### Domain Events
- **WaveCreatedEvent**: Published when wave is created and planned
- **WaveReleasedEvent**: Published when wave is released for execution
- **WaveProgressUpdatedEvent**: Published for progress tracking
- **WaveClosedEvent**: Published when wave execution is completed
- **WaveCancelledEvent**: Published when wave is cancelled

### Domain Services
- **WavePlanningService**: Orchestrates wave creation and optimization
- **WaveExecutionService**: Manages wave execution and progress tracking
- **WavePlanningEngine**: Applies planning strategies and optimization algorithms

## Business Rules

1. **Wave Creation Rules**:
   - Wave must contain at least one order
   - Orders cannot be assigned to multiple active waves
   - Wave planning criteria determine order eligibility
   - Maximum wave size limits prevent overloading

2. **Wave Release Rules**:
   - Only PLANNED waves can be released
   - Release triggers pick list creation and work assignment
   - Released waves cannot be modified
   - Release timing affects warehouse efficiency

3. **Wave Execution Rules**:
   - Progress tracking monitors order completion
   - Exception handling manages wave disruptions
   - Wave completion requires all orders to be fulfilled
   - Performance metrics collected for optimization

4. **Wave Optimization Rules**:
   - Minimize picker travel distance
   - Maximize resource utilization
   - Balance workload across teams
   - Consider item affinity and zone efficiency

## Wave Planning Process

1. **Order Analysis**:
   - Evaluate pending orders for wave eligibility
   - Apply business rules and constraints
   - Calculate order characteristics and requirements

2. **Strategy Application**:
   - Apply selected planning strategies
   - Group orders based on strategy criteria
   - Validate wave composition and constraints

3. **Optimization**:
   - Optimize wave size and composition
   - Balance workload and resource requirements
   - Minimize travel distance and picking time

4. **Validation**:
   - Validate wave against business rules
   - Check resource availability and constraints
   - Generate warnings and recommendations

5. **Wave Creation**:
   - Create wave with selected orders
   - Set planning parameters and metadata
   - Schedule wave for execution

## Wave Execution Lifecycle

### Planning Phase
- **Order Selection**: Identify eligible orders for wave inclusion
- **Strategy Application**: Apply planning algorithms and rules
- **Optimization**: Optimize wave composition for efficiency
- **Validation**: Ensure wave meets all business constraints

### Release Phase
- **Work Generation**: Create pick lists and work assignments
- **Resource Allocation**: Assign pickers and equipment
- **Priority Setting**: Establish work priorities and sequences
- **Notification**: Notify warehouse teams of wave release

### Execution Phase
- **Progress Monitoring**: Track order and pick completion
- **Exception Handling**: Manage disruptions and issues
- **Performance Tracking**: Collect metrics and KPIs
- **Dynamic Adjustment**: Adapt to changing conditions

### Completion Phase
- **Validation**: Verify all orders are completed
- **Metrics Collection**: Gather performance data
- **Analysis**: Analyze wave efficiency and outcomes
- **Learning**: Update planning algorithms based on results

## Integration Points

- **Order Management**: Order lifecycle and fulfillment status
- **Pick List Generation**: Automatic pick list creation from waves
- **Work Management**: Work assignment and execution tracking
- **Inventory Management**: Inventory allocation and reservation
- **Shipping**: Carrier integration and shipment planning
- **Performance Analytics**: Wave metrics and optimization insights

## Planning Strategies

### Time-Based Planning
- **Cutoff Times**: Group orders by shipping cutoff times
- **Delivery Windows**: Align waves with delivery schedules
- **Peak Optimization**: Balance waves across peak periods
- **Schedule Adherence**: Maintain consistent wave timing

### Carrier-Based Planning
- **Service Levels**: Group by shipping service requirements
- **Carrier Preferences**: Optimize for specific carrier efficiency
- **Route Optimization**: Align with carrier pickup schedules
- **Cost Optimization**: Balance service and shipping costs

### Zone-Based Planning
- **Pick Path Optimization**: Minimize travel within zones
- **Zone Affinity**: Group orders with similar zone requirements
- **Resource Allocation**: Balance workload across zones
- **Congestion Management**: Avoid zone overcrowding

### Priority-Based Planning
- **Urgency Levels**: Prioritize high-priority orders
- **SLA Compliance**: Ensure service level agreements
- **Customer Tiers**: Apply customer-specific priorities
- **Exception Handling**: Fast-track critical orders

## Performance Metrics

### Efficiency Metrics
- **Orders per Wave**: Average and optimal wave sizes
- **Pick Rate**: Items picked per hour per wave
- **Travel Distance**: Picker travel optimization
- **Resource Utilization**: Equipment and personnel efficiency

### Quality Metrics
- **Accuracy Rate**: Picking and fulfillment accuracy
- **Damage Rate**: Product damage during fulfillment
- **Exception Rate**: Disruptions and issues per wave
- **Customer Satisfaction**: Order fulfillment quality

### Timing Metrics
- **Cycle Time**: Wave planning to completion time
- **Release Latency**: Time from planning to release
- **Execution Duration**: Wave execution time
- **SLA Adherence**: On-time delivery performance

## Query Patterns

- **By Status**: Find waves in specific execution states
- **By Carrier**: Find waves for specific carriers
- **By Date Range**: Find waves within time periods
- **By Order**: Find wave containing specific order
- **Active Waves**: Find currently executing waves
- **Overdue Waves**: Find waves exceeding planned duration
- **Performance Analytics**: Wave efficiency and metrics

## Event-Driven Workflows

1. **Wave Planning Workflow**: OrdersAvailable → WavePlanned → WaveOptimized → WaveValidated → WaveCreated
2. **Wave Execution Workflow**: WaveReleased → WorkGenerated → PickingStarted → ProgressTracked → WaveCompleted → WaveClosed
3. **Exception Handling Workflow**: ExceptionDetected → WavePaused → IssueResolved → WaveResumed → ExecutionContinued
4. **Optimization Workflow**: WaveAnalyzed → OptimizationApplied → PerformanceImproved → LearningCaptured → StrategyUpdated