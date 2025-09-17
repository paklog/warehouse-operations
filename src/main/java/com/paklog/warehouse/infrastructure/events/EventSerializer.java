package com.paklog.warehouse.infrastructure.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paklog.warehouse.domain.shared.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventSerializer {
    private static final Logger logger = LoggerFactory.getLogger(EventSerializer.class);
    
    private final ObjectMapper objectMapper;

    public EventSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.findAndRegisterModules();
    }

    public String serialize(DomainEvent event) {
        try {
            EventWrapper wrapper = new EventWrapper(
                    event.getClass().getName(),
                    event.getEventId().toString(),
                    event.getOccurredAt(),
                    event
            );
            
            String serialized = objectMapper.writeValueAsString(wrapper);
            logger.debug("Serialized event: {} with ID: {}", event.getClass().getSimpleName(), event.getEventId());
            return serialized;
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event: {} with ID: {}", 
                    event.getClass().getSimpleName(), event.getEventId(), e);
            throw new EventSerializationException("Failed to serialize domain event", e);
        }
    }

    public DomainEvent deserialize(String serializedEvent) {
        try {
            EventWrapper wrapper = objectMapper.readValue(serializedEvent, EventWrapper.class);
            
            Class<?> eventClass = Class.forName(wrapper.getEventType());
            DomainEvent event = (DomainEvent) objectMapper.convertValue(wrapper.getPayload(), eventClass);
            
            logger.debug("Deserialized event: {} with ID: {}", eventClass.getSimpleName(), wrapper.getEventId());
            return event;
            
        } catch (JsonProcessingException | ClassNotFoundException e) {
            logger.error("Failed to deserialize event: {}", serializedEvent, e);
            throw new EventDeserializationException("Failed to deserialize domain event", e);
        }
    }

    public static class EventSerializationException extends RuntimeException {
        public EventSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EventDeserializationException extends RuntimeException {
        public EventDeserializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Inner class for event wrapper
    private static class EventWrapper {
        private String eventType;
        private String eventId;
        private java.time.Instant occurredAt;
        private Object payload;

        public EventWrapper() {}

        public EventWrapper(String eventType, String eventId, java.time.Instant occurredAt, Object payload) {
            this.eventType = eventType;
            this.eventId = eventId;
            this.occurredAt = occurredAt;
            this.payload = payload;
        }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public java.time.Instant getOccurredAt() { return occurredAt; }
        public void setOccurredAt(java.time.Instant occurredAt) { this.occurredAt = occurredAt; }

        public Object getPayload() { return payload; }
        public void setPayload(Object payload) { this.payload = payload; }
    }
}