package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class LicensePlateShippedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId licensePlateId;
    private final String shipmentReference;
    private final String shippedBy;
    private final Instant shippedAt;

    public LicensePlateShippedEvent(LicensePlateId licensePlateId, String shipmentReference, 
                                  String shippedBy, Instant shippedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.licensePlateId = licensePlateId;
        this.shipmentReference = shipmentReference;
        this.shippedBy = shippedBy;
        this.shippedAt = shippedAt;
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

    public String getShipmentReference() {
        return shipmentReference;
    }

    public String getShippedBy() {
        return shippedBy;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }
}