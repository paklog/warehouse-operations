package com.paklog.warehouse.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.boot.actuator.health.Status;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckConfig Tests")
class HealthCheckConfigTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ProducerFactory<String, String> producerFactory;

    private HealthCheckConfig healthCheckConfig;

    @BeforeEach
    void setUp() {
        healthCheckConfig = new HealthCheckConfig();
    }

    @Nested
    @DisplayName("MongoDB Health Indicator")
    class MongoDBHealthIndicator {

        @Test
        @DisplayName("Should create MongoDB health indicator")
        void shouldCreateMongoDbHealthIndicator() {
            // Act
            HealthIndicator indicator = healthCheckConfig.mongoHealthIndicator(mongoTemplate);

            // Assert
            assertThat(indicator).isNotNull();
            assertThat(indicator).isInstanceOf(HealthCheckConfig.MongoHealthIndicator.class);
        }

        @Test
        @DisplayName("Should report healthy when MongoDB connection is successful")
        void shouldReportHealthyWhenMongoDbConnectionIsSuccessful() {
            // Arrange
            when(mongoTemplate.getCollection("health_check")).thenReturn(mock(com.mongodb.client.MongoCollection.class));
            
            HealthCheckConfig.MongoHealthIndicator indicator = 
                new HealthCheckConfig.MongoHealthIndicator(mongoTemplate);

            // Act
            Health health = indicator.health();

            // Assert
            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("database", "MongoDB");
            assertThat(health.getDetails()).containsEntry("status", "Connected");
        }

        @Test
        @DisplayName("Should report unhealthy when MongoDB connection fails")
        void shouldReportUnhealthyWhenMongoDbConnectionFails() {
            // Arrange
            when(mongoTemplate.getCollection("health_check"))
                .thenThrow(new RuntimeException("Connection failed"));
            
            HealthCheckConfig.MongoHealthIndicator indicator = 
                new HealthCheckConfig.MongoHealthIndicator(mongoTemplate);

            // Act
            Health health = indicator.health();

            // Assert
            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("database", "MongoDB");
            assertThat(health.getDetails()).containsEntry("status", "Disconnected");
            assertThat(health.getDetails()).containsKey("error");
        }
    }

    @Nested
    @DisplayName("Kafka Health Indicator")
    class KafkaHealthIndicator {

        @Test
        @DisplayName("Should create Kafka health indicator")
        void shouldCreateKafkaHealthIndicator() {
            // Arrange
            String bootstrapServers = "localhost:9092";

            // Act
            HealthIndicator indicator = healthCheckConfig.kafkaHealthIndicator(kafkaTemplate, bootstrapServers);

            // Assert
            assertThat(indicator).isNotNull();
            assertThat(indicator).isInstanceOf(HealthCheckConfig.KafkaHealthIndicator.class);
        }

        @Test
        @DisplayName("Should report healthy when Kafka producer factory is available")
        void shouldReportHealthyWhenKafkaProducerFactoryIsAvailable() {
            // Arrange
            String bootstrapServers = "localhost:9092";
            when(kafkaTemplate.getProducerFactory()).thenReturn(producerFactory);
            
            HealthCheckConfig.KafkaHealthIndicator indicator = 
                new HealthCheckConfig.KafkaHealthIndicator(kafkaTemplate, bootstrapServers);

            // Act
            Health health = indicator.health();

            // Assert
            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("messaging", "Kafka");
            assertThat(health.getDetails()).containsEntry("bootstrap-servers", bootstrapServers);
            assertThat(health.getDetails()).containsEntry("status", "Connected");
        }

        @Test
        @DisplayName("Should report unhealthy when Kafka producer factory is unavailable")
        void shouldReportUnhealthyWhenKafkaProducerFactoryIsUnavailable() {
            // Arrange
            String bootstrapServers = "localhost:9092";
            when(kafkaTemplate.getProducerFactory()).thenReturn(null);
            
            HealthCheckConfig.KafkaHealthIndicator indicator = 
                new HealthCheckConfig.KafkaHealthIndicator(kafkaTemplate, bootstrapServers);

            // Act
            Health health = indicator.health();

            // Assert
            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("messaging", "Kafka");
            assertThat(health.getDetails()).containsEntry("bootstrap-servers", bootstrapServers);
            assertThat(health.getDetails()).containsEntry("status", "Producer factory unavailable");
        }

        @Test
        @DisplayName("Should report unhealthy when Kafka template throws exception")
        void shouldReportUnhealthyWhenKafkaTemplateThrowsException() {
            // Arrange
            String bootstrapServers = "localhost:9092";
            when(kafkaTemplate.getProducerFactory()).thenThrow(new RuntimeException("Kafka connection failed"));
            
            HealthCheckConfig.KafkaHealthIndicator indicator = 
                new HealthCheckConfig.KafkaHealthIndicator(kafkaTemplate, bootstrapServers);

            // Act
            Health health = indicator.health();

            // Assert
            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("messaging", "Kafka");
            assertThat(health.getDetails()).containsEntry("bootstrap-servers", bootstrapServers);
            assertThat(health.getDetails()).containsEntry("status", "Disconnected");
            assertThat(health.getDetails()).containsKey("error");
        }
    }
}