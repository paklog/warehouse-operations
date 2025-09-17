package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Location;

import java.time.Instant;
import java.util.UUID;

// Quality Quarantine Created Event
class QualityQuarantineCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityQuarantineId quarantineId;
    private final SkuCode item;
    private final String batchNumber;
    private final int quantity;
    private final QualityQuarantineReason reason;
    private final String quarantinedBy;
    private final Instant quarantinedAt;

    public QualityQuarantineCreatedEvent(QualityQuarantineId quarantineId, SkuCode item, 
                                       String batchNumber, int quantity, QualityQuarantineReason reason,
                                       String quarantinedBy, Instant quarantinedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.quarantineId = quarantineId;
        this.item = item;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.reason = reason;
        this.quarantinedBy = quarantinedBy;
        this.quarantinedAt = quarantinedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityQuarantineId getQuarantineId() { return quarantineId; }
    public SkuCode getItem() { return item; }
    public String getBatchNumber() { return batchNumber; }
    public int getQuantity() { return quantity; }
    public QualityQuarantineReason getReason() { return reason; }
    public String getQuarantinedBy() { return quarantinedBy; }
    public Instant getQuarantinedAt() { return quarantinedAt; }
}

// Quality Quarantine Location Changed Event
class QualityQuarantineLocationChangedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityQuarantineId quarantineId;
    private final Location newLocation;
    private final Instant movedAt;

    public QualityQuarantineLocationChangedEvent(QualityQuarantineId quarantineId, 
                                               Location newLocation, Instant movedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.quarantineId = quarantineId;
        this.newLocation = newLocation;
        this.movedAt = movedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityQuarantineId getQuarantineId() { return quarantineId; }
    public Location getNewLocation() { return newLocation; }
    public Instant getMovedAt() { return movedAt; }
}

// Quality Quarantine Released Event
class QualityQuarantineReleasedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityQuarantineId quarantineId;
    private final String releasedBy;
    private final Instant releasedAt;
    private final String notes;

    public QualityQuarantineReleasedEvent(QualityQuarantineId quarantineId, 
                                        String releasedBy, Instant releasedAt, String notes) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.quarantineId = quarantineId;
        this.releasedBy = releasedBy;
        this.releasedAt = releasedAt;
        this.notes = notes;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityQuarantineId getQuarantineId() { return quarantineId; }
    public String getReleasedBy() { return releasedBy; }
    public Instant getReleasedAt() { return releasedAt; }
    public String getNotes() { return notes; }
}

// Quality Quarantine Disposed Event
class QualityQuarantineDisposedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityQuarantineId quarantineId;
    private final String disposedBy;
    private final Instant disposedAt;
    private final String notes;

    public QualityQuarantineDisposedEvent(QualityQuarantineId quarantineId, 
                                        String disposedBy, Instant disposedAt, String notes) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.quarantineId = quarantineId;
        this.disposedBy = disposedBy;
        this.disposedAt = disposedAt;
        this.notes = notes;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityQuarantineId getQuarantineId() { return quarantineId; }
    public String getDisposedBy() { return disposedBy; }
    public Instant getDisposedAt() { return disposedAt; }
    public String getNotes() { return notes; }
}

// Quality Quarantine Extended Event
class QualityQuarantineExtendedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityQuarantineId quarantineId;
    private final Instant previousExpiryDate;
    private final Instant newExpiryDate;
    private final String extendedBy;
    private final String reason;
    private final Instant extendedAt;

    public QualityQuarantineExtendedEvent(QualityQuarantineId quarantineId, 
                                        Instant previousExpiryDate, Instant newExpiryDate,
                                        String extendedBy, String reason, Instant extendedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.quarantineId = quarantineId;
        this.previousExpiryDate = previousExpiryDate;
        this.newExpiryDate = newExpiryDate;
        this.extendedBy = extendedBy;
        this.reason = reason;
        this.extendedAt = extendedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityQuarantineId getQuarantineId() { return quarantineId; }
    public Instant getPreviousExpiryDate() { return previousExpiryDate; }
    public Instant getNewExpiryDate() { return newExpiryDate; }
    public String getExtendedBy() { return extendedBy; }
    public String getReason() { return reason; }
    public Instant getExtendedAt() { return extendedAt; }
}

// Quality Quarantine Expired Event
class QualityQuarantineExpiredEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualityQuarantineId quarantineId;
    private final Instant expiryDate;
    private final Instant detectedAt;

    public QualityQuarantineExpiredEvent(QualityQuarantineId quarantineId, 
                                       Instant expiryDate, Instant detectedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.quarantineId = quarantineId;
        this.expiryDate = expiryDate;
        this.detectedAt = detectedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualityQuarantineId getQuarantineId() { return quarantineId; }
    public Instant getExpiryDate() { return expiryDate; }
    public Instant getDetectedAt() { return detectedAt; }
}