---
layout: default
title: Home
---

# Warehouse Operations Service Documentation

Warehouse management system for optimized picking, packing, and logistics processes with event-driven architecture and DDD.

## Overview

The Warehouse Operations Service manages all warehouse execution activities within the Paklog fulfillment platform. This bounded context orchestrates picking operations, packing stations, put walls for sortation, license plate management, location directives, and quality control processes.

## Quick Links

### Architecture
- [Architecture Overview](README.md) - Main documentation
- [Architecture Details](architecture/) - Detailed architecture documentation
- [Domain Architecture](domain-architecture/) - Domain design patterns

### Domain Documentation
- [Domain Overview](domain/) - Domain model documentation
- [API Documentation](api/) - API reference

### Operations
- [Operations Guide](operations/) - Operational procedures and guides
- [Developer Guides](guides/) - Development guides

## Technology Stack

- **Java 17+** - Programming language
- **Spring Boot 3.x** - Application framework
- **MongoDB** - Document database
- **Apache Kafka** - Event streaming
- **CloudEvents** - Event standard

## Key Features

- Pick list management
- Route optimization
- Packing station orchestration
- Put wall sortation
- License plate tracking
- Location directives
- Quality control
- Event-driven workflows

## Domain Model

### Aggregates
- **PickList** - Pick list execution
- **Package** - Packed boxes
- **PutWall** - Sortation wall management
- **LicensePlate** - Container tracking
- **LocationDirective** - Location selection rules
- **QualityHold** - Quality control holds
- **QualityCorrectiveAction** - Quality issue resolution

### Entities
- **PickInstruction** - Individual pick tasks
- **PutWallSlot** - Put wall slots
- **PackedItem** - Items in packages

### Value Objects
- **PickListId**, **OrderId**, **SkuCode**
- **BinLocation** - Warehouse locations
- **LicensePlateId** - Container identifiers
- **PutWallId**, **PutWallSlotId**

### Domain Events
- Pick list events (created, assigned, completed)
- Packaging events (created, completed)
- Put wall events (assigned, consolidated, released)
- License plate events (created, moved, picked, shipped)
- Quality events (hold created/released)

### Domain Services
- **PickListDomainService** - Pick list management
- **PickRouteOptimizer** - Route optimization
- **PackagingDomainService** - Packing orchestration
- **PutWallService** - Put wall management
- **LicensePlateService** - License plate lifecycle
- **LocationDirectiveService** - Location selection
- **QualityHoldService** - Quality management

### Location Selection Strategies
- Fixed location
- FIFO/LIFO
- Zone-based
- Capacity optimized
- Fast-moving/slow-moving
- Random
- And more...

## Architecture Patterns

- **Hexagonal Architecture** - Clean separation
- **Domain-Driven Design** - Rich domain models
- **Event-Driven Architecture** - Async integration
- **Strategy Pattern** - Pluggable behaviors
- **Repository Pattern** - Data access
- **Domain Services** - Complex orchestration
- **CQRS** - Command/query separation

## API Endpoints

### Pick List Management
- `POST /api/v1/picklists` - Create pick list
- `GET /api/v1/picklists/{id}` - Get pick list
- `POST /api/v1/picklists/{id}/assign` - Assign to worker
- `POST /api/v1/picklists/{id}/pick` - Confirm pick

### Packaging
- `POST /api/v1/packages` - Create package
- `GET /api/v1/packages/{id}` - Get package
- `POST /api/v1/packages/{id}/complete` - Complete package

### Put Wall
- `POST /api/v1/putwalls/{id}/assign` - Assign order to slot
- `POST /api/v1/putwalls/{id}/put` - Place item in slot

### License Plate
- `POST /api/v1/licenseplates` - Create license plate
- `POST /api/v1/licenseplates/{id}/move` - Move license plate

## Domain Features

### Pick Route Optimization
Minimizes travel distance and time for warehouse pickers.

### Location Directives
Flexible location selection with multiple strategies for optimal warehouse operations.

### Put Wall Sortation
Efficient order consolidation using configurable put wall slots.

### Quality Control
Comprehensive quality hold and corrective action tracking.

### License Plate Tracking
Full container lifecycle from receiving through shipping.

## Integration Points

### Consumes Events From
- Order Management (order released)
- Inventory (allocation confirmed)

### Publishes Events To
- Order Management (picking/packing completed)
- Inventory (item picked, stock movements)
- Shipment Transportation (package packed)

## Getting Started

1. Review the main [README](../README.md)
2. Explore the [Architecture](architecture/) documentation
3. Study the [Domain Model](domain/) details
4. Check the [API Documentation](api/)
5. Review [Operations Guide](operations/) for procedures

## Monitoring

- Health checks
- Prometheus metrics
- Custom business metrics
- Performance tracking
- Throughput monitoring

## Contributing

For contribution guidelines, please refer to the main [README](../README.md) in the project root.

## Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/paklog/warehouse-operations/issues)
- **Documentation**: Browse the guides in the navigation
- **Operations**: See [Operations Guide](operations/)
