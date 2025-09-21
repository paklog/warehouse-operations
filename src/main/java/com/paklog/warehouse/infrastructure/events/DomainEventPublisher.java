package com.paklog.warehouse.infrastructure.events;

import com.paklog.warehouse.domain.shared.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DomainEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(DomainEventPublisher.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventSerializer eventSerializer;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher,
                               KafkaTemplate<String, Object> kafkaTemplate,
                               EventSerializer eventSerializer) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.kafkaTemplate = kafkaTemplate;
        this.eventSerializer = eventSerializer;
    }

    public void publish(DomainEvent event) {
        publishEvent(event);
    }

    public void publishEvent(DomainEvent event) {
        logger.debug("Publishing domain event: {}", event.getClass().getSimpleName());
        
        try {
            // Publish locally first for immediate consistency
            applicationEventPublisher.publishEvent(event);
            
            // Then publish to Kafka for eventual consistency and integration
            publishToKafka(event);
            
            logger.info("Successfully published domain event: {} with ID: {}", 
                    event.getClass().getSimpleName(), event.getEventId());
        } catch (Exception e) {
            logger.error("Failed to publish domain event: {} with ID: {}", 
                    event.getClass().getSimpleName(), event.getEventId(), e);
            throw new EventPublishingException("Failed to publish domain event", e);
        }
    }

    public void publishEvents(List<DomainEvent> events) {
        logger.debug("Publishing {} domain events", events.size());
        
        for (DomainEvent event : events) {
            publishEvent(event);
        }
        
        logger.info("Successfully published {} domain events", events.size());
    }

    private void publishToKafka(DomainEvent event) {
        try {
            String topicName = getTopicName(event);
            String eventKey = event.getEventId().toString();
            String serializedEvent = eventSerializer.serialize(event);
            
            kafkaTemplate.send(topicName, eventKey, serializedEvent)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            logger.debug("Event sent to Kafka successfully: {} to topic: {}", 
                                    event.getClass().getSimpleName(), topicName);
                        } else {
                            logger.error("Failed to send event to Kafka: {} to topic: {}", 
                                    event.getClass().getSimpleName(), topicName, ex);
                        }
                    });
                    
        } catch (Exception e) {
            logger.error("Error publishing event to Kafka: {}", event.getClass().getSimpleName(), e);
            throw new KafkaPublishingException("Failed to publish event to Kafka", e);
        }
    }

    private String getTopicName(DomainEvent event) {
        // Convert event class name to topic name (e.g., WorkCompletedEvent -> work-completed)
        String className = event.getClass().getSimpleName();
        if (className.endsWith("Event")) {
            className = className.substring(0, className.length() - 5); // Remove "Event" suffix
        }
        
        // Convert CamelCase to kebab-case
        return className.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }

    public static class EventPublishingException extends RuntimeException {
        public EventPublishingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class KafkaPublishingException extends RuntimeException {
        public KafkaPublishingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}