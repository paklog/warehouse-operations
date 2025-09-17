package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.UUID;

// Quality Inspection Scheduled Event
class QualityInspectionScheduledEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final QualityInspectionType inspectionType;
    private final SkuCode item;
    private final String inspectorId;
    private final Instant scheduledDate;

    public QualityInspectionScheduledEvent(QualityInspectionId inspectionId, 
                                         QualityInspectionType inspectionType, SkuCode item,
                                         String inspectorId, Instant scheduledDate) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.inspectionType = inspectionType;
        this.item = item;
        this.inspectorId = inspectorId;
        this.scheduledDate = scheduledDate;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public QualityInspectionType getInspectionType() { return inspectionType; }
    public SkuCode getItem() { return item; }
    public String getInspectorId() { return inspectorId; }
    public Instant getScheduledDate() { return scheduledDate; }
}

// Quality Inspection Started Event
class QualityInspectionStartedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final String inspectorId;
    private final Instant startedAt;

    public QualityInspectionStartedEvent(QualityInspectionId inspectionId, 
                                       String inspectorId, Instant startedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.inspectorId = inspectorId;
        this.startedAt = startedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public String getInspectorId() { return inspectorId; }
    public Instant getStartedAt() { return startedAt; }
}

// Quality Inspection Step Completed Event
class QualityInspectionStepCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final int stepNumber;
    private final QualityTestResult testResult;
    private final Instant completedAt;

    public QualityInspectionStepCompletedEvent(QualityInspectionId inspectionId, int stepNumber,
                                             QualityTestResult testResult, Instant completedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.stepNumber = stepNumber;
        this.testResult = testResult;
        this.completedAt = completedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public int getStepNumber() { return stepNumber; }
    public QualityTestResult getTestResult() { return testResult; }
    public Instant getCompletedAt() { return completedAt; }
}

// Quality Inspection Completed Event
class QualityInspectionCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final QualityDecision finalDecision;
    private final String supervisorId;
    private final Instant completedAt;
    private final boolean hasNonConformances;

    public QualityInspectionCompletedEvent(QualityInspectionId inspectionId, 
                                         QualityDecision finalDecision, String supervisorId,
                                         Instant completedAt, boolean hasNonConformances) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.finalDecision = finalDecision;
        this.supervisorId = supervisorId;
        this.completedAt = completedAt;
        this.hasNonConformances = hasNonConformances;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public QualityDecision getFinalDecision() { return finalDecision; }
    public String getSupervisorId() { return supervisorId; }
    public Instant getCompletedAt() { return completedAt; }
    public boolean hasNonConformances() { return hasNonConformances; }
}

// Quality Inspection Held Event
class QualityInspectionHeldEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final QualityHoldReason reason;
    private final String notes;
    private final String heldBy;
    private final Instant heldAt;

    public QualityInspectionHeldEvent(QualityInspectionId inspectionId, QualityHoldReason reason,
                                    String notes, String heldBy, Instant heldAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.reason = reason;
        this.notes = notes;
        this.heldBy = heldBy;
        this.heldAt = heldAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public QualityHoldReason getReason() { return reason; }
    public String getNotes() { return notes; }
    public String getHeldBy() { return heldBy; }
    public Instant getHeldAt() { return heldAt; }
}

// Quality Inspection Released Event
class QualityInspectionReleasedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final String notes;
    private final String releasedBy;
    private final Instant releasedAt;

    public QualityInspectionReleasedEvent(QualityInspectionId inspectionId, String notes,
                                        String releasedBy, Instant releasedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.notes = notes;
        this.releasedBy = releasedBy;
        this.releasedAt = releasedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public String getNotes() { return notes; }
    public String getReleasedBy() { return releasedBy; }
    public Instant getReleasedAt() { return releasedAt; }
}

// Quality Inspection Cancelled Event
class QualityInspectionCancelledEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final QualityInspectionStatus previousStatus;
    private final String reason;
    private final String cancelledBy;
    private final Instant cancelledAt;

    public QualityInspectionCancelledEvent(QualityInspectionId inspectionId, 
                                         QualityInspectionStatus previousStatus,
                                         String reason, String cancelledBy, Instant cancelledAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.previousStatus = previousStatus;
        this.reason = reason;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = cancelledAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public QualityInspectionStatus getPreviousStatus() { return previousStatus; }
    public String getReason() { return reason; }
    public String getCancelledBy() { return cancelledBy; }
    public Instant getCancelledAt() { return cancelledAt; }
}

// Quality Non-Conformance Added Event
class QualityNonConformanceAddedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final QualityNonConformanceId nonConformanceId;
    private final QualityNonConformanceType type;
    private final QualitySeverity severity;
    private final String identifiedBy;
    private final Instant identifiedAt;

    public QualityNonConformanceAddedEvent(QualityInspectionId inspectionId, 
                                         QualityNonConformanceId nonConformanceId,
                                         QualityNonConformanceType type, QualitySeverity severity,
                                         String identifiedBy, Instant identifiedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.nonConformanceId = nonConformanceId;
        this.type = type;
        this.severity = severity;
        this.identifiedBy = identifiedBy;
        this.identifiedAt = identifiedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public QualityNonConformanceId getNonConformanceId() { return nonConformanceId; }
    public QualityNonConformanceType getType() { return type; }
    public QualitySeverity getSeverity() { return severity; }
    public String getIdentifiedBy() { return identifiedBy; }
    public Instant getIdentifiedAt() { return identifiedAt; }
}

// Quality Inspection Supervisor Assigned Event
class QualityInspectionSupervisorAssignedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final String supervisorId;
    private final Instant assignedAt;

    public QualityInspectionSupervisorAssignedEvent(QualityInspectionId inspectionId, 
                                                   String supervisorId, Instant assignedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.supervisorId = supervisorId;
        this.assignedAt = assignedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public String getSupervisorId() { return supervisorId; }
    public Instant getAssignedAt() { return assignedAt; }
}

// Quality Inspection Rescheduled Event
class QualityInspectionRescheduledEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityInspectionId inspectionId;
    private final Instant previousDate;
    private final Instant newDate;
    private final Instant rescheduledAt;

    public QualityInspectionRescheduledEvent(QualityInspectionId inspectionId, 
                                           Instant previousDate, Instant newDate, 
                                           Instant rescheduledAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.inspectionId = inspectionId;
        this.previousDate = previousDate;
        this.newDate = newDate;
        this.rescheduledAt = rescheduledAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityInspectionId getInspectionId() { return inspectionId; }
    public Instant getPreviousDate() { return previousDate; }
    public Instant getNewDate() { return newDate; }
    public Instant getRescheduledAt() { return rescheduledAt; }
}