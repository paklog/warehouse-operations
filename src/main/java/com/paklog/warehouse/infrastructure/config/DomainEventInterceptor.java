package com.paklog.warehouse.infrastructure.config;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.infrastructure.events.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DomainEventInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(DomainEventInterceptor.class);
    
    private final DomainEventPublisher eventPublisher;

    public DomainEventInterceptor(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    // Simplified version without AspectJ - to be enhanced later with proper AOP setup
    public void publishEventsAfterSave(AggregateRoot aggregateRoot) {
        logger.debug("Publishing events for aggregate: {}", aggregateRoot.getClass().getSimpleName());
        
        if (!aggregateRoot.getUncommittedEvents().isEmpty()) {
            int eventCount = aggregateRoot.getUncommittedEvents().size();
            eventPublisher.publishEvents(aggregateRoot.getUncommittedEvents());
            aggregateRoot.clearEvents();
            
            logger.info("Published {} events for aggregate: {}", 
                    eventCount, aggregateRoot.getClass().getSimpleName());
        }
    }
}