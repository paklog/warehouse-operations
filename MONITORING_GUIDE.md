# Warehouse Operations Service - Monitoring Guide

## Monitoring Architecture

### Observability Stack
- **Metrics**: Prometheus
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing**: Jaeger
- **Dashboarding**: Grafana

## Prometheus Metrics Configuration

### Key Metrics to Monitor

#### JVM Metrics
- Memory usage
- Garbage collection
- Thread states
- CPU utilization

#### Application-Specific Metrics
```java
@Component
public class WarehouseMetrics {
    private final Counter pickListsCreated;
    private final Counter packagesProcessed;
    private final Histogram pickRouteOptimizationTime;

    public WarehouseMetrics(MeterRegistry registry) {
        pickListsCreated = Counter.builder("warehouse_picklists_created_total")
            .description("Total number of pick lists created")
            .register(registry);

        packagesProcessed = Counter.builder("warehouse_packages_processed_total")
            .description("Total number of packages processed")
            .register(registry);

        pickRouteOptimizationTime = Histogram.builder("warehouse_route_optimization_seconds")
            .description("Pick route optimization processing time")
            .register(registry);
    }
}
```

### Prometheus Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'warehouse-operations'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']

  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka:7071']

  - job_name: 'mongodb'
    static_configs:
      - targets: ['mongodb:9216']
```

## Logging Configuration

### ELK Stack Integration
```yaml
# logback-spring.xml
<configuration>
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>logstash:5044</destination>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <message/>
                <logLevel/>
                <threadName/>
                <loggerName/>
                <mdc/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="LOGSTASH"/>
    </root>
</configuration>
```

## Distributed Tracing

### OpenTelemetry Configuration
```yaml
# application.yml
opentelemetry:
  traces:
    sampler:
      probability: 0.1  # Sample 10% of traces
  exporter:
    jaeger:
      endpoint: http://jaeger:14250
```

## Alerting Strategies

### Critical Alerts
1. High error rates
2. Increased latency
3. Resource exhaustion
4. Event processing failures

### Alert Rules (Prometheus Alertmanager)
```yaml
groups:
- name: warehouse_alerts
  rules:
  - alert: HighErrorRate
    expr: sum(rate(http_server_requests_total{status=~"5.."}[5m])) / sum(rate(http_server_requests_total[5m])) > 0.05
    for: 10m
    labels:
      severity: critical
    annotations:
      summary: "High error rate detected"
      description: "Error rate exceeded 5% for 10 minutes"

  - alert: PickRouteOptimizationSlow
    expr: histogram_quantile(0.99, rate(warehouse_route_optimization_seconds_bucket[5m])) > 2
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "Slow pick route optimization"
      description: "99th percentile route optimization time exceeds 2 seconds"
```

## Monitoring Dashboard (Grafana)

### Dashboard Panels
1. Pick List Creation Rate
2. Package Processing Metrics
3. Route Optimization Performance
4. Event Processing Latency
5. System Resource Utilization
6. Error Rate Trends

## Best Practices
- Implement comprehensive logging
- Use structured logging
- Configure appropriate log levels
- Implement correlation IDs
- Monitor system and application metrics
- Set up proactive alerting

## Troubleshooting
- Check correlation IDs for tracing
- Analyze metrics and logs together
- Use distributed tracing for complex workflows

## Contact and Support
- Monitoring Team: monitoring@paklog.com
- Slack: #warehouse-ops-monitoring