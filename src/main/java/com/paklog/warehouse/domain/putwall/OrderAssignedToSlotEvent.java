package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class OrderAssignedToSlotEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final PutWallId putWallId;
    private final PutWallSlotId slotId;
    private final OrderId orderId;
    private final Map<SkuCode, Quantity> requiredItems;

    public OrderAssignedToSlotEvent(PutWallId putWallId, PutWallSlotId slotId, OrderId orderId,
                                   Map<SkuCode, Quantity> requiredItems) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.putWallId = Objects.requireNonNull(putWallId, "PutWall ID cannot be null");
        this.slotId = Objects.requireNonNull(slotId, "Slot ID cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.requiredItems = Objects.requireNonNull(requiredItems, "Required items cannot be null");
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public PutWallId getPutWallId() {
        return putWallId;
    }

    public PutWallSlotId getSlotId() {
        return slotId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Map<SkuCode, Quantity> getRequiredItems() {
        return requiredItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderAssignedToSlotEvent that = (OrderAssignedToSlotEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "OrderAssignedToSlotEvent{" +
                "eventId=" + eventId +
                ", occurredAt=" + occurredAt +
                ", putWallId=" + putWallId +
                ", slotId=" + slotId +
                ", orderId=" + orderId +
                ", requiredItems=" + requiredItems +
                '}';
    }
}