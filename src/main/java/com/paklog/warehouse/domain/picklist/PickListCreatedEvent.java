package com.paklog.warehouse.domain.picklist;
import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.OrderId;

import java.time.Instant;
import java.util.UUID;

public class PickListCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final PickListId pickListId;
    private final OrderId orderId;

    public PickListCreatedEvent(PickListId pickListId, OrderId orderId) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.pickListId = pickListId;
        this.orderId = orderId;
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

    public OrderId getOrderId() {
        return orderId;
    }
}