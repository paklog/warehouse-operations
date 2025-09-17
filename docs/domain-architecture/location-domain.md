# Location Domain - Domain Model

## Overview
The Location domain manages location directives, strategies, and constraints for warehouse operations. It provides intelligent location selection for work tasks through configurable strategies and constraint evaluation, optimizing warehouse efficiency and resource utilization.

## Domain Model Diagram

```mermaid
classDiagram
    %% Location Directive Aggregate Root
    class LocationDirective {
        -LocationDirectiveId id
        -String name
        -String description
        -WorkType workType
        -LocationStrategy strategy
        -List~LocationConstraint~ constraints
        -int priority
        -boolean active
        -Instant createdAt
        -Instant lastModifiedAt
        -String createdBy
        -int version
        +addConstraint(constraint LocationConstraint) void
        +removeConstraint(constraint LocationConstraint) void
        +updateStrategy(strategy LocationStrategy) void
        +updatePriority(priority int) void
        +activate() void
        +deactivate() void
        +isApplicableFor(workType WorkType) boolean
        +satisfiesConstraints(context LocationContext) boolean
        +selectLocation(query LocationQuery) BinLocation
        +evaluateForLocation(query LocationQuery, location BinLocation) LocationDirectiveResult
    }

    %% Location Constraint Entity
    class LocationConstraint {
        -LocationConstraintType type
        -String parameter
        -String value
        -boolean enabled
        +evaluate(context LocationContext) boolean
        +toString() String
        +isEnabled() boolean
    }

    %% Value Objects
    class LocationDirectiveId {
        -UUID value
        +generate() LocationDirectiveId
        +of(id String) LocationDirectiveId
        +getValue() UUID
    }

    class LocationQuery {
        -WorkType workType
        -SkuCode item
        -Quantity requestedQuantity
        -BinLocation sourceLocation
        -List~BinLocation~ candidateLocations
        -Map~String,Object~ parameters
        +getWorkType() WorkType
        +createContextForLocation(location BinLocation) LocationContext
        +getCandidateLocations() List~BinLocation~
        +withParameters(parameters Map~String,Object~) LocationQuery
    }

    class LocationContext {
        -BinLocation location
        -SkuCode item
        -Quantity availableQuantity
        -Quantity requestedQuantity
        -WorkType workType
        -String zone
        -String aisle
        -String level
        -Map~String,Object~ attributes
        +getLocation() BinLocation
        +getZone() String
        +hasAvailableCapacity() boolean
        +getDistanceTo(other BinLocation) double
    }

    class LocationDirectiveResult {
        -ResultType resultType
        -double score
        -List~String~ violations
        -String reason
        +isSuitable() boolean
        +hasConstraintViolations() boolean
        +getScore() double
        +getViolations() List~String~
        +static suitable(score double) LocationDirectiveResult
        +static notApplicable(reason String) LocationDirectiveResult
        +static constraintViolation(violations List~String~) LocationDirectiveResult
    }

    %% Enums
    class LocationStrategy {
        <<enumeration>>
        FIXED
        NEAREST_EMPTY
        BULK_LOCATION
        FAST_MOVING
        ZONE_BASED
        CAPACITY_OPTIMIZED
        FIFO
        LIFO
        LOWEST_LEVEL
        HIGHEST_LEVEL
        RANDOM
        +requiresFixedMapping() boolean
        +requiresInventoryData() boolean
        +requiresZoneConfiguration() boolean
    }

    class LocationConstraintType {
        <<enumeration>>
        ZONE_RESTRICTION
        AISLE_RESTRICTION
        LEVEL_RESTRICTION
        CAPACITY_MINIMUM
        CAPACITY_MAXIMUM
        INVENTORY_AVAILABLE
        ITEM_COMPATIBILITY
        EQUIPMENT_REQUIRED
        TEMPERATURE_RANGE
        SECURITY_LEVEL
        HAZMAT_COMPATIBLE
        BULK_STORAGE_ONLY
    }

    class WorkType {
        <<enumeration>>
        PUTAWAY
        PICK
        REPLENISHMENT
        CYCLE_COUNT
        MOVE
        PACK
        CONSOLIDATION
        CROSS_DOCK
    }

    %% Location Selectors (Strategy Pattern)
    class LocationSelector {
        <<interface>>
        +selectOptimalLocation(query LocationQuery, directive LocationDirective) BinLocation
        +evaluateLocation(query LocationQuery, location BinLocation) double
    }

    class FixedLocationSelector {
        +selectOptimalLocation(query LocationQuery, directive LocationDirective) BinLocation
        -getFixedLocation(item SkuCode) BinLocation
    }

    class NearestEmptyLocationSelector {
        +selectOptimalLocation(query LocationQuery, directive LocationDirective) BinLocation
        -calculateDistanceScore(fromLocation BinLocation, toLocation BinLocation) double
        -findNearestEmptyLocation(availableLocations List~BinLocation~, sourceLocation BinLocation) BinLocation
    }

    class CapacityOptimizedLocationSelector {
        +selectOptimalLocation(query LocationQuery, directive LocationDirective) BinLocation
        -calculateCapacityUtilization(location BinLocation, requestedQuantity Quantity) double
        -findOptimalCapacityLocation(candidates List~BinLocation~, quantity Quantity) BinLocation
    }

    class ZoneBasedLocationSelector {
        +selectOptimalLocation(query LocationQuery, directive LocationDirective) BinLocation
        -getPreferredZone(item SkuCode) String
        -findLocationInZone(zone String, candidates List~BinLocation~) BinLocation
    }

    class FifoLocationSelector {
        +selectOptimalLocation(query LocationQuery, directive LocationDirective) BinLocation
        -findOldestInventoryLocation(item SkuCode, locations List~BinLocation~) BinLocation
    }

    class LifoLocationSelector {
        +selectOptimalLocation(query LocationQuery, directive LocationDirective) BinLocation
        -findNewestInventoryLocation(item SkuCode, locations List~BinLocation~) BinLocation
    }

    %% Domain Service
    class LocationDirectiveService {
        -LocationDirectiveRepository locationDirectiveRepository
        +selectOptimalLocation(query LocationQuery) BinLocation
        +evaluateLocation(query LocationQuery, location BinLocation) LocationEvaluationResult
        +findBestLocations(query LocationQuery, maxResults int) List~BinLocation~
        +canSatisfyQuery(query LocationQuery) boolean
        +getApplicableDirectives(workType WorkType) List~LocationDirective~
        +validateDirective(directive LocationDirective) LocationDirectiveValidationResult
        +createDefaultDirective(workType WorkType, strategy LocationStrategy) LocationDirective
    }

    %% Domain Service Results
    class LocationEvaluationResult {
        -boolean suitable
        -double score
        -int directiveCount
        -List~String~ violations
        +isSuitable() boolean
        +getScore() double
        +static suitable(score double, count int) LocationEvaluationResult
        +static unsuitable(violations List~String~) LocationEvaluationResult
        +static noDirectivesAvailable() LocationEvaluationResult
    }

    class LocationDirectiveValidationResult {
        -boolean valid
        -List~String~ issues
        +isValid() boolean
        +getIssues() List~String~
    }

    %% Application Services
    class LocationDirectiveApplicationService {
        -LocationDirectiveRepository directiveRepository
        -LocationDirectiveService directiveService
        +createLocationDirective(command CreateLocationDirectiveCommand) LocationDirectiveId
        +updateLocationDirective(command UpdateLocationDirectiveCommand) void
        +activateDirective(directiveId LocationDirectiveId) void
        +deactivateDirective(directiveId LocationDirectiveId) void
        +findOptimalLocation(query LocationQuery) BinLocation
    }

    class LocationQueryService {
        -LocationDirectiveRepository directiveRepository
        +findByWorkType(workType WorkType) List~LocationDirective~
        +findByStrategy(strategy LocationStrategy) List~LocationDirective~
        +findActiveDirectives() List~LocationDirective~
        +findByPriority(priority int) List~LocationDirective~
    }

    %% Repository
    class LocationDirectiveRepository {
        <<interface>>
        +save(directive LocationDirective) void
        +findById(id LocationDirectiveId) LocationDirective
        +findByWorkType(workType WorkType) List~LocationDirective~
        +findByWorkTypeAndActive(workType WorkType, active boolean) List~LocationDirective~
        +findByStrategy(strategy LocationStrategy) List~LocationDirective~
        +findActiveDirectives() List~LocationDirective~
        +findByPriority(priority int) List~LocationDirective~
        +delete(directive LocationDirective) void
    }

    %% Shared Value Objects
    class BinLocation {
        -String aisle
        -String rack
        -String level
        +of(aisle String, rack String, level String) BinLocation
        +toString() String
        +getZone() String
        +calculateDistance(other BinLocation) double
    }

    class Location {
        -String zone
        -String aisle
        -String shelf
        +getZone() String
        +getAisle() String
        +getShelf() String
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
    }

    %% Domain Events
    class LocationDirectiveCreatedEvent {
        -LocationDirectiveId directiveId
        -String name
        -WorkType workType
        -LocationStrategy strategy
        -Instant createdAt
        +LocationDirectiveCreatedEvent(LocationDirectiveId, String, WorkType, LocationStrategy, Instant)
    }

    class LocationDirectiveUpdatedEvent {
        -LocationDirectiveId directiveId
        -String fieldName
        -Object oldValue
        -Object newValue
        -Instant updatedAt
        +LocationDirectiveUpdatedEvent(LocationDirectiveId, String, Object, Object, Instant)
    }

    class LocationDirectiveActivatedEvent {
        -LocationDirectiveId directiveId
        -Instant activatedAt
        +LocationDirectiveActivatedEvent(LocationDirectiveId, Instant)
    }

    class LocationDirectiveDeactivatedEvent {
        -LocationDirectiveId directiveId
        -Instant deactivatedAt
        +LocationDirectiveDeactivatedEvent(LocationDirectiveId, Instant)
    }

    class LocationSelectedEvent {
        -LocationQuery query
        -BinLocation selectedLocation
        -LocationDirectiveId usedDirectiveId
        -double score
        -Instant selectedAt
        +LocationSelectedEvent(LocationQuery, BinLocation, LocationDirectiveId, double, Instant)
    }

    %% Relationships
    LocationDirective ||--|| LocationDirectiveId : "identified by"
    LocationDirective ||--o{ LocationConstraint : "contains"
    LocationDirective ||--|| LocationStrategy : "uses strategy"
    LocationDirective ||--|| WorkType : "applies to"
    
    LocationConstraint ||--|| LocationConstraintType : "has type"
    
    LocationQuery ||--|| WorkType : "for work type"
    LocationQuery ||--|| SkuCode : "for item"
    LocationQuery ||--o{ BinLocation : "considers candidates"
    
    LocationContext ||--|| BinLocation : "evaluates"
    LocationContext ||--|| WorkType : "for work"
    
    LocationDirectiveResult ||--|| ResultType : "has type"
    
    LocationSelector <|-- FixedLocationSelector : "implements"
    LocationSelector <|-- NearestEmptyLocationSelector : "implements"
    LocationSelector <|-- CapacityOptimizedLocationSelector : "implements"
    LocationSelector <|-- ZoneBasedLocationSelector : "implements"
    LocationSelector <|-- FifoLocationSelector : "implements"
    LocationSelector <|-- LifoLocationSelector : "implements"
    
    LocationDirective ..> LocationSelector : "uses"
    LocationDirective ..> LocationDirectiveCreatedEvent : "publishes"
    LocationDirective ..> LocationDirectiveUpdatedEvent : "publishes"
    
    LocationDirectiveService ..> LocationDirectiveRepository : "uses"
    LocationDirectiveService ..> LocationEvaluationResult : "produces"
    LocationDirectiveService ..> LocationSelectedEvent : "publishes"
    
    LocationDirectiveApplicationService ..> LocationDirectiveService : "uses"
    LocationDirectiveApplicationService ..> LocationDirectiveRepository : "uses"
```

## Key Domain Concepts

### Location Directive Aggregate
- **Root Entity**: `LocationDirective` - Manages location selection rules and strategies
- **Identity**: `LocationDirectiveId` - Strongly typed UUID-based identifier
- **Lifecycle**: Created → Active/Inactive → Updated → Deleted
- **Business Rules**:
  - Priority determines directive evaluation order
  - Constraints must be satisfied for location selection
  - Strategy defines location selection algorithm
  - Work type determines applicability scope

### Location Constraint Entity
- **Purpose**: Defines conditions that must be met for location selection
- **Types**: Zone, capacity, inventory, compatibility, equipment requirements
- **Evaluation**: Boolean logic against location context
- **Configuration**: Parameter-based constraint definitions

### Location Selection Strategies
- **Fixed**: Predefined location assignments
- **Nearest Empty**: Distance-based optimization
- **Capacity Optimized**: Space utilization efficiency
- **Zone Based**: Zone preference and segregation
- **FIFO/LIFO**: Inventory rotation strategies
- **Level Based**: Vertical optimization (lowest/highest)

### Domain Services
- **LocationDirectiveService**: Orchestrates location selection process
- **Strategy Pattern**: Pluggable location selection algorithms
- **Constraint Engine**: Evaluates location suitability

## Business Rules

1. **Directive Priority**:
   - Lower priority numbers execute first (1 = highest priority)
   - Directives evaluated in priority order until suitable location found
   - Inactive directives are skipped during evaluation

2. **Constraint Evaluation**:
   - All constraints must be satisfied for location approval
   - Constraint violations prevent location selection
   - Constraints are configurable per directive

3. **Strategy Selection**:
   - Each directive uses exactly one strategy
   - Strategy determines location selection algorithm
   - Strategy compatibility validated against constraints

4. **Work Type Mapping**:
   - Directives apply to specific work types
   - Multiple directives can target same work type
   - Work type determines directive applicability

## Location Selection Process

1. **Query Evaluation**:
   - Identify applicable directives by work type
   - Order directives by priority
   - Filter active directives only

2. **Constraint Validation**:
   - Evaluate each constraint against location context
   - Reject locations that violate any constraint
   - Log constraint violations for troubleshooting

3. **Strategy Execution**:
   - Apply strategy-specific selection algorithm
   - Calculate location scores and rankings
   - Return optimal location based on strategy

4. **Result Validation**:
   - Verify selected location meets all requirements
   - Log selection rationale and score
   - Handle selection failures gracefully

## Integration Points

- **Work Domain**: Location directives support work task assignment
- **Inventory Domain**: Location selection considers inventory levels
- **Pick/Put Strategies**: Optimize pick path and put-away efficiency
- **Zone Management**: Enforce zone-based segregation rules
- **Capacity Planning**: Consider location capacity constraints
- **Mobile Operations**: Support real-time location suggestions

## Query Patterns

- **By Work Type**: Find directives applicable to specific work types
- **By Strategy**: Find directives using specific strategies
- **By Priority**: Find directives within priority ranges
- **Active Only**: Find currently active directives
- **Validation**: Check directive configuration validity

## Constraint Types

1. **Zone Constraints**:
   - Zone restriction (whitelist/blacklist)
   - Zone preference scoring
   - Cross-zone movement rules

2. **Capacity Constraints**:
   - Minimum available capacity
   - Maximum utilization thresholds
   - Weight and volume limits

3. **Item Constraints**:
   - Item compatibility rules
   - Hazmat segregation requirements
   - Temperature range requirements

4. **Equipment Constraints**:
   - Required equipment availability
   - Equipment compatibility checks
   - Access restrictions

## Strategy Patterns

1. **Distance-Based**:
   - Nearest empty location
   - Shortest travel path
   - Zone proximity optimization

2. **Capacity-Based**:
   - Optimal space utilization
   - Consolidation preferences
   - Fragmentation minimization

3. **Inventory-Based**:
   - FIFO/LIFO rotation
   - Batch segregation
   - Expiry date management

4. **Zone-Based**:
   - Fast-moving item zones
   - Bulk storage areas
   - Special handling zones

## Event-Driven Workflows

1. **Directive Management**: DirectiveCreated → Validated → Activated → LocationSelectionEnabled
2. **Location Selection**: WorkRequested → DirectivesEvaluated → LocationSelected → WorkAssigned
3. **Constraint Updates**: ConstraintAdded → DirectiveValidated → SelectionBehaviorChanged
4. **Strategy Changes**: StrategyUpdated → DirectiveReconfigured → SelectionAlgorithmChanged