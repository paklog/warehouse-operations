package com.paklog.warehouse.infrastructure.events;

import com.paklog.warehouse.domain.shared.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DomainEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(DomainEventHandler.class);

    // Work Domain Events
    @EventListener
    @Async
    public void handleWorkAssignedEvent(WorkAssignedEvent event) {
        logger.info("Handling WorkAssignedEvent: Work {} assigned to {}", 
                event.getWorkId(), event.getAssignedTo());
        // Handle work assignment logic (notifications, metrics, etc.)
    }

    @EventListener
    @Async
    public void handleWorkCompletedEvent(WorkCompletedEvent event) {
        logger.info("Handling WorkCompletedEvent: Work {} completed by {} at {}", 
                event.getWorkId(), event.getCompletedBy(), event.getCompletedAt());
        // Handle work completion logic (metrics, notifications, downstream processes)
    }

    @EventListener
    @Async
    public void handleWorkStartedEvent(WorkStartedEvent event) {
        logger.info("Handling WorkStartedEvent: Work {} started by {} at {}", 
                event.getWorkId(), event.getStartedBy(), event.getStartedAt());
        // Handle work start logic (tracking, notifications)
    }

    @EventListener
    @Async
    public void handleWorkCancelledEvent(WorkCancelledEvent event) {
        logger.info("Handling WorkCancelledEvent: Work {} cancelled - {}", 
                event.getWorkId(), event.getReason());
        // Handle work cancellation logic (resource release, notifications)
    }

    // Wave Domain Events
    @EventListener
    @Async
    public void handleWaveReleasedEvent(WaveReleasedEvent event) {
        logger.info("Handling WaveReleasedEvent: Wave {} with {} orders released at {}", 
                event.getWaveId(), event.getOrderCount(), event.getReleaseDate());
        // Handle wave release logic (work generation, resource allocation)
    }

    @EventListener
    @Async
    public void handleWaveClosedEvent(WaveClosedEvent event) {
        logger.info("Handling WaveClosedEvent: Wave {} closed at {}", 
                event.getWaveId(), event.getClosedDate());
        // Handle wave closure logic (metrics collection, cleanup)
    }

    @EventListener
    @Async
    public void handleWaveCancelledEvent(WaveCancelledEvent event) {
        logger.info("Handling WaveCancelledEvent: Wave {} cancelled at {}", 
                event.getWaveId(), event.getCancelledAt());
        // Handle wave cancellation logic (resource release, order reallocation)
    }

    // Generic domain event handler for logging and monitoring
    @EventListener
    @Async
    public void handleGenericDomainEvent(DomainEvent event) {
        logger.debug("Generic domain event handled: {} with ID: {} at {}", 
                event.getClass().getSimpleName(), event.getEventId(), event.getOccurredAt());
        
        // Could add generic monitoring, metrics collection, or audit logging here
        recordEventMetrics(event);
    }

    private void recordEventMetrics(DomainEvent event) {
        // Placeholder for metrics recording
        logger.debug("Recording metrics for event: {}", event.getClass().getSimpleName());
    }
}