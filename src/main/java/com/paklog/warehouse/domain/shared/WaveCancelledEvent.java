package com.paklog.warehouse.domain.shared;

import com.paklog.warehouse.domain.wave.WaveId;

import java.time.Instant;
import java.util.UUID;

public class WaveCancelledEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final WaveId waveId;
    private final Instant cancelledDate;

    public WaveCancelledEvent(WaveId waveId, Instant cancelledDate) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.waveId = waveId;
        this.cancelledDate = cancelledDate;
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

    public Instant getCancelledDate() {
        return cancelledDate;
    }

    public Instant getCancelledAt() {
        return cancelledDate;
    }
}