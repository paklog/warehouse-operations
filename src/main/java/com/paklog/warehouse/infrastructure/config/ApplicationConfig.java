package com.paklog.warehouse.infrastructure.config;

import com.paklog.warehouse.infrastructure.events.DomainEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableMongoAuditing
public class ApplicationConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("warehouse-ops-");
        executor.initialize();
        return executor;
    }

    @Bean
    public DomainEventInterceptor domainEventInterceptor(DomainEventPublisher eventPublisher) {
        return new DomainEventInterceptor(eventPublisher);
    }
}