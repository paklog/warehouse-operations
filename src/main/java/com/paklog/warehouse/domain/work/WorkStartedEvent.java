package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WorkStartedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID workId;
    private final String assignedTo;
    private final Instant occurredAt;

    public WorkStartedEvent(UUID workId, String assignedTo) {
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

    public String getStartedBy() {
        return assignedTo;  // Alias for getAssignedTo
    }

    public Instant getStartedAt() {
        return occurredAt;  // Alias for getOccurredAt
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkStartedEvent that = (WorkStartedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "WorkStartedEvent{" +
                "workId=" + workId +
                ", assignedTo='" + assignedTo + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}