# Warehouse Operations Service Documentation

A comprehensive Spring Boot microservice for managing warehouse operations including pick lists, packaging, quality control, and license plate management.

## Documentation Structure

### Core Components
- [Architecture Overview](./architecture/README.md) - System architecture and design patterns
- [Domain Model](./domain/README.md) - Business domain and entities
- [API Reference](./api/README.md) - REST API endpoints and specifications

### Developer Guides
- [Getting Started](./guides/getting-started.md) - Setup and development environment
- [Implementation Examples](./guides/examples.md) - Code examples and patterns
- [Testing Guide](./guides/testing.md) - Testing strategies and best practices

### Operations
- [Configuration](./operations/configuration.md) - Application configuration
- [Troubleshooting](./operations/troubleshooting.md) - Common issues and solutions
- [Performance](./operations/performance.md) - Performance considerations and optimization

### Mobile & Integration
- [Mobile API](./mobile/README.md) - Mobile device integration
- [Event System](./events/README.md) - Domain events and messaging
- [Persistence](./persistence/README.md) - Data models and repositories

## Quick Reference

### Key Technologies
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MongoDB
- **Messaging**: Apache Kafka
- **Documentation**: OpenAPI/Swagger
- **Testing**: JUnit 5, Mockito

### API Endpoints
- **Pick Lists**: `/api/v1/picklists`
- **Packages**: `/api/v1/packages`
- **Mobile**: `/api/v1/mobile`
- **Swagger UI**: `/swagger-ui.html`

### Core Domain Entities
- `PickList` - Work assignments for warehouse pickers
- `Package` - Items prepared for shipping
- `LicensePlate` - Container tracking units
- `QualityInspection` - Quality control processes
- `Wave` - Batch processing units