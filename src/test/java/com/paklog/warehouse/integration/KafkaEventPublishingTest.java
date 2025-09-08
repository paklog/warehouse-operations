package com.paklog.warehouse.integration;

import com.paklog.warehouse.infrastructure.messaging.KafkaEventPublisher;
import com.paklog.warehouse.infrastructure.messaging.OutboxEvent;
import com.paklog.warehouse.infrastructure.messaging.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
class KafkaEventPublishingTest {
    @Autowired
    private KafkaEventPublisher kafkaEventPublisher;

    @Autowired
    private OutboxRepository outboxRepository;

    @BeforeEach
    void setUp() {
        // Clear any existing events in the outbox
        outboxRepository.deleteAll();
    }

    @Test
    void testSaveEventToOutbox() {
        // Arrange
        String eventType = "com.paklog.warehouse.package.created";
        String subject = "package-123";
        String eventData = "{\"packageId\":\"package-123\",\"orderId\":\"order-456\"}";

        // Act
        kafkaEventPublisher.saveEventToOutbox(eventType, subject, eventData);

        // Assert
        List<OutboxEvent> pendingEvents = outboxRepository.findByProcessedFalse();
        assertEquals(1, pendingEvents.size());
        
        OutboxEvent savedEvent = pendingEvents.get(0);
        assertEquals(eventType, savedEvent.getType());
        assertEquals(subject, savedEvent.getSubject());
        assertEquals(eventData, savedEvent.getData());
        assertFalse(savedEvent.isProcessed());
    }

    @Test
    void testPublishPendingEvents() {
        // Arrange
        String eventType = "com.paklog.warehouse.package.created";
        String subject = "package-123";
        String eventData = "{\"packageId\":\"package-123\",\"orderId\":\"order-456\"}";

        // Save event to outbox
        kafkaEventPublisher.saveEventToOutbox(eventType, subject, eventData);

        // Act
        kafkaEventPublisher.publishPendingEvents();

        // Assert
        List<OutboxEvent> pendingEvents = outboxRepository.findByProcessedFalse();
        assertTrue(pendingEvents.isEmpty());

        List<OutboxEvent> processedEvents = outboxRepository.findAll().stream()
            .filter(OutboxEvent::isProcessed)
            .collect(Collectors.toList());
        assertEquals(1, processedEvents.size());
    }

    @Test
    void testPublishEventsWithEmptyOutbox() {
        // Act
        kafkaEventPublisher.publishPendingEvents();

        // Assert
        // No exceptions should be thrown
        List<OutboxEvent> events = outboxRepository.findAll();
        assertTrue(events.isEmpty());
    }

    @Test
    void testMultipleEventPublishing() {
        // Arrange
        kafkaEventPublisher.saveEventToOutbox(
            "com.paklog.warehouse.package.created", 
            "package-123", 
            "{\"packageId\":\"package-123\",\"orderId\":\"order-456\"}"
        );
        kafkaEventPublisher.saveEventToOutbox(
            "com.paklog.warehouse.package.shipped", 
            "package-456", 
            "{\"packageId\":\"package-456\",\"orderId\":\"order-789\"}"
        );

        // Act
        kafkaEventPublisher.publishPendingEvents();

        // Assert
        List<OutboxEvent> pendingEvents = outboxRepository.findByProcessedFalse();
        assertTrue(pendingEvents.isEmpty());

        List<OutboxEvent> processedEvents = outboxRepository.findAll().stream()
            .filter(OutboxEvent::isProcessed)
            .collect(Collectors.toList());
        assertEquals(2, processedEvents.size());
    }
}