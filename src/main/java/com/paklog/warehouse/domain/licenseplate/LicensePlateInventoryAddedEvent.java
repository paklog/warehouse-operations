package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;

import java.time.Instant;
import java.util.UUID;

public class LicensePlateInventoryAddedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId licensePlateId;
    private final SkuCode item;
    private final Quantity addedQuantity;
    private final Quantity totalQuantity;

    public LicensePlateInventoryAddedEvent(LicensePlateId licensePlateId, SkuCode item, 
                                         Quantity addedQuantity, Quantity totalQuantity) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.licensePlateId = licensePlateId;
        this.item = item;
        this.addedQuantity = addedQuantity;
        this.totalQuantity = totalQuantity;
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

    public Quantity getAddedQuantity() {
        return addedQuantity;
    }

    public Quantity getTotalQuantity() {
        return totalQuantity;
    }
}