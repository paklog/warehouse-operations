# Architecture Overview

## 1. OVERVIEW

### Purpose and Primary Functionality
The Warehouse Operations Service is a Spring Boot microservice designed to manage core warehouse operations including:
- **Pick List Management**: Creating, assigning, and tracking warehouse pick lists
- **Package Processing**: Managing package creation, confirmation, and shipping preparation
- **Quality Control**: Handling quality inspections and holds
- **License Plate Tracking**: Managing container/pallet tracking throughout warehouse
- **Mobile Operations**: Supporting mobile device workflows for warehouse workers

### When to Use This Component vs. Alternatives
Use this service when you need:
- **Centralized warehouse operations management** with event-driven architecture
- **Mobile-first warehouse workflows** with barcode scanning capabilities
- **Domain-driven design** with strong business logic encapsulation
- **Event sourcing patterns** for audit trails and state reconstruction

Consider alternatives for:
- Simple inventory management (use dedicated inventory service)
- Pure order management (use order management service)
- Basic shipping operations (use shipping service)

### Architectural Context
This service fits within a microservices ecosystem as the **warehouse operations hub**:
```
┌─────────────────┐    ┌──────────────────────┐    ┌─────────────────┐
│ Order Management│────│ Warehouse Operations │────│ Shipping Service│
└─────────────────┘    └──────────────────────┘    └─────────────────┘
                                   │
                       ┌───────────┼───────────┐
                       │           │           │
              ┌────────▼────┐ ┌────▼────┐ ┌───▼──────┐
              │ Inventory   │ │ Mobile  │ │ Analytics│
              │ Service     │ │ Devices │ │ Service  │
              └─────────────┘ └─────────┘ └──────────┘
```

## 2. TECHNICAL SPECIFICATION

### Architecture Pattern
**Domain-Driven Design (DDD)** with **Hexagonal Architecture** (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                     │
│  ┌─────────────────┐  ┌─────────────────────────────────┐│
│  │   REST APIs     │  │      Mobile Services           ││
│  │  (Controllers)  │  │   (Workflow, Notification)     ││
│  └─────────────────┘  └─────────────────────────────────┘│
├─────────────────────────────────────────────────────────┤
│                     Domain Layer                        │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐│
│  │  PickList   │ │  Package    │ │   LicensePlate      ││
│  │  Aggregate  │ │  Aggregate  │ │   Aggregate         ││
│  └─────────────┘ └─────────────┘ └─────────────────────┘│
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐│
│  │  Quality    │ │    Wave     │ │    Workload         ││
│  │  Aggregate  │ │  Aggregate  │ │    Management       ││
│  └─────────────┘ └─────────────┘ └─────────────────────┘│
├─────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                   │
│  ┌─────────────────┐  ┌─────────────────────────────────┐│
│  │   MongoDB       │  │        Kafka Events            ││
│  │  Repositories   │  │      (CloudEvents)             ││
│  └─────────────────┘  └─────────────────────────────────┘│
└─────────────────────────────────────────────────────────┘
```

### Core Technologies
- **Runtime**: Java 17, Spring Boot 3.2.0
- **Persistence**: MongoDB with Spring Data
- **Messaging**: Apache Kafka with CloudEvents
- **Web**: Spring Web MVC, WebSocket
- **Documentation**: OpenAPI 3 / Swagger
- **Testing**: JUnit 5, Mockito, Embedded MongoDB

### Key Design Patterns

#### 1. Domain-Driven Design (DDD)
- **Aggregates**: `PickList`, `Package`, `LicensePlate`, `QualityInspection`, `Wave`
- **Value Objects**: `SkuCode`, `Quantity`, `BinLocation`, `OrderId`
- **Domain Events**: `PickListAssignedEvent`, `ItemPickedEvent`, `PackageConfirmedEvent`
- **Domain Services**: Business logic that doesn't belong to a specific aggregate

#### 2. CQRS (Command Query Responsibility Segregation)
- **Commands**: `ConfirmItemPick`, `CreatePackage`, `AssignPickList`
- **Queries**: `PickListQueryService`, optimized read models
- **Handlers**: Separate command and query handlers

#### 3. Event Sourcing (Partial)
- Domain events are published to Kafka
- Aggregates track domain events for publishing
- Events enable audit trails and eventual consistency

#### 4. Hexagonal Architecture
- **Ports** (Interfaces): Repository interfaces, Service interfaces
- **Adapters**: MongoDB repositories, REST controllers, Kafka publishers

## 3. IMPLEMENTATION EXAMPLES

### Basic Domain Aggregate Usage
```java
// Creating a new pick list
PickList pickList = new PickList(OrderId.of("ORD-123"));
pickList.addInstruction(new PickInstruction(
    SkuCode.of("SKU-001"),
    Quantity.of(5),
    BinLocation.of("A1-B2-C3")
));

// Assigning to picker
pickList.assignToPicker("PICKER-001");

// Processing pick
pickList.pickItem(
    SkuCode.of("SKU-001"),
    Quantity.of(5),
    BinLocation.of("A1-B2-C3")
);

// Events are automatically registered
List<DomainEvent> events = pickList.getDomainEvents();
```

### Command Handler Pattern
```java
@Component
public class ConfirmItemPickHandler {
    private final PickListRepository pickListRepository;
    private final DomainEventPublisher eventPublisher;

    public void handle(ConfirmItemPick command) {
        PickList pickList = pickListRepository.findById(command.getPickListId());

        pickList.pickItem(
            command.getSkuCode(),
            command.getQuantity(),
            command.getBinLocation()
        );

        pickListRepository.save(pickList);
        eventPublisher.publishAll(pickList.getDomainEvents());
        pickList.clearDomainEvents();
    }
}
```

### Repository Adapter Pattern
```java
@Component
public class PickListRepositoryAdapter implements PickListRepository {
    private final SpringPickListRepository springRepository;

    @Override
    public PickList findById(PickListId id) {
        return springRepository.findById(id.getValue())
            .map(this::toDomain)
            .orElse(null);
    }

    private PickList toDomain(PickListDocument doc) {
        // Conversion logic from document to domain object
    }
}
```

### Event Publishing
```java
@EventListener
public void handle(PickListCompletedEvent event) {
    CloudEvent cloudEvent = CloudEventBuilder.v1()
        .withId(UUID.randomUUID().toString())
        .withSource(URI.create("/fulfillment/warehouse-operations-service"))
        .withType("com.paklog.warehouse.picklist.completed")
        .withData("application/json", event)
        .build();

    kafkaTemplate.send("warehouse-events", cloudEvent);
}
```

## 4. TROUBLESHOOTING

### Common Architecture Issues

#### 1. Aggregate Boundary Violations
**Problem**: Trying to modify multiple aggregates in a single transaction
```java
// ❌ Wrong - violates aggregate boundaries
public void processOrder(OrderId orderId) {
    PickList pickList = pickListRepo.findByOrderId(orderId);
    Package package = packageRepo.findByOrderId(orderId);

    pickList.complete(); // Modifying aggregate 1
    package.confirm();   // Modifying aggregate 2 in same transaction
}
```

**Solution**: Use domain events for inter-aggregate communication
```java
// ✅ Correct - use events
@EventListener
public void on(PickListCompletedEvent event) {
    Package package = packageRepo.findByOrderId(event.getOrderId());
    package.markReadyForPacking();
    packageRepo.save(package);
}
```

#### 2. Repository Pattern Misuse
**Problem**: Business logic in repositories
```java
// ❌ Wrong - business logic in repository
public void confirmPickList(PickListId id) {
    PickListDocument doc = findById(id);
    doc.setStatus("COMPLETED");
    doc.setCompletedAt(Instant.now());
    // ... complex business logic
}
```

**Solution**: Keep repositories simple, logic in domain
```java
// ✅ Correct - logic in domain
PickList pickList = repository.findById(id);
pickList.complete(); // Business logic in domain
repository.save(pickList);
```

#### 3. Domain Event Memory Leaks
**Problem**: Forgetting to clear domain events after publishing
```java
// ❌ Wrong - events accumulate
pickList.processItem(item);
repository.save(pickList);
// Events never cleared!
```

**Solution**: Always clear events after publishing
```java
// ✅ Correct - clear events
repository.save(pickList);
eventPublisher.publishAll(pickList.getDomainEvents());
pickList.clearDomainEvents();
```

### Performance Considerations

#### 1. Event Processing
- **Async Processing**: Use `@Async` for event handlers
- **Batch Publishing**: Accumulate events before publishing
- **Dead Letter Queues**: Handle failed event processing

#### 2. MongoDB Optimization
- **Indexes**: Create indexes on frequently queried fields
- **Projection**: Use Spring Data projections for read-only queries
- **Connection Pooling**: Configure appropriate connection pool sizes

#### 3. API Performance
- **Caching**: Use `@Cacheable` for read-heavy operations
- **Pagination**: Implement proper pagination for list endpoints
- **Response Compression**: Enable gzip compression

## 5. RELATED COMPONENTS

### Dependencies
- **Spring Boot Starter Web**: REST API framework
- **Spring Boot Starter Data MongoDB**: Database access
- **Spring Kafka**: Event messaging
- **CloudEvents**: Event standardization
- **SpringDoc OpenAPI**: API documentation

### Components Commonly Used Alongside
- **Order Management Service**: Provides fulfillment orders
- **Inventory Service**: Stock level management
- **Shipping Service**: Package shipping coordination
- **Analytics Service**: Operational metrics and reporting
- **Mobile Apps**: Warehouse worker interfaces

### Alternative Approaches
- **Simple CRUD**: For basic warehouse operations without complex workflows
- **State Machines**: For simpler state management (Spring State Machine)
- **Workflow Engines**: For complex business processes (Camunda, Zeebe)
- **Event Streaming**: For real-time data processing (Kafka Streams)

### Integration Patterns
- **Event-Driven**: Publish domain events for loose coupling
- **API Gateway**: Route external requests through gateway
- **Circuit Breaker**: Resilience patterns for external service calls
- **Saga Pattern**: Distributed transaction management across services