package com.paklog.warehouse.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class KafkaEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String EVENT_SOURCE = "/fulfillment/warehouse-operations-service";
    private static final String TOPIC_NAME = "warehouse-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisher(
        KafkaTemplate<String, String> kafkaTemplate,
        OutboxRepository outboxRepository,
        ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByProcessedFalse();
        
        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                CloudEvent cloudEvent = createCloudEvent(outboxEvent);
                String serializedEvent = objectMapper.writeValueAsString(cloudEvent);
                
                kafkaTemplate.send(TOPIC_NAME, outboxEvent.getId().toString(), serializedEvent);
                
                // Mark event as processed
                outboxEvent.markProcessed();
                outboxRepository.save(outboxEvent);
                
                logger.info("Published event: {}", outboxEvent.getId());
            } catch (Exception e) {
                logger.error("Failed to publish event: {}", outboxEvent.getId(), e);
            }
        }
    }

    private CloudEvent createCloudEvent(OutboxEvent outboxEvent) {
        return CloudEventBuilder.v1()
            .withId(outboxEvent.getId().toString())
            .withType(outboxEvent.getType())
            .withSource(URI.create(EVENT_SOURCE))
            .withSubject(outboxEvent.getSubject())
            .withTime(OffsetDateTime.now())
            .withDataContentType("application/json")
            .withData(outboxEvent.getData().getBytes())
            .build();
    }

    @Transactional
    public void saveEventToOutbox(
        String type, 
        String subject, 
        String data
    ) {
        OutboxEvent outboxEvent = new OutboxEvent(
            type, 
            EVENT_SOURCE, 
            subject, 
            data
        );
        
        outboxRepository.save(outboxEvent);
    }
}