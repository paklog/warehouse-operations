package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class LicensePlateReceivedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId licensePlateId;
    private final BinLocation location;
    private final String receivedBy;
    private final Instant receivedAt;
    private final String receivingReference;

    public LicensePlateReceivedEvent(LicensePlateId licensePlateId, BinLocation location, 
                                   String receivedBy, Instant receivedAt, String receivingReference) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.licensePlateId = licensePlateId;
        this.location = location;
        this.receivedBy = receivedBy;
        this.receivedAt = receivedAt;
        this.receivingReference = receivingReference;
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

    public BinLocation getLocation() {
        return location;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public String getReceivingReference() {
        return receivingReference;
    }
}