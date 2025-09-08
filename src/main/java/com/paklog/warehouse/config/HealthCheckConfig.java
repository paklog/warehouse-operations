package com.paklog.warehouse.config;

import org.springframework.context.annotation.Configuration;

/**
 * Health check configuration for warehouse operations.
 * 
 * Spring Boot Actuator automatically provides health indicators for:
 * - MongoDB (when spring-boot-starter-data-mongodb is present) 
 * - Kafka (when spring-kafka is present)
 * - Application status, disk space, etc.
 * 
 * These are automatically configured and available at /actuator/health
 * 
 * To enable health endpoint, add to application.properties:
 * management.endpoints.web.exposure.include=health,info
 * management.endpoint.health.show-details=when-authorized
 */
@Configuration
public class HealthCheckConfig {
    
    // Spring Boot Actuator auto-configuration handles health checks automatically
    // MongoDB health: checks connection to MongoDB
    // Kafka health: checks connection to Kafka brokers
    // Additional custom health indicators can be added here if needed
    
}