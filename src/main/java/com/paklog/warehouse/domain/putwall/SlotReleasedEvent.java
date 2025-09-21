package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.OrderId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class SlotReleasedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final PutWallId putWallId;
    private final PutWallSlotId slotId;
    private final OrderId releasedOrderId;

    public SlotReleasedEvent(PutWallId putWallId, PutWallSlotId slotId, OrderId releasedOrderId) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.putWallId = Objects.requireNonNull(putWallId, "PutWall ID cannot be null");
        this.slotId = Objects.requireNonNull(slotId, "Slot ID cannot be null");
        this.releasedOrderId = Objects.requireNonNull(releasedOrderId, "Released order ID cannot be null");
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

    public OrderId getReleasedOrderId() {
        return releasedOrderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotReleasedEvent that = (SlotReleasedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "SlotReleasedEvent{" +
                "eventId=" + eventId +
                ", occurredAt=" + occurredAt +
                ", putWallId=" + putWallId +
                ", slotId=" + slotId +
                ", releasedOrderId=" + releasedOrderId +
                '}';
    }
}