package com.paklog.warehouse.domain.shared;

import java.time.Instant;
import java.util.UUID;

public abstract class AbstractDomainEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;

    protected AbstractDomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
    }

    protected AbstractDomainEvent(Instant occurredAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}