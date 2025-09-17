package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// Quality Corrective Action Created Event
class QualityCorrectiveActionCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityCorrectiveActionId actionId;
    private final QualityNonConformanceId nonConformanceId;
    private final String description;
    private final QualityCorrectiveActionType actionType;
    private final QualityCorrectiveActionPriority priority;
    private final String assignedTo;
    private final Instant assignedAt;
    private final Instant dueDate;

    public QualityCorrectiveActionCreatedEvent(QualityCorrectiveActionId actionId, 
                                             QualityNonConformanceId nonConformanceId,
                                             String description, QualityCorrectiveActionType actionType,
                                             QualityCorrectiveActionPriority priority,
                                             String assignedTo, Instant assignedAt, Instant dueDate) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.actionId = actionId;
        this.nonConformanceId = nonConformanceId;
        this.description = description;
        this.actionType = actionType;
        this.priority = priority;
        this.assignedTo = assignedTo;
        this.assignedAt = assignedAt;
        this.dueDate = dueDate;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityCorrectiveActionId getActionId() { return actionId; }
    public QualityNonConformanceId getNonConformanceId() { return nonConformanceId; }
    public String getDescription() { return description; }
    public QualityCorrectiveActionType getActionType() { return actionType; }
    public QualityCorrectiveActionPriority getPriority() { return priority; }
    public String getAssignedTo() { return assignedTo; }
    public Instant getAssignedAt() { return assignedAt; }
    public Instant getDueDate() { return dueDate; }
}

// Quality Corrective Action Started Event
class QualityCorrectiveActionStartedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityCorrectiveActionId actionId;
    private final Instant startedAt;

    public QualityCorrectiveActionStartedEvent(QualityCorrectiveActionId actionId, Instant startedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.actionId = actionId;
        this.startedAt = startedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityCorrectiveActionId getActionId() { return actionId; }
    public Instant getStartedAt() { return startedAt; }
}

// Quality Corrective Action Step Added Event
class QualityCorrectiveActionStepAddedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityCorrectiveActionId actionId;
    private final QualityCorrectiveAction.QualityCorrectiveActionStep step;
    private final Instant addedAt;

    public QualityCorrectiveActionStepAddedEvent(QualityCorrectiveActionId actionId,
                                               QualityCorrectiveAction.QualityCorrectiveActionStep step,
                                               Instant addedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.actionId = actionId;
        this.step = step;
        this.addedAt = addedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityCorrectiveActionId getActionId() { return actionId; }
    public QualityCorrectiveAction.QualityCorrectiveActionStep getStep() { return step; }
    public Instant getAddedAt() { return addedAt; }
}

// Quality Corrective Action Step Completed Event
class QualityCorrectiveActionStepCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityCorrectiveActionId actionId;
    private final int stepNumber;
    private final String completedBy;
    private final Instant completedAt;
    private final String notes;

    public QualityCorrectiveActionStepCompletedEvent(QualityCorrectiveActionId actionId,
                                                   int stepNumber, String completedBy,
                                                   Instant completedAt, String notes) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.actionId = actionId;
        this.stepNumber = stepNumber;
        this.completedBy = completedBy;
        this.completedAt = completedAt;
        this.notes = notes;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityCorrectiveActionId getActionId() { return actionId; }
    public int getStepNumber() { return stepNumber; }
    public String getCompletedBy() { return completedBy; }
    public Instant getCompletedAt() { return completedAt; }
    public String getNotes() { return notes; }
}

// Quality Corrective Action Completed Event
class QualityCorrectiveActionCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityCorrectiveActionId actionId;
    private final String completedBy;
    private final Instant completedAt;
    private final String notes;

    public QualityCorrectiveActionCompletedEvent(QualityCorrectiveActionId actionId,
                                               String completedBy, Instant completedAt, String notes) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.actionId = actionId;
        this.completedBy = completedBy;
        this.completedAt = completedAt;
        this.notes = notes;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityCorrectiveActionId getActionId() { return actionId; }
    public String getCompletedBy() { return completedBy; }
    public Instant getCompletedAt() { return completedAt; }
    public String getNotes() { return notes; }
}

// Quality Corrective Action Effectiveness Verified Event
class QualityCorrectiveActionEffectivenessVerifiedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityCorrectiveActionId actionId;
    private final QualityCorrectiveActionEffectiveness effectiveness;
    private final String verifiedBy;
    private final String notes;
    private final Instant verifiedAt;

    public QualityCorrectiveActionEffectivenessVerifiedEvent(QualityCorrectiveActionId actionId,
                                                           QualityCorrectiveActionEffectiveness effectiveness,
                                                           String verifiedBy, String notes, 
                                                           Instant verifiedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.actionId = actionId;
        this.effectiveness = effectiveness;
        this.verifiedBy = verifiedBy;
        this.notes = notes;
        this.verifiedAt = verifiedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityCorrectiveActionId getActionId() { return actionId; }
    public QualityCorrectiveActionEffectiveness getEffectiveness() { return effectiveness; }
    public String getVerifiedBy() { return verifiedBy; }
    public String getNotes() { return notes; }
    public Instant getVerifiedAt() { return verifiedAt; }
}

// Quality Corrective Action Cancelled Event
class QualityCorrectiveActionCancelledEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityCorrectiveActionId actionId;
    private final String cancelledBy;
    private final String reason;
    private final Instant cancelledAt;

    public QualityCorrectiveActionCancelledEvent(QualityCorrectiveActionId actionId,
                                               String cancelledBy, String reason, Instant cancelledAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.actionId = actionId;
        this.cancelledBy = cancelledBy;
        this.reason = reason;
        this.cancelledAt = cancelledAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityCorrectiveActionId getActionId() { return actionId; }
    public String getCancelledBy() { return cancelledBy; }
    public String getReason() { return reason; }
    public Instant getCancelledAt() { return cancelledAt; }
}