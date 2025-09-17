# Configuration Guide

## Application Configuration

### Environment-Specific Configuration
The application uses Spring profiles to manage different environments:

#### Development Environment
```yaml
# application-dev.yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/warehouse-dev
      database: warehouse-dev
  kafka:
    bootstrap-servers: localhost:9092

warehouse:
  picking:
    route-optimization:
      enabled: true
      strategy: continuous
  packing:
    validation:
      strict-mode: false  # Relaxed for development

logging:
  level:
    com.paklog.warehouse: DEBUG
    org.springframework.kafka: INFO
```

#### Production Environment
```yaml
# application-prod.yml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      database: ${MONGODB_DATABASE}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    producer:
      retries: 3
      retry-backoff-ms: 1000
    consumer:
      enable-auto-commit: false

warehouse:
  picking:
    route-optimization:
      enabled: true
      strategy: ${PICKING_STRATEGY:continuous}
  packing:
    validation:
      strict-mode: true

logging:
  level:
    root: INFO
    com.paklog.warehouse: INFO
```

### Database Configuration
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://username:password@host:port/database?authSource=admin
      database: warehouse

# Connection pool settings
mongodb:
  connection-pool:
    max-size: 50
    min-size: 10
    max-wait-time: 30000
    max-connection-life-time: 1800000
    max-connection-idle-time: 600000
```

### Kafka Configuration
```yaml
spring:
  kafka:
    bootstrap-servers: kafka1:9092,kafka2:9092,kafka3:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      batch-size: 16384
      linger-ms: 10
      compression-type: snappy
    consumer:
      group-id: warehouse-operations-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      max-poll-records: 500
      properties:
        spring.json.trusted.packages: "com.paklog.warehouse.domain"

cloudevents:
  kafka:
    topic: warehouse-events
    source: /fulfillment/warehouse-operations-service
```

### Security Configuration
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI}
          jwk-set-uri: ${JWT_JWK_SET_URI}

warehouse:
  security:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
      allowed-methods: GET,POST,PUT,DELETE,PATCH
      allowed-headers: "*"
      allow-credentials: true
```

## Custom Configuration Properties

### Warehouse-Specific Properties
```java
@ConfigurationProperties(prefix = "warehouse")
@Data
@Component
public class WarehouseProperties {

    private Picking picking = new Picking();
    private Packing packing = new Packing();
    private Quality quality = new Quality();
    private Mobile mobile = new Mobile();

    @Data
    public static class Picking {
        private RouteOptimization routeOptimization = new RouteOptimization();
        private Assignment assignment = new Assignment();

        @Data
        public static class RouteOptimization {
            private boolean enabled = true;
            private String strategy = "continuous";
            private int maxPicksPerRoute = 50;
            private Duration maxRouteTime = Duration.ofHours(2);
        }

        @Data
        public static class Assignment {
            private String algorithm = "balanced";
            private int maxPickListsPerPicker = 5;
            private boolean autoAssignment = true;
        }
    }

    @Data
    public static class Packing {
        private Validation validation = new Validation();
        private Optimization optimization = new Optimization();

        @Data
        public static class Validation {
            private boolean strictMode = true;
            private boolean requireWeightConfirmation = false;
            private boolean requireDimensionMeasurement = false;
        }

        @Data
        public static class Optimization {
            private boolean enabled = true;
            private String packingAlgorithm = "best-fit";
            private int maxItemsPerPackage = 20;
        }
    }

    @Data
    public static class Quality {
        private Inspection inspection = new Inspection();

        @Data
        public static class Inspection {
            private boolean autoTrigger = true;
            private double highValueThreshold = 1000.0;
            private int samplingPercentage = 5;
            private Duration maxInspectionTime = Duration.ofMinutes(15);
        }
    }

    @Data
    public static class Mobile {
        private Scanner scanner = new Scanner();
        private Notification notification = new Notification();

        @Data
        public static class Scanner {
            private boolean strictLocationValidation = true;
            private int scanTimeoutSeconds = 30;
            private boolean allowManualEntry = false;
        }

        @Data
        public static class Notification {
            private boolean enabled = true;
            private String provider = "websocket";
            private Duration retryInterval = Duration.ofSeconds(30);
        }
    }
}
```

## Environment Variables

### Required Environment Variables
```bash
# Database
MONGODB_URI=mongodb://user:password@mongo-cluster:27017/warehouse?authSource=admin
MONGODB_DATABASE=warehouse

# Messaging
KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092,kafka3:9092

# Security
JWT_ISSUER_URI=https://auth.company.com/auth/realms/warehouse
JWT_JWK_SET_URI=https://auth.company.com/auth/realms/warehouse/protocol/openid-connect/certs

# Application
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
```

### Optional Environment Variables
```bash
# Performance Tuning
JVM_HEAP_SIZE=2g
JVM_NEW_SIZE=512m
GC_ALGORITHM=G1GC

# Monitoring
METRICS_EXPORT_ENABLED=true
PROMETHEUS_ENDPOINT_ENABLED=true

# Features
PICKING_STRATEGY=continuous
PACKING_STRICT_MODE=true
QUALITY_AUTO_INSPECTION=true

# External Services
INVENTORY_SERVICE_URL=http://inventory-service:8080
SHIPPING_SERVICE_URL=http://shipping-service:8080

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://warehouse-ui.company.com,https://mobile-app.company.com
```

## Docker Configuration

### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

# Add application user
RUN addgroup --system warehouse && adduser --system --group warehouse

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/warehouse-operations-*.jar warehouse-operations.jar

# Change ownership
RUN chown -R warehouse:warehouse /app

# Switch to application user
USER warehouse

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM tuning
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar warehouse-operations.jar"]
```

### Docker Compose
```yaml
version: '3.8'

services:
  warehouse-operations:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - MONGODB_URI=mongodb://warehouse-mongo:27017/warehouse
      - KAFKA_BOOTSTRAP_SERVERS=warehouse-kafka:9092
    depends_on:
      warehouse-mongo:
        condition: service_healthy
      warehouse-kafka:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    volumes:
      - ./logs:/app/logs
    networks:
      - warehouse-network

  warehouse-mongo:
    image: mongo:6.0
    ports:
      - "27017:27017"
    environment:
      - MONGO_INITDB_ROOT_USERNAME=admin
      - MONGO_INITDB_ROOT_PASSWORD=password
      - MONGO_INITDB_DATABASE=warehouse
    volumes:
      - mongo-data:/data/db
      - ./mongo-init:/docker-entrypoint-initdb.d
    healthcheck:
      test: echo 'db.runCommand("ping").ok' | mongosh localhost:27017/test --quiet
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - warehouse-network

  warehouse-kafka:
    image: confluentinc/cp-kafka:7.4.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: warehouse-zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://warehouse-kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    volumes:
      - kafka-data:/var/lib/kafka/data
    depends_on:
      - warehouse-zookeeper
    healthcheck:
      test: kafka-broker-api-versions --bootstrap-server localhost:9092
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - warehouse-network

  warehouse-zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    volumes:
      - zookeeper-data:/var/lib/zookeeper/data
    networks:
      - warehouse-network

volumes:
  mongo-data:
  kafka-data:
  zookeeper-data:

networks:
  warehouse-network:
    driver: bridge
```

## Kubernetes Configuration

### Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-operations
  labels:
    app: warehouse-operations
spec:
  replicas: 3
  selector:
    matchLabels:
      app: warehouse-operations
  template:
    metadata:
      labels:
        app: warehouse-operations
    spec:
      containers:
      - name: warehouse-operations
        image: warehouse-operations:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: warehouse-secrets
              key: mongodb-uri
        - name: KAFKA_BOOTSTRAP_SERVERS
          valueFrom:
            configMapKeyRef:
              name: warehouse-config
              key: kafka-bootstrap-servers
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
      volumes:
      - name: config-volume
        configMap:
          name: warehouse-config
      - name: logs-volume
        emptyDir: {}
```

### Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: warehouse-operations-service
spec:
  selector:
    app: warehouse-operations
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
  type: ClusterIP
```

### ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: warehouse-config
data:
  application-kubernetes.yml: |
    spring:
      data:
        mongodb:
          uri: ${MONGODB_URI}
      kafka:
        bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    warehouse:
      picking:
        route-optimization:
          enabled: true
          strategy: continuous
    logging:
      level:
        com.paklog.warehouse: INFO
  kafka-bootstrap-servers: "kafka-service:9092"
```

### Secret
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: warehouse-secrets
type: Opaque
stringData:
  mongodb-uri: "mongodb://user:password@mongo-service:27017/warehouse?authSource=admin"
  jwt-secret: "your-jwt-secret-key"
```

## Monitoring Configuration

### Actuator Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include:
          - health
          - info
          - metrics
          - prometheus
          - loggers
          - configprops
          - env
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: always
    metrics:
      enabled: true
  health:
    mongo:
      enabled: true
    kafka:
      enabled: true
    diskspace:
      enabled: true
      threshold: 10GB
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

### Logging Configuration
```yaml
logging:
  level:
    root: INFO
    com.paklog.warehouse: INFO
    org.springframework.data.mongodb: WARN
    org.springframework.kafka: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
  file:
    name: /app/logs/warehouse-operations.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 1GB
  logback:
    rollingpolicy:
      file-name-pattern: /app/logs/warehouse-operations.%d{yyyy-MM-dd}.%i.log.gz
      max-file-size: 100MB
      max-history: 30
```

## Security Configuration

### JWT Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/api/v1/picklists/**").hasRole("PICKER")
                .requestMatchers("/api/v1/packages/**").hasRole("PACKER")
                .requestMatchers("/api/v1/mobile/**").hasRole("MOBILE_USER")
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(corsAllowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

This configuration guide provides comprehensive setup instructions for all environments and deployment scenarios.