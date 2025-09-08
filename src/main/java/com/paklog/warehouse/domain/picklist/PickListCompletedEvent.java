package com.paklog.warehouse.domain.picklist;
import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class PickListCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final PickListId pickListId;

    public PickListCompletedEvent(PickListId pickListId) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.pickListId = pickListId;
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
}