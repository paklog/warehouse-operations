package com.paklog.warehouse.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;
import java.util.Optional;

@TestConfiguration
@EnableMongoAuditing(auditorAwareRef = "testAuditorProvider")
public class TestMongoDBConfig {

    @Bean
    @Primary
    public AuditorAware<String> testAuditorProvider() {
        return new TestAuditorAware();
    }

    @Bean
    @Primary
    public MongoCustomConversions testCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
            // Add custom converters if needed for testing
        ));
    }

    /**
     * Test AuditorAware implementation
     */
    public static class TestAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            return Optional.of("test-user");
        }
    }
}