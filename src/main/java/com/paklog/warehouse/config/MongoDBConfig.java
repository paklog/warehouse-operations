package com.paklog.warehouse.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoRepositories(basePackages = {
    "com.paklog.warehouse.adapter.persistence.mongodb",
    "com.paklog.warehouse.infrastructure.messaging"
})
@EnableMongoAuditing(auditorAwareRef = "auditorProvider")
public class MongoDBConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/warehouse-operations}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:warehouse-operations}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(builder ->
                    builder.maxSize(100)
                           .minSize(5)
                           .maxConnectionIdleTime(30, TimeUnit.SECONDS)
                           .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                )
                .applyToSocketSettings(builder ->
                    builder.connectTimeout(5, TimeUnit.SECONDS)
                           .readTimeout(10, TimeUnit.SECONDS)
                )
                .applyToServerSettings(builder ->
                    builder.heartbeatFrequency(10, TimeUnit.SECONDS)
                           .minHeartbeatFrequency(5, TimeUnit.SECONDS)
                )
                .retryWrites(true)
                .retryReads(true)
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SimpleAuditorAware();
    }

    @Bean
    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
            // Add custom converters if needed
        ));
    }

    /**
     * Simple AuditorAware implementation - returns "system" for now
     * Can be enhanced with actual user context when authentication is added
     */
    public static class SimpleAuditorAware implements AuditorAware<String> {
        
        @Override
        public Optional<String> getCurrentAuditor() {
            // For now, return "system" - can be enhanced with actual user context
            // when Spring Security or other authentication mechanism is added
            return Optional.of("system");
        }
    }
}