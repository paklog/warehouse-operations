package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.UUID;

// Quality Hold Created Event
class QualityHoldCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityHoldId holdId;
    private final SkuCode item;
    private final String batchNumber;
    private final int quantity;
    private final QualityHoldReason reason;
    private final String heldBy;
    private final Instant heldAt;
    private final String notes;

    public QualityHoldCreatedEvent(QualityHoldId holdId, SkuCode item, String batchNumber,
                                 int quantity, QualityHoldReason reason, String heldBy, 
                                 Instant heldAt, String notes) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.holdId = holdId;
        this.item = item;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.reason = reason;
        this.heldBy = heldBy;
        this.heldAt = heldAt;
        this.notes = notes;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityHoldId getHoldId() { return holdId; }
    public SkuCode getItem() { return item; }
    public String getBatchNumber() { return batchNumber; }
    public int getQuantity() { return quantity; }
    public QualityHoldReason getReason() { return reason; }
    public String getHeldBy() { return heldBy; }
    public Instant getHeldAt() { return heldAt; }
    public String getNotes() { return notes; }
}

// Quality Hold Released Event
class QualityHoldReleasedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityHoldId holdId;
    private final String releasedBy;
    private final Instant releasedAt;
    private final String notes;

    public QualityHoldReleasedEvent(QualityHoldId holdId, String releasedBy, 
                                  Instant releasedAt, String notes) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.holdId = holdId;
        this.releasedBy = releasedBy;
        this.releasedAt = releasedAt;
        this.notes = notes;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityHoldId getHoldId() { return holdId; }
    public String getReleasedBy() { return releasedBy; }
    public Instant getReleasedAt() { return releasedAt; }
    public String getNotes() { return notes; }
}

// Quality Hold Escalated Event
class QualityHoldEscalatedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityHoldId holdId;
    private final QualityHoldReason previousReason;
    private final QualityHoldReason newReason;
    private final String escalatedBy;
    private final Instant escalatedAt;

    public QualityHoldEscalatedEvent(QualityHoldId holdId, QualityHoldReason previousReason,
                                   QualityHoldReason newReason, String escalatedBy, 
                                   Instant escalatedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.holdId = holdId;
        this.previousReason = previousReason;
        this.newReason = newReason;
        this.escalatedBy = escalatedBy;
        this.escalatedAt = escalatedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityHoldId getHoldId() { return holdId; }
    public QualityHoldReason getPreviousReason() { return previousReason; }
    public QualityHoldReason getNewReason() { return newReason; }
    public String getEscalatedBy() { return escalatedBy; }
    public Instant getEscalatedAt() { return escalatedAt; }
}

// Quality Hold Note Added Event
class QualityHoldNoteAddedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityHoldId holdId;
    private final String note;
    private final String addedBy;
    private final Instant addedAt;

    public QualityHoldNoteAddedEvent(QualityHoldId holdId, String note, 
                                   String addedBy, Instant addedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.holdId = holdId;
        this.note = note;
        this.addedBy = addedBy;
        this.addedAt = addedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityHoldId getHoldId() { return holdId; }
    public String getNote() { return note; }
    public String getAddedBy() { return addedBy; }
    public Instant getAddedAt() { return addedAt; }
}