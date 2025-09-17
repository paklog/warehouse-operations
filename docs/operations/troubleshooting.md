# Troubleshooting Guide

## 1. OVERVIEW

### Purpose and Primary Functionality
This troubleshooting guide provides systematic approaches to diagnosing and resolving common issues in the Warehouse Operations Service. It covers application startup problems, runtime errors, performance issues, and integration failures.

### When to Use This Guide vs. Alternatives
Use this guide when:
- **Application is failing to start** or behaving unexpectedly
- **Performance is degraded** compared to baseline metrics
- **Integration with external systems** is failing
- **Domain logic errors** are occurring in business workflows

Use alternatives for:
- Infrastructure issues (consult infrastructure team)
- Network connectivity problems (use network diagnostics)
- Hardware failures (contact system administrators)

### Troubleshooting Context
This guide assumes familiarity with:
- Spring Boot application structure
- MongoDB operations and queries
- Kafka messaging concepts
- Basic Linux/Unix command-line tools
- Docker container management

## 2. TECHNICAL SPECIFICATION

### Diagnostic Tools and Endpoints

#### Health Check Endpoints
```bash
# Overall application health
curl http://localhost:8080/actuator/health

# Detailed health with components
curl http://localhost:8080/actuator/health/mongo
curl http://localhost:8080/actuator/health/kafka

# Application info
curl http://localhost:8080/actuator/info
```

#### Metrics Endpoints
```bash
# JVM memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP request metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Database connection pool
curl http://localhost:8080/actuator/metrics/mongodb.driver.pool.size

# Kafka consumer lag
curl http://localhost:8080/actuator/metrics/kafka.consumer.lag.max
```

#### Logging Configuration
```yaml
# application.yml - Enable debug logging
logging:
  level:
    com.paklog.warehouse: DEBUG
    org.springframework.data.mongodb: DEBUG
    org.springframework.kafka: DEBUG
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: warehouse-operations.log
    max-size: 100MB
    max-history: 30
```

### Common Error Categories

#### 1. Application Startup Issues
- Configuration problems
- Database connectivity
- Missing dependencies
- Port conflicts

#### 2. Runtime Errors
- Domain logic violations
- Data consistency issues
- Transaction failures
- Event processing failures

#### 3. Performance Issues
- Slow database queries
- Memory leaks
- Thread pool exhaustion
- High CPU usage

#### 4. Integration Issues
- Kafka connectivity
- External API failures
- Authentication problems
- Network timeouts

## 3. IMPLEMENTATION EXAMPLES

### Application Startup Issues

#### Issue: Application Fails to Start with MongoDB Connection Error
```
Error: com.mongodb.MongoSocketOpenException: Exception opening socket
```

**Diagnosis Steps:**
```bash
# 1. Check MongoDB service status
systemctl status mongod
# or
docker ps | grep mongo

# 2. Test MongoDB connectivity
mongosh mongodb://localhost:27017/warehouse
# or
telnet localhost 27017

# 3. Check application configuration
grep -r "mongodb" src/main/resources/

# 4. Verify MongoDB authentication
mongosh mongodb://username:password@localhost:27017/warehouse
```

**Common Solutions:**
```yaml
# Fix 1: Correct MongoDB URI in application.yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/warehouse
      # Add authentication if needed
      # uri: mongodb://user:pass@localhost:27017/warehouse?authSource=admin

# Fix 2: Wait for MongoDB to be ready in Docker
version: '3.8'
services:
  app:
    depends_on:
      mongodb:
        condition: service_healthy
  mongodb:
    healthcheck:
      test: echo 'db.runCommand("ping").ok' | mongosh localhost:27017/test --quiet
      interval: 10s
      timeout: 5s
      retries: 5
```

#### Issue: Port Already in Use
```
Error: Port 8080 was already in use
```

**Diagnosis and Solution:**
```bash
# Find process using port 8080
lsof -i :8080
netstat -tulpn | grep :8080

# Kill the process
kill -9 <PID>

# Or change application port
echo "server.port=8081" >> application-local.properties
```

#### Issue: Bean Creation Failure
```
Error: Parameter 0 of constructor in PickListController required a bean of type 'PickListQueryService' that could not be found
```

**Diagnosis:**
```bash
# Check for missing @Component/@Service annotations
grep -r "class.*QueryService" src/main/java/
grep -r "@Component\|@Service" src/main/java/ | grep QueryService

# Check component scan configuration
grep -r "@ComponentScan" src/main/java/
```

**Solution:**
```java
// Add missing annotation
@Service  // Add this annotation
public class DefaultPickListQueryService implements PickListQueryService {
    // Implementation
}

// Or configure bean manually
@Configuration
public class ServiceConfig {
    @Bean
    public PickListQueryService pickListQueryService() {
        return new DefaultPickListQueryService();
    }
}
```

### Runtime Error Troubleshooting

#### Issue: Domain Logic Validation Errors
```
Error: java.lang.IllegalArgumentException: Cannot pick item - PickList already completed
```

**Diagnosis:**
```java
// Add detailed logging to domain objects
public class PickList extends AggregateRoot {
    public void pickItem(SkuCode sku, Quantity quantity, BinLocation location) {
        log.debug("Attempting to pick item: SKU={}, Quantity={}, Location={}, CurrentStatus={}",
                 sku, quantity, location, this.status);

        if (this.status == PickListStatus.COMPLETED) {
            log.error("Cannot pick item - PickList {} already completed at {}",
                     this.id, this.completedAt);
            throw new IllegalArgumentException("Cannot pick item - PickList already completed");
        }
        // ... rest of method
    }
}
```

**Prevention and Solution:**
```java
// Add optimistic locking
@Entity
public class PickListDocument {
    @Version
    private Long version;
    // ... other fields
}

// Check status before operations
@Service
public class PickListService {
    public void processItemPick(PickListId id, SkuCode sku, Quantity quantity, BinLocation location) {
        PickList pickList = repository.findById(id);

        // Validate state before operation
        if (!pickList.canPickItems()) {
            throw new PickListNotAvailableForPickingException(id, pickList.getStatus());
        }

        try {
            pickList.pickItem(sku, quantity, location);
            repository.save(pickList);
        } catch (OptimisticLockException e) {
            throw new ConcurrentModificationException("PickList was modified by another user");
        }
    }
}
```

#### Issue: Event Processing Failures
```
Error: Failed to process PickListCompletedEvent for pickList PL-12345
```

**Diagnosis:**
```bash
# Check Kafka consumer logs
kubectl logs -f <pod-name> | grep "kafka\|event"

# Check dead letter topic
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic warehouse-events-dlq --from-beginning

# Monitor consumer lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group warehouse-operations-group
```

**Solution:**
```java
// Add retry and error handling
@EventListener
@Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public void handle(PickListCompletedEvent event) {
    try {
        // Process event
        packageService.createPackageFromPickList(event.getPickListId());
    } catch (Exception e) {
        log.error("Failed to process PickListCompletedEvent: {}", event, e);
        throw e; // Retryable will handle this
    }
}

@Recover
public void recover(Exception e, PickListCompletedEvent event) {
    log.error("Failed to process event after retries, sending to DLQ: {}", event, e);
    deadLetterService.send(event, e);
}
```

### Performance Issues

#### Issue: Slow Database Queries
```
Slow query detected: db.pickListDocuments.find({pickerId: "PICKER-001"}) - 2.5s
```

**Diagnosis:**
```bash
# Enable MongoDB profiling
mongosh
use warehouse
db.setProfilingLevel(2, { slowms: 100 })

# Check slow queries
db.system.profile.find().sort({ts: -1}).limit(5).pretty()

# Check current operations
db.currentOp()

# Check indexes
db.pickListDocuments.getIndexes()
```

**Solution:**
```java
// Add database indexes
@Document(collection = "pickListDocuments")
@CompoundIndex(name = "picker_status_idx", def = "{'pickerId': 1, 'status': 1}")
@CompoundIndex(name = "order_created_idx", def = "{'orderId': 1, 'createdAt': -1}")
public class PickListDocument {
    @Indexed
    private String pickerId;

    @Indexed
    private String status;

    // ... other fields
}

// Optimize query projections
@Repository
public interface SpringPickListRepository extends MongoRepository<PickListDocument, String> {

    @Query(value = "{'pickerId': ?0, 'status': ?1}", fields = "{'id': 1, 'status': 1, 'createdAt': 1}")
    List<PickListSummaryProjection> findSummaryByPickerIdAndStatus(String pickerId, String status);

    interface PickListSummaryProjection {
        String getId();
        String getStatus();
        Instant getCreatedAt();
    }
}
```

#### Issue: Memory Leaks
```
OutOfMemoryError: Java heap space
```

**Diagnosis:**
```bash
# Monitor memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Generate heap dump
jcmd <PID> GC.run_finalization
jcmd <PID> VM.gc
jcmd <PID> GC.dump heap-dump.hprof

# Analyze with Eclipse MAT or VisualVM
java -jar eclipse-mat.jar heap-dump.hprof
```

**Common Causes and Solutions:**
```java
// Issue: Domain events not being cleared
public class PickListService {
    public void processPickList(PickListId id) {
        PickList pickList = repository.findById(id);
        pickList.complete();
        repository.save(pickList);

        // MUST clear events to prevent memory leak
        eventPublisher.publishAll(pickList.getDomainEvents());
        pickList.clearDomainEvents(); // This is critical!
    }
}

// Issue: Large result sets in queries
@Service
public class PickListQueryService {
    public Page<PickList> findByStatus(PickListStatus status, Pageable pageable) {
        // Use pagination instead of loading all results
        return repository.findByStatus(status, pageable);
    }
}

// Issue: Connection pool leaks
@Configuration
public class MongoConfig {
    @Bean
    public MongoClientSettings mongoClientSettings() {
        return MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(mongoUri))
            .applyToConnectionPoolSettings(builder ->
                builder.maxSize(20)
                       .minSize(5)
                       .maxWaitTime(30, TimeUnit.SECONDS)
                       .maxConnectionIdleTime(10, TimeUnit.MINUTES))
            .build();
    }
}
```

### Integration Issues

#### Issue: Kafka Connection Failures
```
Error: org.apache.kafka.common.errors.TimeoutException: Topic warehouse-events not present in metadata
```

**Diagnosis:**
```bash
# Check Kafka broker status
kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# List available topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Check consumer group status
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

# Test basic connectivity
telnet localhost 9092
```

**Solution:**
```bash
# Create missing topic
kafka-topics.sh --create \
  --topic warehouse-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# Or enable auto-topic creation (not recommended for production)
# server.properties: auto.create.topics.enable=true
```

```yaml
# Add retry configuration
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      retries: 3
      retry-backoff-ms: 1000
    consumer:
      enable-auto-commit: false
      auto-offset-reset: earliest
      properties:
        session.timeout.ms: 30000
        request.timeout.ms: 40000
```

#### Issue: External API Timeouts
```
Error: java.net.SocketTimeoutException: Read timeout
```

**Solution:**
```java
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return new RestTemplate(factory);
    }
}

// Add circuit breaker pattern
@Component
public class ExternalServiceClient {

    @CircuitBreaker(name = "external-service", fallbackMethod = "fallbackMethod")
    @Retryable(value = {SocketTimeoutException.class}, maxAttempts = 3)
    public String callExternalService(String request) {
        return restTemplate.postForObject("/api/external", request, String.class);
    }

    public String fallbackMethod(String request, Exception e) {
        log.warn("External service call failed, using fallback: {}", e.getMessage());
        return "FALLBACK_RESPONSE";
    }
}
```

## 4. TROUBLESHOOTING WORKFLOWS

### Systematic Debugging Approach

#### 1. Information Gathering
```bash
#!/bin/bash
# diagnostic-info.sh - Gather system information

echo "=== Application Status ==="
curl -s http://localhost:8080/actuator/health | jq '.'

echo "=== JVM Memory ==="
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq '.'

echo "=== Recent Logs ==="
tail -n 50 warehouse-operations.log

echo "=== Database Status ==="
mongosh --eval "db.adminCommand('ping')" mongodb://localhost:27017/warehouse

echo "=== Kafka Status ==="
kafka-topics.sh --bootstrap-server localhost:9092 --list

echo "=== System Resources ==="
ps aux | grep java
free -h
df -h
```

#### 2. Error Pattern Analysis
```java
// Custom error tracking
@ControllerAdvice
public class ErrorTrackingAdvice {

    private final MeterRegistry meterRegistry;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        // Track error patterns
        meterRegistry.counter("warehouse.errors",
            "type", e.getClass().getSimpleName(),
            "message", e.getMessage())
            .increment();

        // Log with correlation ID
        String correlationId = UUID.randomUUID().toString();
        log.error("Error [{}]: {}", correlationId, e.getMessage(), e);

        return ResponseEntity.status(500)
            .header("X-Correlation-ID", correlationId)
            .body(ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Internal server error")
                .correlationId(correlationId)
                .build());
    }
}
```

#### 3. Performance Monitoring
```java
@Component
public class PerformanceMonitor {

    @EventListener
    public void monitorPickListProcessing(PickListCompletedEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        // Processing logic
        sample.stop(Timer.builder("warehouse.picklist.processing.time")
            .tag("picker", event.getPickerId())
            .register(meterRegistry));
    }

    @Scheduled(fixedRate = 60000) // Every minute
    public void checkSystemHealth() {
        // Check database response time
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        mongoTemplate.execute("test", collection -> collection.estimatedDocumentCount());
        stopWatch.stop();

        if (stopWatch.getTotalTimeMillis() > 1000) {
            log.warn("Database response time is slow: {}ms", stopWatch.getTotalTimeMillis());
            alertService.sendAlert("DATABASE_SLOW",
                "Database response time: " + stopWatch.getTotalTimeMillis() + "ms");
        }
    }
}
```

### Emergency Response Procedures

#### 1. Service Degradation Response
```bash
# Immediate actions for performance issues
# 1. Scale horizontally if possible
kubectl scale deployment warehouse-operations --replicas=3

# 2. Enable circuit breakers
kubectl patch configmap warehouse-config --patch '{"data":{"circuit.breaker.enabled":"true"}}'

# 3. Reduce logging verbosity
kubectl patch configmap warehouse-config --patch '{"data":{"logging.level.com.paklog.warehouse":"WARN"}}'

# 4. Monitor key metrics
watch -n 5 'curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq ".measurements[0].value"'
```

#### 2. Data Corruption Recovery
```javascript
// MongoDB data verification script
// verify-data-integrity.js

db = db.getSiblingDB('warehouse');

print("=== Checking PickList Data Integrity ===");
let invalidPickLists = db.pickListDocuments.find({
    $or: [
        { status: { $nin: ['PENDING', 'ASSIGNED', 'COMPLETED'] } },
        { orderId: null },
        { orderId: '' },
        { instructions: { $size: 0 } }
    ]
});

print("Invalid PickLists found: " + invalidPickLists.count());
invalidPickLists.forEach(doc => {
    print("Invalid PickList: " + doc._id + " - " + JSON.stringify(doc));
});

print("=== Checking Package Data Integrity ===");
let invalidPackages = db.packageDocuments.find({
    $or: [
        { status: { $nin: ['PENDING', 'CONFIRMED', 'SHIPPED'] } },
        { packedItems: { $size: 0 } }
    ]
});

print("Invalid Packages found: " + invalidPackages.count());

// Generate repair script
print("=== Generating Repair Commands ===");
print("// Run these commands to fix data issues:");
```

#### 3. Event Replay Mechanism
```java
@Component
public class EventReplayService {

    public void replayEventsFromTimestamp(Instant fromTimestamp) {
        log.info("Starting event replay from {}", fromTimestamp);

        // Find events after timestamp
        List<DomainEventLog> events = eventLogRepository.findEventsAfter(fromTimestamp);

        for (DomainEventLog eventLog : events) {
            try {
                // Reconstruct and republish event
                DomainEvent event = eventSerializer.deserialize(eventLog.getEventData());
                eventPublisher.publish(event);

                log.debug("Replayed event: {} - {}", event.getClass().getSimpleName(), event.getId());

            } catch (Exception e) {
                log.error("Failed to replay event: {}", eventLog.getId(), e);
                // Continue with next event
            }
        }

        log.info("Event replay completed. Processed {} events", events.size());
    }
}
```

## 5. RELATED COMPONENTS

### Monitoring and Observability Tools
- **Micrometer**: Application metrics collection
- **Prometheus**: Metrics storage and alerting
- **Grafana**: Metrics visualization and dashboards
- **ELK Stack**: Centralized logging and analysis
- **Jaeger/Zipkin**: Distributed tracing

### External Debugging Tools
- **MongoDB Compass**: Database query and analysis
- **Kafka UI**: Message queue monitoring
- **Spring Boot Admin**: Application monitoring
- **JProfiler**: JVM profiling and analysis
- **Eclipse MAT**: Memory analysis

### Development Tools
- **IntelliJ Debugger**: Step-through debugging
- **Postman**: API testing and validation
- **Docker Desktop**: Container management
- **Maven**: Build and dependency debugging

### Alternative Approaches
- **APM Tools**: New Relic, DataDog for comprehensive monitoring
- **Cloud Monitoring**: AWS CloudWatch, Google Cloud Monitoring
- **Custom Dashboards**: Application-specific monitoring
- **Health Check Libraries**: Spring Boot Actuator alternatives