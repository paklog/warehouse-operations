package com.paklog.warehouse.domain.picklist;
import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class PickListAssignedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final PickListId pickListId;
    private final String pickerId;

    public PickListAssignedEvent(PickListId pickListId, String pickerId) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.pickListId = pickListId;
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

    public String getPickerId() {
        return pickerId;
    }
}