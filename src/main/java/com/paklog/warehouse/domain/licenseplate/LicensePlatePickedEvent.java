package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class LicensePlatePickedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId licensePlateId;
    private final String pickedBy;
    private final Instant pickedAt;

    public LicensePlatePickedEvent(LicensePlateId licensePlateId, String pickedBy, Instant pickedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.licensePlateId = licensePlateId;
        this.pickedBy = pickedBy;
        this.pickedAt = pickedAt;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public LicensePlateId getLicensePlateId() {
        return licensePlateId;
    }

    public String getPickedBy() {
        return pickedBy;
    }

    public Instant getPickedAt() {
        return pickedAt;
    }
}

// Additional events for completeness
class LicensePlateStagedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId licensePlateId;
    private final com.paklog.warehouse.domain.shared.BinLocation fromLocation;
    private final com.paklog.warehouse.domain.shared.BinLocation stagingLocation;
    private final String stagedBy;
    private final Instant stagedAt;

    public LicensePlateStagedEvent(LicensePlateId licensePlateId, 
                                 com.paklog.warehouse.domain.shared.BinLocation fromLocation,
                                 com.paklog.warehouse.domain.shared.BinLocation stagingLocation, 
                                 String stagedBy, Instant stagedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.licensePlateId = licensePlateId;
        this.fromLocation = fromLocation;
        this.stagingLocation = stagingLocation;
        this.stagedBy = stagedBy;
        this.stagedAt = stagedAt;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public LicensePlateId getLicensePlateId() {
        return licensePlateId;
    }

    public com.paklog.warehouse.domain.shared.BinLocation getFromLocation() {
        return fromLocation;
    }

    public com.paklog.warehouse.domain.shared.BinLocation getStagingLocation() {
        return stagingLocation;
    }

    public String getStagedBy() {
        return stagedBy;
    }

    public Instant getStagedAt() {
        return stagedAt;
    }
}

class LicensePlateStatusChangedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId licensePlateId;
    private final LicensePlateStatus fromStatus;
    private final LicensePlateStatus toStatus;

    public LicensePlateStatusChangedEvent(LicensePlateId licensePlateId, 
                                        LicensePlateStatus fromStatus, 
                                        LicensePlateStatus toStatus) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.licensePlateId = licensePlateId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public LicensePlateId getLicensePlateId() {
        return licensePlateId;
    }

    public LicensePlateStatus getFromStatus() {
        return fromStatus;
    }

    public LicensePlateStatus getToStatus() {
        return toStatus;
    }
}

class LicensePlateCancelledEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId licensePlateId;
    private final LicensePlateStatus previousStatus;
    private final String reason;
    private final String cancelledBy;
    private final Instant cancelledAt;

    public LicensePlateCancelledEvent(LicensePlateId licensePlateId, 
                                    LicensePlateStatus previousStatus, 
                                    String reason, String cancelledBy, 
                                    Instant cancelledAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.licensePlateId = licensePlateId;
        this.previousStatus = previousStatus;
        this.reason = reason;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = cancelledAt;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public LicensePlateId getLicensePlateId() {
        return licensePlateId;
    }

    public LicensePlateStatus getPreviousStatus() {
        return previousStatus;
    }

    public String getReason() {
        return reason;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }
}