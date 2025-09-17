# Getting Started Guide

## Prerequisites

### System Requirements
- **Java 17** or higher
- **Maven 3.8+** for dependency management
- **MongoDB 4.4+** for data persistence
- **Apache Kafka 3.3+** for event messaging
- **IDE** with Java 17 support (IntelliJ IDEA, Eclipse, VS Code)

### Development Tools (Optional)
- **Docker & Docker Compose** for local infrastructure
- **Postman** or **curl** for API testing
- **MongoDB Compass** for database management
- **Kafka UI** for message monitoring

## Quick Start

### 1. Clone and Build
```bash
# Clone the repository
git clone <repository-url>
cd warehouse-operations

# Build the application
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package
```

### 2. Start Infrastructure
Using Docker Compose (recommended for development):
```bash
# Start MongoDB and Kafka
docker-compose up -d mongodb kafka

# Verify services are running
docker-compose ps
```

Or install manually:
- MongoDB: https://docs.mongodb.com/manual/installation/
- Kafka: https://kafka.apache.org/quickstart

### 3. Configure Application
Create `application-local.yml` (ignored by git):
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/warehouse-local
      database: warehouse-local
  kafka:
    bootstrap-servers: localhost:9092

logging:
  level:
    com.paklog.warehouse: DEBUG
```

### 4. Run Application
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Using Java
java -jar target/warehouse-operations-0.0.1-SNAPSHOT.jar --spring.profiles.active=local

# Using IDE
# Run WarehouseOperationsApplication.main() with VM options: -Dspring.profiles.active=local
```

### 5. Verify Installation
```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP","components":{"mongo":{"status":"UP"},"kafka":{"status":"UP"}}}

# Access Swagger UI
open http://localhost:8080/swagger-ui.html

# Check API documentation
curl http://localhost:8080/api-docs
```

## Development Workflow

### Project Structure
```
src/
├── main/java/com/paklog/warehouse/
│   ├── adapter/                    # External interfaces
│   │   ├── rest/                   # REST controllers
│   │   └── persistence/            # Database adapters
│   ├── application/                # Application services
│   │   ├── service/                # Application logic
│   │   └── mobile/                 # Mobile-specific services
│   ├── domain/                     # Core business logic
│   │   ├── shared/                 # Shared domain concepts
│   │   ├── picklist/               # Pick list domain
│   │   ├── packaging/              # Package domain
│   │   └── [other domains]/        # Other bounded contexts
│   ├── infrastructure/             # Technical infrastructure
│   └── config/                     # Configuration classes
├── test/java/                      # Test classes (mirror structure)
└── resources/
    ├── application.yml             # Main configuration
    └── application-test.yml        # Test configuration
```

### Development Commands
```bash
# Run with auto-reload (requires spring-boot-devtools)
mvn spring-boot:run

# Run specific test class
mvn test -Dtest=PickListControllerTest

# Run tests with coverage
mvn test jacoco:report

# Check code style
mvn checkstyle:check

# Start application with debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

### IDE Setup

#### IntelliJ IDEA
1. Import as Maven project
2. Set Project SDK to Java 17
3. Install plugins:
   - Spring Boot
   - MongoDB Plugin
   - Docker
4. Configure run configuration:
   - Main class: `WarehouseOperationsApplication`
   - VM options: `-Dspring.profiles.active=local`
   - Environment variables: `MONGODB_URI=mongodb://localhost:27017/warehouse-local`

#### VS Code
1. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - MongoDB for VS Code
2. Open project folder
3. Configure launch.json:
```json
{
  "type": "java",
  "name": "Warehouse Operations",
  "request": "launch",
  "mainClass": "com.paklog.warehouse.WarehouseOperationsApplication",
  "vmArgs": "-Dspring.profiles.active=local"
}
```

## Common Development Tasks

### 1. Creating a New Domain Entity
```java
// 1. Create the aggregate root
package com.paklog.warehouse.domain.mynewdomain;

public class MyNewEntity extends AggregateRoot {
    private final MyEntityId id;
    private MyEntityStatus status;

    public MyNewEntity(MyEntityId id) {
        this.id = Objects.requireNonNull(id);
        this.status = MyEntityStatus.CREATED;
        registerEvent(new MyEntityCreatedEvent(id));
    }

    // Business methods here
}

// 2. Create repository interface
public interface MyNewEntityRepository {
    MyNewEntity findById(MyEntityId id);
    void save(MyNewEntity entity);
}

// 3. Create MongoDB adapter
@Component
public class MyNewEntityRepositoryAdapter implements MyNewEntityRepository {
    // Implementation
}
```

### 2. Adding a New REST Endpoint
```java
@RestController
@RequestMapping("/api/v1/my-entities")
@Tag(name = "My Entities", description = "API for managing my entities")
public class MyEntityController {

    private final MyEntityService service;

    @GetMapping("/{id}")
    public ResponseEntity<MyEntityDto> getEntity(@PathVariable String id) {
        MyEntity entity = service.findById(MyEntityId.of(id));
        return ResponseEntity.ok(MyEntityDto.fromDomain(entity));
    }
}
```

### 3. Creating Tests
```java
// Domain test
class MyNewEntityTest {
    @Test
    void shouldCreateEntityWithValidId() {
        MyEntityId id = MyEntityId.generate();
        MyNewEntity entity = new MyNewEntity(id);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getStatus()).isEqualTo(MyEntityStatus.CREATED);
        assertThat(entity.getDomainEvents()).hasSize(1);
    }
}

// Integration test
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyEntityControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnEntityWhenFound() {
        ResponseEntity<MyEntityDto> response = restTemplate.getForEntity(
            "/api/v1/my-entities/test-id", MyEntityDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## Testing Strategy

### Unit Tests
- **Domain Objects**: Test business logic and invariants
- **Value Objects**: Test validation and immutability
- **Application Services**: Test orchestration logic

```bash
# Run only unit tests (fast)
mvn test -Dtest="**/*Test.java"
```

### Integration Tests
- **Repository Adapters**: Test MongoDB integration
- **REST Controllers**: Test HTTP endpoints
- **Event Publishers**: Test Kafka integration

```bash
# Run only integration tests
mvn test -Dtest="**/*IntegrationTest.java"
```

### Test Configuration
The application uses different configurations for testing:
```yaml
# application-test.yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/warehouse-test
  kafka:
    bootstrap-servers: ${spring.embedded.kafka.brokers}

logging:
  level:
    com.paklog.warehouse: DEBUG
    org.springframework.test: INFO
```

## Configuration Management

### Environment-Specific Configuration
```yaml
# application.yml (default)
spring:
  application:
    name: warehouse-operations-service

# application-dev.yml (development)
spring:
  data:
    mongodb:
      uri: mongodb://dev-server:27017/warehouse-dev

# application-prod.yml (production)
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

### External Configuration
Use environment variables for sensitive data:
```bash
export MONGODB_URI="mongodb://user:pass@prod-server:27017/warehouse"
export KAFKA_BOOTSTRAP_SERVERS="kafka1:9092,kafka2:9092"
```

### Configuration Properties
Create custom configuration classes:
```java
@ConfigurationProperties(prefix = "warehouse")
@Component
public class WarehouseProperties {
    private Picking picking = new Picking();
    private Packing packing = new Packing();

    public static class Picking {
        private RouteOptimization routeOptimization = new RouteOptimization();

        public static class RouteOptimization {
            private boolean enabled = true;
            private String strategy = "continuous";
        }
    }
}
```

## Troubleshooting

### Common Issues

#### 1. Application Won't Start
```bash
# Check Java version
java -version

# Verify MongoDB connection
mongosh mongodb://localhost:27017/warehouse

# Check port availability
netstat -an | grep 8080
```

#### 2. Tests Failing
```bash
# Run with verbose output
mvn test -X

# Run single test class
mvn test -Dtest=PickListTest

# Skip tests temporarily
mvn spring-boot:run -DskipTests
```

#### 3. Database Connection Issues
```bash
# Check MongoDB status
systemctl status mongod

# View MongoDB logs
tail -f /var/log/mongodb/mongod.log

# Test connection
mongosh --eval "db.runCommand('ping')"
```

#### 4. Kafka Connection Issues
```bash
# Check Kafka status
bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# List topics
bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# Create topic manually
bin/kafka-topics.sh --create --topic warehouse-events --bootstrap-server localhost:9092
```

### Debug Mode
Enable debug logging for specific packages:
```yaml
logging:
  level:
    com.paklog.warehouse: DEBUG
    org.springframework.data.mongodb: DEBUG
    org.springframework.kafka: DEBUG
```

### Performance Monitoring
```bash
# View application metrics
curl http://localhost:8080/actuator/metrics

# Monitor JVM
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Database connection pool
curl http://localhost:8080/actuator/metrics/mongodb.driver.pool.size
```

## Next Steps

1. **Explore the Domain Model**: Read [Domain Model Documentation](../domain/README.md)
2. **API Integration**: Check [API Reference](../api/README.md)
3. **Mobile Development**: See [Mobile API Guide](../mobile/README.md)
4. **Production Deployment**: Review [Operations Guide](../operations/README.md)
5. **Contributing**: Follow [Development Guidelines](./development-guidelines.md)