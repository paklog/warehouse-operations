# Domain Model

## 1. OVERVIEW

### Purpose and Primary Functionality
The domain model represents the core business concepts and rules of warehouse operations. It implements Domain-Driven Design (DDD) principles with rich domain objects that encapsulate business logic and maintain consistency.

### When to Use This Domain Model vs. Alternatives
Use this domain model when you need:
- **Complex business rules** that change frequently
- **Strong consistency** within aggregate boundaries
- **Event-driven architecture** with domain events
- **Rich behavioral models** rather than anemic data structures

Consider alternatives for:
- Simple CRUD operations (use data transfer objects)
- Read-only queries (use query projections)
- External system integration (use integration models)

### Architectural Context
The domain layer is the heart of the application, implementing the Ubiquitous Language and business rules. It's isolated from infrastructure concerns and can be tested independently.

## 2. TECHNICAL SPECIFICATION

### Domain Structure
```
domain/
├── shared/                 # Shared value objects and events
│   ├── AggregateRoot      # Base class for aggregates
│   ├── DomainEvent        # Base domain event interface
│   ├── ValueObjects       # SkuCode, Quantity, BinLocation, etc.
│   └── SharedEvents       # Cross-domain events
├── picklist/              # Pick list bounded context
│   ├── PickList           # Aggregate root
│   ├── PickInstruction    # Entity
│   ├── PickListRepository # Repository interface
│   └── Events             # Domain events
├── packaging/             # Package bounded context
│   ├── Package            # Aggregate root
│   ├── PackedItem         # Value object
│   └── PackageRepository  # Repository interface
├── licenseplate/          # License plate bounded context
├── quality/              # Quality control bounded context
├── wave/                 # Wave management bounded context
└── work/                 # Work management bounded context
```

### Aggregate Design Principles
1. **Consistency Boundary**: Each aggregate maintains its own invariants
2. **Single Responsibility**: One aggregate per business concept
3. **Reference by ID**: Aggregates reference others by ID, not direct references
4. **Transaction Boundary**: One aggregate per transaction

## Core Domain Entities

### PickList Aggregate
The `PickList` aggregate manages warehouse pick operations and picker assignments.

**Aggregate Root**: `PickList`
```java
public class PickList extends AggregateRoot {
    private final PickListId id;
    private PickListStatus status;
    private final List<PickInstruction> instructions;
    private String pickerId;
    private final OrderId orderId;
    private Instant assignedAt;
    private Instant createdAt;
    private Instant completedAt;
}
```

**Key Business Rules**:
- Pick list can only be assigned to one picker at a time
- Items can only be picked if pick list is assigned
- All instructions must be completed to mark pick list as complete
- Status transitions: `PENDING` → `ASSIGNED` → `COMPLETED`

**Domain Events**:
- `PickListAssignedEvent`: When picker is assigned
- `ItemPickedEvent`: When item is picked
- `PickListCompletedEvent`: When all items are picked

**Value Objects**:
- `PickListId`: Unique identifier
- `PickListStatus`: Status enumeration
- `PickInstruction`: Pick instruction details

### Package Aggregate
The `Package` aggregate manages package creation and confirmation for shipping.

**Aggregate Root**: `Package`
```java
public class Package {
    private final UUID packageId;
    private PackageStatus status;
    private final List<PackedItem> packedItems;
}
```

**Key Business Rules**:
- Package must contain at least one packed item
- Cannot add duplicate SKUs to same package
- Package can only be confirmed once
- Status transitions: `PENDING` → `CONFIRMED` → `SHIPPED`

**Domain Events**:
- `PackageCreatedEvent`: When package is created
- `PackageConfirmedEvent`: When package is confirmed
- `ItemPackedEvent`: When item is added to package

### LicensePlate Aggregate
The `LicensePlate` aggregate tracks containers and pallets throughout the warehouse.

**Aggregate Root**: `LicensePlate`
```java
public class LicensePlate extends AggregateRoot {
    private final LicensePlateId id;
    private LicensePlateType type;
    private Location currentLocation;
    private final List<InventoryItem> inventory;
    private LicensePlateStatus status;
}
```

**Key Business Rules**:
- License plates can contain multiple inventory items
- Location changes must be tracked with events
- Inventory additions/removals must maintain accurate counts
- Nesting relationships for pallets and containers

### Quality Aggregates
Quality control is managed through two main aggregates:

#### QualityInspection
```java
public class QualityInspection extends AggregateRoot {
    private final QualityInspectionId id;
    private final LicensePlateId licensePlateId;
    private QualityInspectionStatus status;
    private final List<QualityCheck> checks;
    private String inspectorId;
}
```

#### QualityHold
```java
public class QualityHold extends AggregateRoot {
    private final QualityHoldId id;
    private final LicensePlateId licensePlateId;
    private QualityHoldReason reason;
    private QualityHoldStatus status;
    private String holdDetails;
}
```

### Wave Aggregate
The `Wave` aggregate manages batch processing of orders.

**Aggregate Root**: `Wave`
```java
public class Wave extends AggregateRoot {
    private final WaveId id;
    private WaveStatus status;
    private final List<OrderId> orders;
    private WaveStrategy strategy;
    private Instant plannedReleaseTime;
}
```

## Shared Value Objects

### SkuCode
Represents a Stock Keeping Unit identifier.
```java
public class SkuCode {
    private final String value;

    public static SkuCode of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU code cannot be null or empty");
        }
        return new SkuCode(value.trim().toUpperCase());
    }
}
```

### Quantity
Represents item quantities with validation.
```java
public class Quantity {
    private final int value;

    public static Quantity of(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return new Quantity(value);
    }
}
```

### BinLocation
Represents warehouse storage locations.
```java
public class BinLocation {
    private final String value;

    public static BinLocation of(String value) {
        if (value == null || !isValidLocationFormat(value)) {
            throw new IllegalArgumentException("Invalid bin location format");
        }
        return new BinLocation(value);
    }

    private static boolean isValidLocationFormat(String value) {
        // Format: Zone-Aisle-Shelf (e.g., A1-B2-C3)
        return value.matches("^[A-Z]\\d+-[A-Z]\\d+-[A-Z]\\d+$");
    }
}
```

### OrderId
Represents order identifiers.
```java
public class OrderId {
    private final String value;

    public static OrderId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        return new OrderId(value.trim());
    }

    public static OrderId generate() {
        return new OrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}
```

## 3. IMPLEMENTATION EXAMPLES

### Creating and Managing Pick Lists
```java
// Create new pick list
PickList pickList = new PickList(OrderId.of("ORD-123"));

// Add pick instructions
pickList.addInstruction(new PickInstruction(
    SkuCode.of("SKU-001"),
    Quantity.of(5),
    BinLocation.of("A1-B2-C3")
));

pickList.addInstruction(new PickInstruction(
    SkuCode.of("SKU-002"),
    Quantity.of(3),
    BinLocation.of("A2-B1-C4")
));

// Assign to picker
pickList.assignToPicker("PICKER-001");

// Process picks
pickList.pickItem(
    SkuCode.of("SKU-001"),
    Quantity.of(5),
    BinLocation.of("A1-B2-C3")
);

pickList.pickItem(
    SkuCode.of("SKU-002"),
    Quantity.of(3),
    BinLocation.of("A2-B1-C4")
);

// Pick list is now completed
assert pickList.isComplete();

// Retrieve and publish domain events
List<DomainEvent> events = pickList.getDomainEvents();
eventPublisher.publishAll(events);
pickList.clearDomainEvents();
```

### Package Management
```java
// Create package from order
FulfillmentOrder order = new FulfillmentOrder(
    OrderId.generate(),
    "STANDARD",
    shippingAddress,
    Arrays.asList(
        new OrderItem(SkuCode.of("SKU-001"), Quantity.of(2)),
        new OrderItem(SkuCode.of("SKU-002"), Quantity.of(1))
    )
);

Package package = new Package(order, pickList);

// Add items to package
package.addPackedItem(new PackedItem(SkuCode.of("SKU-001"), 2));
package.addPackedItem(new PackedItem(SkuCode.of("SKU-002"), 1));

// Confirm package for shipping
package.confirmPacking();

assert package.getStatus() == PackageStatus.CONFIRMED;
assert package.getTotalQuantity() == 3;
```

### License Plate Operations
```java
// Create license plate
LicensePlate licensePlate = new LicensePlate(
    LicensePlateId.generate(),
    LicensePlateType.PALLET
);

// Add inventory
licensePlate.addInventory(new InventoryItem(
    SkuCode.of("SKU-001"),
    Quantity.of(100),
    "LOT-2024-001"
));

// Move to location
licensePlate.moveTo(Location.of("ZONE-A", "AISLE-01", "LEVEL-01"));

// Verify events
List<DomainEvent> events = licensePlate.getDomainEvents();
assertThat(events).hasSize(2); // InventoryAddedEvent, LocationChangedEvent
```

### Quality Control Workflow
```java
// Create quality inspection
QualityInspection inspection = new QualityInspection(
    QualityInspectionId.generate(),
    licensePlateId,
    "INSPECTOR-001"
);

// Add quality checks
inspection.addCheck(new QualityCheck(
    QualityCheckType.VISUAL_INSPECTION,
    "Check for damage"
));

inspection.addCheck(new QualityCheck(
    QualityCheckType.COUNT_VERIFICATION,
    "Verify item count"
));

// Complete inspection
inspection.completeCheck(QualityCheckType.VISUAL_INSPECTION, QualityResult.PASS);
inspection.completeCheck(QualityCheckType.COUNT_VERIFICATION, QualityResult.PASS);

// If all checks pass, inspection is complete
if (inspection.isComplete()) {
    // Release license plate
    licensePlate.releaseFromQualityHold();
} else {
    // Create quality hold
    QualityHold hold = new QualityHold(
        QualityHoldId.generate(),
        licensePlateId,
        QualityHoldReason.FAILED_INSPECTION,
        "Visual damage detected"
    );
}
```

### Domain Service Example
```java
@Component
public class PickListDomainService {

    public PickList createOptimizedPickList(List<OrderItem> items, WaveStrategy strategy) {
        // Business logic for creating optimized pick lists
        PickList pickList = new PickList(OrderId.generate());

        // Sort items by location for optimal picking route
        List<OrderItem> optimizedItems = strategy.optimizePickingRoute(items);

        for (OrderItem item : optimizedItems) {
            BinLocation location = locationService.findOptimalLocation(item.getSkuCode());
            pickList.addInstruction(new PickInstruction(
                item.getSkuCode(),
                item.getQuantity(),
                location
            ));
        }

        return pickList;
    }

    public void assignPickListToBestPicker(PickList pickList) {
        String bestPicker = pickerAssignmentStrategy.findBestPicker(
            pickList.getInstructions()
        );
        pickList.assignToPicker(bestPicker);
    }
}
```

## 4. TROUBLESHOOTING

### Common Domain Model Issues

#### 1. Aggregate Boundary Violations
**Problem**: Trying to modify multiple aggregates in one operation
```java
// ❌ Wrong - violates aggregate boundaries
public void processCompleteOrder(OrderId orderId) {
    PickList pickList = pickListRepo.findByOrderId(orderId);
    Package package = packageRepo.findByOrderId(orderId);

    pickList.complete();  // Modifying aggregate 1
    package.confirm();    // Modifying aggregate 2

    // Both saved in same transaction - violates DDD principles
}
```

**Solution**: Use domain events for cross-aggregate operations
```java
// ✅ Correct - use events for cross-aggregate communication
@EventListener
public void on(PickListCompletedEvent event) {
    Package package = packageRepo.findByOrderId(event.getOrderId());
    if (package != null) {
        package.markReadyForPacking();
        packageRepo.save(package);
    }
}
```

#### 2. Anemic Domain Model
**Problem**: Domain objects with only getters/setters, no behavior
```java
// ❌ Wrong - anemic model
public class PickList {
    private String status;
    private List<PickInstruction> instructions;

    // Only getters and setters, no business logic
    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }
}
```

**Solution**: Move business logic into domain objects
```java
// ✅ Correct - rich domain model
public class PickList {
    public void assignToPicker(String pickerId) {
        if (this.status != PickListStatus.PENDING) {
            throw new IllegalStateException("Pick list must be pending to assign");
        }
        this.pickerId = pickerId;
        this.status = PickListStatus.ASSIGNED;
        this.assignedAt = Instant.now();
        registerEvent(new PickListAssignedEvent(this.id, pickerId));
    }
}
```

#### 3. Value Object Validation Issues
**Problem**: Missing or insufficient validation in value objects
```java
// ❌ Wrong - no validation
public class Quantity {
    private final int value;

    public Quantity(int value) {
        this.value = value; // No validation!
    }
}
```

**Solution**: Implement proper validation
```java
// ✅ Correct - proper validation
public class Quantity {
    private final int value;

    private Quantity(int value) {
        this.value = value;
    }

    public static Quantity of(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got: " + value);
        }
        if (value > 999999) {
            throw new IllegalArgumentException("Quantity too large, max: 999999, got: " + value);
        }
        return new Quantity(value);
    }
}
```

#### 4. Domain Event Memory Leaks
**Problem**: Domain events accumulating without being cleared
```java
// ❌ Wrong - events never cleared
public void processPickList(PickListId id) {
    PickList pickList = repository.findById(id);
    pickList.complete();
    repository.save(pickList);
    // Events accumulate in memory!
}
```

**Solution**: Always clear events after publishing
```java
// ✅ Correct - clear events after publishing
public void processPickList(PickListId id) {
    PickList pickList = repository.findById(id);
    pickList.complete();
    repository.save(pickList);

    eventPublisher.publishAll(pickList.getDomainEvents());
    pickList.clearDomainEvents(); // Important!
}
```

### Testing Domain Objects

#### Unit Testing Aggregates
```java
class PickListTest {

    @Test
    void shouldAssignPickerWhenPending() {
        // Given
        PickList pickList = new PickList(OrderId.of("ORD-123"));

        // When
        pickList.assignToPicker("PICKER-001");

        // Then
        assertThat(pickList.getStatus()).isEqualTo(PickListStatus.ASSIGNED);
        assertThat(pickList.getPickerId()).isEqualTo("PICKER-001");
        assertThat(pickList.getDomainEvents()).hasSize(1);
        assertThat(pickList.getDomainEvents().get(0))
            .isInstanceOf(PickListAssignedEvent.class);
    }

    @Test
    void shouldThrowExceptionWhenAssigningAlreadyAssignedPickList() {
        // Given
        PickList pickList = new PickList(OrderId.of("ORD-123"));
        pickList.assignToPicker("PICKER-001");

        // When & Then
        assertThatThrownBy(() -> pickList.assignToPicker("PICKER-002"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Pick list is already assigned");
    }
}
```

#### Testing Value Objects
```java
class QuantityTest {

    @Test
    void shouldCreateValidQuantity() {
        Quantity quantity = Quantity.of(5);
        assertThat(quantity.getValue()).isEqualTo(5);
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThatThrownBy(() -> Quantity.of(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Quantity must be positive, got: -1");
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThatThrownBy(() -> Quantity.of(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Quantity must be positive, got: 0");
    }
}
```

## 5. RELATED COMPONENTS

### Dependencies
- **Java 17**: Records, pattern matching, enhanced switch expressions
- **Spring Framework**: Dependency injection, transaction management
- **Jakarta Validation**: Bean validation annotations
- **SLF4J**: Logging abstraction

### Components Commonly Used Alongside
- **Application Services**: Orchestrate domain operations
- **Repository Implementations**: Persist aggregate state
- **Event Publishers**: Publish domain events
- **Domain Services**: Cross-aggregate business logic
- **Specification Pattern**: Complex query logic

### Alternative Approaches
- **Data Transfer Objects**: For simple CRUD operations
- **Active Record**: For simpler domain models
- **Transaction Script**: For procedural business logic
- **Table Module**: For database-centric designs

### Domain Patterns Used
- **Aggregate Pattern**: Consistency boundaries
- **Repository Pattern**: Persistence abstraction
- **Domain Events**: Decoupled communication
- **Value Objects**: Immutable concepts
- **Domain Services**: Cross-aggregate logic
- **Specification Pattern**: Business rules encapsulation
- **Factory Pattern**: Complex object creation