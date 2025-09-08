package com.paklog.warehouse.domain.shared;

import com.paklog.warehouse.domain.wave.WaveId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WaveCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final WaveId waveId;
    private final List<OrderId> orderIds;
    private final String shippingSpeedCategory;

    public WaveCreatedEvent(WaveId waveId, List<OrderId> orderIds, String shippingSpeedCategory, Instant occurredAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = occurredAt;
        this.waveId = waveId;
        this.orderIds = orderIds;
        this.shippingSpeedCategory = shippingSpeedCategory;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public WaveId getWaveId() {
        return waveId;
    }

    public List<OrderId> getOrderIds() {
        return orderIds;
    }

    public String getShippingSpeedCategory() {
        return shippingSpeedCategory;
    }

    public int getOrderCount() {
        return orderIds.size();
    }
}