# Performance Guide

## 1. OVERVIEW

### Purpose and Primary Functionality
This performance guide provides comprehensive strategies for optimizing the Warehouse Operations Service across all layers - from database queries to API response times, memory usage, and event processing throughput.

### When to Use This Guide vs. Alternatives
Use this guide when:
- **Response times exceed** acceptable thresholds (>500ms for APIs)
- **Memory usage grows** continuously or exceeds limits
- **Database queries are slow** (>100ms for simple queries)
- **Event processing lags** behind production rate
- **System throughput** needs to scale for higher loads

Use alternatives for:
- Infrastructure scaling (consult DevOps team)
- Network optimization (use network performance tools)
- Hardware upgrades (contact system administrators)

### Performance Context
This service operates in a high-throughput environment with:
- **Concurrent operations**: Multiple pickers working simultaneously
- **Real-time updates**: Mobile devices sending frequent updates
- **Event streams**: High-volume domain events via Kafka
- **Data consistency**: Strong consistency within aggregates

## 2. TECHNICAL SPECIFICATION

### Performance Targets

#### Response Time Targets
| Operation Type | Target | Acceptable | Investigation Threshold |
|----------------|--------|------------|------------------------|
| Simple GET (by ID) | <50ms | <200ms | >500ms |
| Complex queries | <200ms | <500ms | >1000ms |
| POST/PUT operations | <100ms | <300ms | >800ms |
| Batch operations | <500ms | <2000ms | >5000ms |

#### Throughput Targets
| Operation | Target TPS | Peak TPS | Acceptable Load |
|-----------|------------|----------|-----------------|
| Pick confirmations | 100 TPS | 300 TPS | 80% CPU |
| Pick list queries | 200 TPS | 500 TPS | 70% CPU |
| Package operations | 50 TPS | 150 TPS | 60% CPU |
| Event processing | 1000 TPS | 2000 TPS | 75% CPU |

#### Resource Usage Targets
- **JVM Heap**: <80% utilization
- **CPU**: <70% average, <90% peak
- **Database connections**: <80% pool usage
- **Kafka lag**: <1000 messages per partition

### Monitoring and Measurement

#### Key Performance Metrics
```java
@Component
public class PerformanceMetrics {

    private final MeterRegistry meterRegistry;

    // Response time tracking
    @EventListener
    public void trackApiResponse(ApiResponseEvent event) {
        Timer.builder("api.response.time")
            .tag("endpoint", event.getEndpoint())
            .tag("method", event.getMethod())
            .tag("status", String.valueOf(event.getStatusCode()))
            .register(meterRegistry)
            .record(event.getDuration(), TimeUnit.MILLISECONDS);
    }

    // Database operation timing
    @EventListener
    public void trackDatabaseOperation(DatabaseOperationEvent event) {
        Timer.builder("database.operation.time")
            .tag("collection", event.getCollection())
            .tag("operation", event.getOperation())
            .register(meterRegistry)
            .record(event.getDuration(), TimeUnit.MILLISECONDS);
    }

    // Memory usage tracking
    @Scheduled(fixedRate = 30000)
    public void recordMemoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

        Gauge.builder("jvm.memory.heap.used.percent")
            .register(meterRegistry, () ->
                (double) heapUsage.getUsed() / heapUsage.getMax() * 100);
    }
}
```

#### Performance Testing Setup
```java
@SpringBootTest
class PerformanceTest {

    @Test
    void pickListCreationPerformance() {
        int numberOfPickLists = 1000;
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        List<PickList> pickLists = IntStream.range(0, numberOfPickLists)
            .parallel()
            .mapToObj(i -> createTestPickList())
            .collect(Collectors.toList());
        stopWatch.stop();

        double throughputPerSecond = numberOfPickLists / (stopWatch.getTotalTimeSeconds());
        assertThat(throughputPerSecond).isGreaterThan(100); // Target: 100 TPS

        // Memory usage check
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        assertThat(memoryUsed).isLessThan(500 * 1024 * 1024); // Max 500MB
    }
}
```

## 3. IMPLEMENTATION EXAMPLES

### Database Performance Optimization

#### Query Optimization
```java
// ❌ Inefficient - loads all data then filters in memory
@Repository
public class SlowPickListRepository {
    public List<PickList> findActivePickListsForPicker(String pickerId) {
        List<PickList> allPickLists = repository.findAll();
        return allPickLists.stream()
            .filter(pl -> pl.getPickerId().equals(pickerId))
            .filter(pl -> pl.getStatus() == PickListStatus.ASSIGNED)
            .collect(Collectors.toList());
    }
}

// ✅ Optimized - database-level filtering with indexes
@Repository
public class OptimizedPickListRepository {

    @Query("{'pickerId': ?0, 'status': 'ASSIGNED'}")
    List<PickListDocument> findActiveByPickerId(String pickerId);

    // With projection for summary views
    @Query(value = "{'pickerId': ?0, 'status': 'ASSIGNED'}",
           fields = "{'id': 1, 'orderId': 1, 'createdAt': 1, 'instructionCount': 1}")
    List<PickListSummaryProjection> findActiveSummaryByPickerId(String pickerId);
}
```

#### Index Strategy
```java
@Document(collection = "pickListDocuments")
@CompoundIndex(name = "picker_status_idx", def = "{'pickerId': 1, 'status': 1}")
@CompoundIndex(name = "order_created_idx", def = "{'orderId': 1, 'createdAt': -1}")
@CompoundIndex(name = "status_created_idx", def = "{'status': 1, 'createdAt': -1}")
public class PickListDocument {

    @Id
    private String id;

    @Indexed // Single field index for frequent lookups
    private String pickerId;

    @Indexed
    private String status;

    // Add sparse index for optional fields
    @Indexed(sparse = true)
    private Instant completedAt;

    // Text index for search functionality
    @TextIndexed
    private String orderReference;
}
```

#### Connection Pool Optimization
```java
@Configuration
public class MongoPerformanceConfig {

    @Bean
    public MongoClientSettings mongoClientSettings() {
        return MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(mongoUri))
            .applyToConnectionPoolSettings(builder -> builder
                .maxSize(50)                    // Max connections
                .minSize(10)                    // Min connections maintained
                .maxWaitTime(30, TimeUnit.SECONDS)
                .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                .maxConnectionIdleTime(10, TimeUnit.MINUTES)
                .maintenanceInitialDelay(5, TimeUnit.SECONDS)
                .maintenanceFrequency(10, TimeUnit.SECONDS))
            .applyToSocketSettings(builder -> builder
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS))
            .build();
    }
}
```

### API Performance Optimization

#### Response Caching
```java
@RestController
@RequestMapping("/api/v1/picklists")
public class OptimizedPickListController {

    // Cache frequently accessed data
    @GetMapping("/{pickListId}")
    @Cacheable(value = "pickLists", key = "#pickListId")
    public ResponseEntity<PickListDto> getPickList(@PathVariable String pickListId) {
        PickList pickList = pickListService.findById(PickListId.of(pickListId));
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
            .body(PickListDto.fromDomain(pickList));
    }

    // Pagination for large datasets
    @GetMapping("/picker/{pickerId}")
    public ResponseEntity<Page<PickListSummaryDto>> getPickListsByPicker(
            @PathVariable String pickerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<PickListSummaryDto> pickLists = pickListService.findSummaryByPickerId(pickerId, pageable);
        return ResponseEntity.ok(pickLists);
    }

    // Async processing for heavy operations
    @PostMapping("/batch")
    public ResponseEntity<BatchOperationResult> processBatchOperation(
            @RequestBody BatchPickListRequest request) {

        String operationId = UUID.randomUUID().toString();

        // Process asynchronously
        CompletableFuture.supplyAsync(() ->
            batchPickListService.processBatch(request))
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    batchResultService.markFailed(operationId, throwable.getMessage());
                } else {
                    batchResultService.markCompleted(operationId, result);
                }
            });

        return ResponseEntity.accepted()
            .body(BatchOperationResult.accepted(operationId));
    }
}
```

#### DTO Optimization
```java
// ❌ Heavy DTO with unnecessary data
public class HeavyPickListDto {
    private String id;
    private OrderDto fullOrder; // Includes all order details
    private List<DetailedInstructionDto> instructions; // Full instruction objects
    private List<DomainEventDto> domainEvents; // Event history
    // ... many other heavy fields
}

// ✅ Lightweight summary DTO
public class PickListSummaryDto {
    private String id;
    private String orderId; // Just the ID, not full object
    private String status;
    private int totalInstructions;
    private int completedInstructions;
    private Instant createdAt;

    // Factory method for efficient creation
    public static PickListSummaryDto fromProjection(PickListSummaryProjection projection) {
        return PickListSummaryDto.builder()
            .id(projection.getId())
            .orderId(projection.getOrderId())
            .status(projection.getStatus())
            .totalInstructions(projection.getTotalInstructions())
            .completedInstructions(projection.getCompletedInstructions())
            .createdAt(projection.getCreatedAt())
            .build();
    }
}

// ✅ Detailed DTO only when needed
public class DetailedPickListDto extends PickListSummaryDto {
    private List<PickInstructionDto> instructions;
    private String pickerId;
    private Instant assignedAt;
    private Instant completedAt;

    // Lazy loading of heavy fields
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PickHistoryDto> pickHistory;
}
```

### Memory Management

#### Domain Event Management
```java
// ❌ Memory leak - events accumulate
public class LeakyPickListService {
    public void processPickList(PickListId id) {
        PickList pickList = repository.findById(id);
        pickList.complete();
        repository.save(pickList);
        // Events never cleared - memory leak!
    }
}

// ✅ Proper event cleanup
@Service
@Transactional
public class OptimizedPickListService {

    public void processPickList(PickListId id) {
        PickList pickList = repository.findById(id);
        pickList.complete();

        // Save first to ensure persistence
        repository.save(pickList);

        // Publish events
        List<DomainEvent> events = new ArrayList<>(pickList.getDomainEvents());
        eventPublisher.publishAll(events);

        // Clear events immediately
        pickList.clearDomainEvents();
    }

    // Batch processing with memory management
    public void processBatchPickLists(List<PickListId> ids) {
        int batchSize = 100;
        List<List<PickListId>> batches = Lists.partition(ids, batchSize);

        for (List<PickListId> batch : batches) {
            processBatch(batch);
            // Force garbage collection between batches if needed
            if (isMemoryPressureHigh()) {
                System.gc();
                Thread.sleep(100); // Brief pause
            }
        }
    }

    private boolean isMemoryPressureHigh() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return (double) heapUsage.getUsed() / heapUsage.getMax() > 0.8;
    }
}
```

#### Object Pool Pattern for Heavy Objects
```java
@Component
public class PickListValidatorPool {

    private final ObjectPool<PickListValidator> validatorPool;

    public PickListValidatorPool() {
        this.validatorPool = new GenericObjectPool<>(new PickListValidatorFactory());
        ((GenericObjectPool<PickListValidator>) validatorPool).setMaxTotal(10);
        ((GenericObjectPool<PickListValidator>) validatorPool).setMaxIdle(5);
    }

    public ValidationResult validatePickList(PickList pickList) {
        PickListValidator validator = null;
        try {
            validator = validatorPool.borrowObject();
            return validator.validate(pickList);
        } catch (Exception e) {
            throw new ValidationException("Failed to validate pick list", e);
        } finally {
            if (validator != null) {
                try {
                    validatorPool.returnObject(validator);
                } catch (Exception e) {
                    log.warn("Failed to return validator to pool", e);
                }
            }
        }
    }

    private static class PickListValidatorFactory implements PooledObjectFactory<PickListValidator> {
        @Override
        public PooledObject<PickListValidator> makeObject() {
            return new DefaultPooledObject<>(new PickListValidator());
        }

        @Override
        public void destroyObject(PooledObject<PickListValidator> p) {
            // Cleanup if needed
        }

        @Override
        public boolean validateObject(PooledObject<PickListValidator> p) {
            return p.getObject().isValid();
        }

        @Override
        public void activateObject(PooledObject<PickListValidator> p) {
            p.getObject().reset();
        }

        @Override
        public void passivateObject(PooledObject<PickListValidator> p) {
            p.getObject().cleanup();
        }
    }
}
```

### Event Processing Performance

#### Async Event Processing
```java
@Configuration
@EnableAsync
public class AsyncEventConfig {

    @Bean(name = "eventProcessingExecutor")
    public Executor eventProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("event-processing-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

@Component
public class AsyncEventHandlers {

    @EventListener
    @Async("eventProcessingExecutor")
    public void handlePickListCompleted(PickListCompletedEvent event) {
        // Non-blocking event processing
        packageService.createPackageFromPickList(event.getPickListId());
    }

    @EventListener
    @Async("eventProcessingExecutor")
    public void handleLicensePlateUpdated(LicensePlateMovedEvent event) {
        // Update location tracking asynchronously
        locationTrackingService.updateLocation(event);
    }
}
```

#### Kafka Performance Tuning
```yaml
spring:
  kafka:
    producer:
      batch-size: 16384
      linger-ms: 10  # Wait up to 10ms for batching
      compression-type: snappy
      buffer-memory: 33554432
      retries: 3
      properties:
        max.in.flight.requests.per.connection: 5
        enable.idempotence: true

    consumer:
      max-poll-records: 500
      fetch-min-size: 1
      fetch-max-wait: 500
      properties:
        max.partition.fetch.bytes: 1048576
        session.timeout.ms: 30000
        heartbeat.interval.ms: 3000

    listener:
      concurrency: 3  # Parallel consumers per topic
      poll-timeout: 3000
      ack-mode: batch
```

#### Event Batching
```java
@Component
public class BatchEventProcessor {

    private final Queue<DomainEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger queueSize = new AtomicInteger(0);

    public void queueEvent(DomainEvent event) {
        eventQueue.offer(event);
        int currentSize = queueSize.incrementAndGet();

        // Trigger batch processing if queue is full
        if (currentSize >= 100) {
            processBatch();
        }
    }

    @Scheduled(fixedDelay = 1000) // Process remaining events every second
    public void processPendingEvents() {
        if (!eventQueue.isEmpty()) {
            processBatch();
        }
    }

    private void processBatch() {
        List<DomainEvent> batch = new ArrayList<>();
        DomainEvent event;

        // Drain up to 100 events
        while (batch.size() < 100 && (event = eventQueue.poll()) != null) {
            batch.add(event);
            queueSize.decrementAndGet();
        }

        if (!batch.isEmpty()) {
            eventPublisher.publishBatch(batch);
        }
    }
}
```

## 4. TROUBLESHOOTING PERFORMANCE ISSUES

### Identifying Performance Bottlenecks

#### Database Query Analysis
```bash
# MongoDB slow query profiling
mongosh
use warehouse
db.setProfilingLevel(2, { slowms: 100 })

# Find slow queries
db.system.profile.find({
  "millis": { $gt: 100 }
}).sort({ "ts": -1 }).limit(10).pretty()

# Analyze specific query performance
db.pickListDocuments.find({
  "pickerId": "PICKER-001",
  "status": "ASSIGNED"
}).explain("executionStats")
```

#### JVM Performance Analysis
```bash
# Monitor JVM metrics
jstat -gc <PID> 1s 10  # GC statistics every second for 10 iterations
jstat -gccapacity <PID>  # Heap capacity information
jstat -gcutil <PID> 1s  # GC utilization

# Generate thread dump
jstack <PID> > thread-dump.txt

# Memory analysis
jmap -histo <PID> | head -20  # Histogram of objects
jmap -dump:live,format=b,file=heap-dump.hprof <PID>
```

#### Application Metrics Analysis
```java
@Component
public class PerformanceAnalyzer {

    @EventListener
    public void analyzeSlowOperation(SlowOperationEvent event) {
        if (event.getDuration() > Duration.ofMillis(500)) {
            log.warn("Slow operation detected: {} took {}ms",
                    event.getOperation(), event.getDuration().toMillis());

            // Collect additional metrics
            MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

            SlowOperationReport report = SlowOperationReport.builder()
                .operation(event.getOperation())
                .duration(event.getDuration())
                .heapUsagePercent((double) heapUsage.getUsed() / heapUsage.getMax() * 100)
                .activeThreadCount(threadBean.getThreadCount())
                .timestamp(Instant.now())
                .build();

            performanceReportService.recordSlowOperation(report);
        }
    }
}
```

### Performance Optimization Strategies

#### Query Optimization Checklist
1. **Index Coverage**: Ensure queries use appropriate indexes
2. **Projection**: Only fetch required fields
3. **Pagination**: Use limit/skip for large result sets
4. **Aggregation**: Use MongoDB aggregation pipeline for complex operations
5. **Connection Pooling**: Optimize connection pool settings

#### Memory Optimization Checklist
1. **Object Lifecycle**: Clear references when no longer needed
2. **Collection Sizing**: Initialize collections with expected size
3. **String Interning**: Use string constants for repeated values
4. **Lazy Loading**: Load expensive data only when needed
5. **Garbage Collection**: Monitor and tune GC settings

#### API Optimization Checklist
1. **Response Caching**: Cache frequently accessed data
2. **Compression**: Enable gzip compression
3. **Async Processing**: Use async for non-critical operations
4. **Batch Operations**: Group similar operations
5. **Circuit Breakers**: Prevent cascade failures

## 5. RELATED COMPONENTS

### Performance Monitoring Tools
- **Micrometer**: Application metrics collection
- **Spring Boot Actuator**: Built-in monitoring endpoints
- **Prometheus**: Metrics storage and querying
- **Grafana**: Performance dashboard visualization
- **New Relic/DataDog**: Application Performance Monitoring

### Profiling and Analysis Tools
- **JProfiler**: JVM profiling and memory analysis
- **Eclipse MAT**: Memory Analyzer Tool
- **VisualVM**: Java application profiler
- **Async Profiler**: Low-overhead profiling
- **MongoDB Compass**: Database query analysis

### Load Testing Tools
- **Apache JMeter**: HTTP load testing
- **Gatling**: High-performance load testing
- **Artillery**: Modern load testing toolkit
- **k6**: Developer-centric load testing

### Alternative Approaches
- **Reactive Programming**: Spring WebFlux for non-blocking I/O
- **Event Streaming**: Kafka Streams for real-time processing
- **CQRS**: Separate read/write models for optimization
- **Microservices**: Split into smaller, focused services
- **Caching Layers**: Redis, Hazelcast for distributed caching