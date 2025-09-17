package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WorkAssignedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID workId;
    private final String assignedTo;
    private final Instant occurredAt;

    public WorkAssignedEvent(UUID workId, String assignedTo) {
        this.eventId = UUID.randomUUID();
        this.workId = Objects.requireNonNull(workId, "Work ID cannot be null");
        this.assignedTo = Objects.requireNonNull(assignedTo, "Assigned to cannot be null");
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

    public String getAssignedTo() {
        return assignedTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkAssignedEvent that = (WorkAssignedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "WorkAssignedEvent{" +
                "workId=" + workId +
                ", assignedTo='" + assignedTo + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}