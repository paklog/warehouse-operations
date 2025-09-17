package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WorkReleasedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID workId;
    private final Instant occurredAt;

    public WorkReleasedEvent(UUID workId) {
        this.eventId = UUID.randomUUID();
        this.workId = Objects.requireNonNull(workId, "Work ID cannot be null");
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public UUID getWorkId() {
        return workId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkReleasedEvent that = (WorkReleasedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "WorkReleasedEvent{" +
                "workId=" + workId +
                ", occurredAt=" + occurredAt +
                '}';
    }
}