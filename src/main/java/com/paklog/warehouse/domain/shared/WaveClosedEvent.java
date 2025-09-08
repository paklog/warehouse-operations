package com.paklog.warehouse.domain.shared;

import com.paklog.warehouse.domain.wave.WaveId;

import java.time.Instant;
import java.util.UUID;

public class WaveClosedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final WaveId waveId;
    private final Instant closedDate;

    public WaveClosedEvent(WaveId waveId, Instant closedDate) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.waveId = waveId;
        this.closedDate = closedDate;
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

    public Instant getClosedDate() {
        return closedDate;
    }
}