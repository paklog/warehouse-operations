package com.paklog.warehouse.domain.shared;

import com.paklog.warehouse.domain.wave.WaveId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WaveReleasedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final WaveId waveId;
    private final List<OrderId> orderIds;
    private final Instant releaseDate;

    public WaveReleasedEvent(WaveId waveId, List<OrderId> orderIds, Instant releaseDate) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.waveId = waveId;
        this.orderIds = orderIds;
        this.releaseDate = releaseDate;
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

    public Instant getReleaseDate() {
        return releaseDate;
    }

    public int getOrderCount() {
        return orderIds.size();
    }
}