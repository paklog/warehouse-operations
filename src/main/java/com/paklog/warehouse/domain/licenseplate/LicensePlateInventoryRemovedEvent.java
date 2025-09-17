package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;

import java.time.Instant;
import java.util.UUID;

public class LicensePlateInventoryRemovedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId licensePlateId;
    private final SkuCode item;
    private final Quantity removedQuantity;
    private final Quantity remainingQuantity;

    public LicensePlateInventoryRemovedEvent(LicensePlateId licensePlateId, SkuCode item, 
                                           Quantity removedQuantity, Quantity remainingQuantity) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.licensePlateId = licensePlateId;
        this.item = item;
        this.removedQuantity = removedQuantity;
        this.remainingQuantity = remainingQuantity;
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

    public SkuCode getItem() {
        return item;
    }

    public Quantity getRemovedQuantity() {
        return removedQuantity;
    }

    public Quantity getRemainingQuantity() {
        return remainingQuantity;
    }
}