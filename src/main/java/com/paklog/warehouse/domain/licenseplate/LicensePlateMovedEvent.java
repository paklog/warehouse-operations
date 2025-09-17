package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class LicensePlateMovedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId licensePlateId;
    private final BinLocation fromLocation;
    private final BinLocation toLocation;
    private final String movedBy;
    private final Instant movedAt;

    public LicensePlateMovedEvent(LicensePlateId licensePlateId, BinLocation fromLocation, 
                                BinLocation toLocation, String movedBy, Instant movedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.licensePlateId = licensePlateId;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.movedBy = movedBy;
        this.movedAt = movedAt;
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

    public BinLocation getFromLocation() {
        return fromLocation;
    }

    public BinLocation getToLocation() {
        return toLocation;
    }

    public String getMovedBy() {
        return movedBy;
    }

    public Instant getMovedAt() {
        return movedAt;
    }
}