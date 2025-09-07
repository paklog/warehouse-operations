package com.paklog.warehouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class HealthCheckConfig {

    @Bean
    public HealthIndicator mongoHealthIndicator(MongoTemplate mongoTemplate) {
        return new MongoHealthIndicator(mongoTemplate);
    }

    @Bean
    public HealthIndicator kafkaHealthIndicator(KafkaTemplate<String, String> kafkaTemplate,
                                               @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaHealthIndicator(kafkaTemplate, bootstrapServers);
    }

    public static class MongoHealthIndicator implements HealthIndicator {
        private final MongoTemplate mongoTemplate;

        public MongoHealthIndicator(MongoTemplate mongoTemplate) {
            this.mongoTemplate = mongoTemplate;
        }

        @Override
        public Health health() {
            try {
                // Simple ping to check MongoDB connectivity
                mongoTemplate.getCollection("health_check").estimatedDocumentCount();
                return Health.up()
                    .withDetail("database", "MongoDB")
                    .withDetail("status", "Connected")
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withDetail("database", "MongoDB")
                    .withDetail("status", "Disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
            }
        }
    }

    public static class KafkaHealthIndicator implements HealthIndicator {
        private final KafkaTemplate<String, String> kafkaTemplate;
        private final String bootstrapServers;

        public KafkaHealthIndicator(KafkaTemplate<String, String> kafkaTemplate, String bootstrapServers) {
            this.kafkaTemplate = kafkaTemplate;
            this.bootstrapServers = bootstrapServers;
        }

        @Override
        public Health health() {
            try {
                // Check if Kafka producer is available
                if (kafkaTemplate.getProducerFactory() != null) {
                    return Health.up()
                        .withDetail("messaging", "Kafka")
                        .withDetail("bootstrap-servers", bootstrapServers)
                        .withDetail("status", "Connected")
                        .build();
                } else {
                    return Health.down()
                        .withDetail("messaging", "Kafka")
                        .withDetail("bootstrap-servers", bootstrapServers)
                        .withDetail("status", "Producer factory unavailable")
                        .build();
                }
            } catch (Exception e) {
                return Health.down()
                    .withDetail("messaging", "Kafka")
                    .withDetail("bootstrap-servers", bootstrapServers)
                    .withDetail("status", "Disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
            }
        }
    }
}