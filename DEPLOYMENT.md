# Warehouse Operations Service - Deployment Guide

## Prerequisites

- Java 17+
- MongoDB 4.4+
- Apache Kafka 2.8+
- Docker (optional)

## Configuration

### Environment Variables

| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/warehouse` | No |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` | No |
| `SPRING_PROFILES_ACTIVE` | Spring active profile | `default` | No |

### Application Configuration

Key configuration options are available in `application.yml`:

#### MongoDB Configuration
- `spring.data.mongodb.uri`: Connection string for MongoDB
- `spring.data.mongodb.database`: Database name

#### Kafka Configuration
- `spring.kafka.bootstrap-servers`: Kafka broker addresses
- `spring.kafka.producer.*`: Kafka producer settings
- `spring.kafka.consumer.*`: Kafka consumer settings

#### Logging Configuration
- `logging.level.root`: Root logging level
- `logging.level.com.paklog.warehouse`: Package-specific logging

#### Warehouse-Specific Configuration
- `warehouse.picking.route-optimization.enabled`: Enable/disable route optimization
- `warehouse.picking.route-optimization.strategy`: Picking route optimization strategy
- `warehouse.packing.validation.strict-mode`: Enable strict packing validation

## Deployment Options

### Local Development

1. Ensure prerequisites are installed
2. Set environment variables (optional)
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Docker Deployment

1. Build the Docker image:
   ```bash
   ./mvnw spring-boot:build-image
   ```

2. Run the Docker container:
   ```bash
   docker run -p 8080:8080 \
     -e MONGODB_URI=mongodb://mongodb:27017/warehouse \
     -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
     warehouse-operations:latest
   ```

### Kubernetes Deployment

Sample Kubernetes deployment manifest:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-operations
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: warehouse-operations
        image: warehouse-operations:latest
        env:
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: mongodb-credentials
              key: connection-string
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: kafka-broker:9092
        ports:
        - containerPort: 8080
```

## Monitoring and Observability

The service exposes the following actuator endpoints:
- `/actuator/health`: Service health status
- `/actuator/metrics`: Prometheus metrics
- `/actuator/info`: Service information

## Troubleshooting

- Check logs for detailed error information
- Verify MongoDB and Kafka connectivity
- Ensure all required environment variables are set

## API Documentation

- OpenAPI Specification: `openapi/warehouse-operations-api.yaml`
- AsyncAPI Specification: `asyncapi/warehouse-events.yaml`

## Performance Considerations

- Adjust Kafka consumer/producer settings based on load
- Monitor MongoDB connection pool
- Use connection pooling for external services

## Security

- Use environment-specific credentials
- Enable encryption for Kafka and MongoDB connections
- Implement network-level security restrictions

## Scaling

- Horizontal scaling supported via stateless design
- Use Kubernetes HorizontalPodAutoscaler for dynamic scaling