# Warehouse Operations Service

Warehouse management system for optimized picking, packing, and logistics processes with event-driven architecture and DDD.

## Overview

The Warehouse Operations Service manages all warehouse execution activities within the Paklog fulfillment platform. This bounded context orchestrates picking operations, packing stations, put walls for sortation, license plate management, location directives, and quality control processes. It optimizes warehouse workflows to maximize throughput and accuracy while minimizing labor costs.

## Domain-Driven Design

### Bounded Context
**Warehouse Operations & Execution** - Manages all physical warehouse processes including picking, packing, sortation, receiving, putaway, and quality control.

### Core Domain Model

#### Aggregates
- **PickList** - Pick list assignment and execution for order fulfillment
- **Package** - Packed box ready for shipment
- **PutWall** - Sortation wall with slots for order consolidation
- **LicensePlate** - Inventory container tracking (pallet, case, etc.)
- **LocationDirective** - Rules for optimal location selection
- **QualityHold** - Quality control hold and inspection
- **QualityCorrectiveAction** - Corrective actions for quality issues

#### Entities
- **PickInstruction** - Individual pick task within pick list
- **PutWallSlot** - Individual slot on put wall
- **PackedItem** - Item packed in a package

#### Value Objects
- **PickListId** - Unique pick list identifier
- **OrderId** - Fulfillment order reference
- **SkuCode** - Product identifier
- **BinLocation** - Warehouse location identifier
- **Address** - Shipping address
- **OrderItem** - Order line item details
- **LicensePlateId** - License plate identifier
- **LocationContext** - Context for location selection
- **LocationQuery** - Query for location directive
- **PutWallId** - Put wall identifier
- **PutWallSlotId** - Put wall slot identifier

#### Domain Events
- **PickListCreatedEvent** - Pick list generated
- **PickListAssignedEvent** - Pick list assigned to worker
- **ItemPickedEvent** - Individual item picked
- **PickListCompletedEvent** - All items picked
- **PackageCreatedEvent** - Package created at packing station
- **PackageCompletedEvent** - Package sealed and labeled
- **OrderAssignedToSlotEvent** - Order assigned to put wall slot
- **ItemPlacedInSlotEvent** - Item placed in put wall slot
- **OrderConsolidatedInSlotEvent** - All items for order consolidated
- **SlotReleasedEvent** - Put wall slot released
- **LicensePlateCreatedEvent** - License plate generated
- **LicensePlateMovedEvent** - License plate moved to new location
- **LicensePlatePickedEvent** - License plate picked
- **LicensePlateShippedEvent** - License plate shipped
- **QualityHoldCreatedEvent** - Quality hold initiated
- **QualityHoldReleasedEvent** - Quality hold released

#### Domain Services
- **PickListDomainService** - Pick list generation and assignment
- **PickRouteOptimizer** - Optimizes pick path through warehouse
- **PackagingDomainService** - Packing orchestration
- **PutWallService** - Put wall slot management
- **LicensePlateService** - License plate lifecycle management
- **LocationDirectiveService** - Location selection logic
- **QualityHoldService** - Quality hold management
- **LocationSelector** (Strategy Pattern) - Multiple location selection strategies:
  - FixedLocationSelector
  - FifoLocationSelector
  - LifoLocationSelector
  - RandomLocationSelector
  - ZoneBasedLocationSelector
  - CapacityOptimizedLocationSelector
  - FastMovingLocationSelector
  - And more...

### Ubiquitous Language
- **Pick List**: Ordered list of items to pick for fulfillment
- **Pick Instruction**: Single item pick task
- **Pick Route**: Optimized path through warehouse
- **Packing Station**: Workstation for packing orders
- **Put Wall**: Sortation wall with slots for orders
- **Slot**: Individual position on put wall
- **License Plate**: Container identifier (pallet, case, each)
- **Location Directive**: Rule for selecting storage/pick location
- **Putaway**: Storing inventory in locations
- **Quality Hold**: Temporary hold for inspection
- **Corrective Action**: Resolution for quality issues
- **Wave**: Batch of orders released for picking

## Architecture & Patterns

### Hexagonal Architecture (Ports and Adapters)

```
src/main/java/com/paklog/warehouse/
├── domain/                           # Core business logic
│   ├── picklist/                    # Pick list subdomain
│   │   ├── PickList.java            # Aggregate root
│   │   ├── PickInstruction.java     # Entity
│   │   ├── PickListDomainService.java
│   │   └── PickRouteOptimizer.java
│   ├── packaging/                   # Packaging subdomain
│   │   ├── Package.java             # Aggregate root
│   │   └── PackagingDomainService.java
│   ├── putwall/                     # Put wall subdomain
│   │   ├── PutWall.java             # Aggregate root
│   │   ├── PutWallSlot.java         # Entity
│   │   └── PutWallService.java
│   ├── licenseplate/                # License plate subdomain
│   │   ├── LicensePlate.java        # Aggregate root
│   │   └── LicensePlateService.java
│   ├── location/                    # Location directive subdomain
│   │   ├── LocationDirective.java   # Aggregate root
│   │   ├── LocationDirectiveService.java
│   │   └── LocationSelector.java    # Strategy interface
│   ├── quality/                     # Quality control subdomain
│   │   ├── QualityHold.java         # Aggregate root
│   │   └── QualityHoldService.java
│   └── shared/                      # Shared value objects
├── application/                      # Use cases & orchestration
│   ├── service/                     # Application services
│   └── port/                        # Application ports
└── infrastructure/                   # External adapters
    ├── persistence/                 # MongoDB repositories
    ├── messaging/                   # Kafka publishers
    ├── web/                         # REST controllers
    └── config/                      # Configuration
```

### Design Patterns & Principles
- **Hexagonal Architecture** - Clean separation of domain and infrastructure
- **Domain-Driven Design** - Rich domain model with multiple bounded contexts
- **Event-Driven Architecture** - Integration via domain events
- **Strategy Pattern** - Pluggable location selection strategies
- **Repository Pattern** - Data access abstraction
- **Aggregate Pattern** - Consistency boundaries
- **Domain Services** - Complex business logic coordination
- **CQRS** - Command/query separation
- **SOLID Principles** - Maintainable and extensible code

## Technology Stack

### Core Framework
- **Java 17+** - Programming language
- **Spring Boot 3.x** - Application framework
- **Maven** - Build and dependency management

### Data & Persistence
- **MongoDB** - Document database for aggregates
- **Spring Data MongoDB** - Data access layer

### Messaging & Events
- **Apache Kafka** - Event streaming platform
- **Spring Kafka** - Kafka integration
- **CloudEvents** - Standardized event format

### API & Documentation
- **Spring Web MVC** - REST API framework
- **Bean Validation** - Input validation
- **OpenAPI/Swagger** - API documentation

### Observability
- **Spring Boot Actuator** - Health checks and metrics
- **Micrometer** - Metrics collection
- **Prometheus** - Metrics aggregation
- **Grafana** - Metrics visualization

### Testing
- **JUnit 5** - Unit testing framework
- **Testcontainers** - Integration testing
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Local development environment

## Standards Applied

### Architectural Standards
- ✅ Hexagonal Architecture (Ports and Adapters)
- ✅ Domain-Driven Design tactical patterns
- ✅ Event-Driven Architecture
- ✅ Microservices architecture
- ✅ RESTful API design
- ✅ Subdomain decomposition

### Code Quality Standards
- ✅ SOLID principles
- ✅ Clean Code practices
- ✅ Comprehensive unit and integration testing
- ✅ Domain-driven design patterns
- ✅ Strategy pattern for extensibility
- ✅ Rich domain models with business logic

### Event & Integration Standards
- ✅ CloudEvents specification
- ✅ Event-driven integration
- ✅ At-least-once delivery semantics
- ✅ Event versioning strategy

### Observability Standards
- ✅ Structured logging (JSON)
- ✅ Health check endpoints
- ✅ Prometheus metrics
- ✅ Business metrics tracking
- ✅ Correlation ID propagation

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/paklog/warehouse-operations.git
   cd warehouse-operations
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d mongodb kafka
   ```

3. **Build and run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify the service is running**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Using Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f warehouse-operations

# Stop all services
docker-compose down
```

## API Documentation

Once running, access the interactive API documentation:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### Key Endpoints

#### Pick List Management
- `POST /api/v1/picklists` - Create pick list
- `GET /api/v1/picklists/{id}` - Get pick list
- `POST /api/v1/picklists/{id}/assign` - Assign to worker
- `POST /api/v1/picklists/{id}/pick` - Confirm item picked

#### Packaging
- `POST /api/v1/packages` - Create package
- `GET /api/v1/packages/{id}` - Get package
- `POST /api/v1/packages/{id}/complete` - Complete package

#### Put Wall
- `POST /api/v1/putwalls/{id}/assign` - Assign order to slot
- `POST /api/v1/putwalls/{id}/put` - Place item in slot

#### License Plate
- `POST /api/v1/licenseplates` - Create license plate
- `POST /api/v1/licenseplates/{id}/move` - Move license plate

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run tests with coverage
mvn clean verify jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Configuration

Key configuration properties:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/warehouse_operations
  kafka:
    bootstrap-servers: localhost:9092

warehouse:
  picking:
    route-optimization-enabled: true
  putwall:
    default-slot-count: 48
  location:
    default-strategy: FIFO
```

## Event Integration

### Consumed Events
- `com.paklog.fulfillment.order.released` - From Order Management
- `com.paklog.inventory.allocation.confirmed` - From Inventory

### Published Events
- `com.paklog.warehouse.picklist.created.v1`
- `com.paklog.warehouse.picklist.completed.v1`
- `com.paklog.warehouse.item.picked.v1`
- `com.paklog.warehouse.package.packed.v1`
- `com.paklog.warehouse.package.completed.v1`
- `com.paklog.warehouse.licenseplate.created.v1`
- `com.paklog.warehouse.licenseplate.moved.v1`
- `com.paklog.warehouse.quality.hold.created.v1`

### Event Format
All events follow the CloudEvents specification and are published to Kafka.

## Domain Features

### Pick Route Optimization
Optimizes pick path to minimize travel distance and time.

### Location Directives
Flexible location selection with multiple strategies:
- Fixed locations
- FIFO/LIFO
- Zone-based
- Capacity optimization
- Fast-moving vs slow-moving
- And more...

### Put Wall Sortation
Efficient order consolidation using put wall slots.

### Quality Control
Quality holds and corrective action tracking.

### License Plate Tracking
Full container lifecycle from receiving through shipping.

## Monitoring

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

### Custom Metrics
- `warehouse.picklists.created`
- `warehouse.picklists.completed`
- `warehouse.packages.created`
- `warehouse.packages.completed`
- `warehouse.putwall.throughput`

## Contributing

1. Follow hexagonal architecture principles
2. Implement domain logic in appropriate subdomain
3. Use domain services for cross-aggregate logic
4. Maintain aggregate consistency boundaries
5. Use strategy pattern for extensible behaviors
6. Write comprehensive tests for all layers
7. Document domain concepts using ubiquitous language
8. Follow existing code style and conventions

## License

Copyright © 2024 Paklog. All rights reserved.
