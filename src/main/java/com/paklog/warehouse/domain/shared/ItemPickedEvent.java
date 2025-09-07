package com.paklog.warehouse.domain.shared;

import java.time.Instant;
import java.util.UUID;

public class ItemPickedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final PickListId pickListId;
    private final SkuCode sku;
    private final Quantity quantity;
    private final BinLocation binLocation;
    private final String pickerId;

    public ItemPickedEvent(PickListId pickListId, SkuCode sku, Quantity quantity, 
                           BinLocation binLocation, String pickerId) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.pickListId = pickListId;
        this.sku = sku;
        this.quantity = quantity;
        this.binLocation = binLocation;
        this.pickerId = pickerId;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public PickListId getPickListId() {
        return pickListId;
    }

    public SkuCode getSku() {
        return sku;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public BinLocation getBinLocation() {
        return binLocation;
    }

    public String getPickerId() {
        return pickerId;
    }
}