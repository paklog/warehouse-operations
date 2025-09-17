package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WorkCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID workId;
    private final String completedBy;
    private final Instant completedAt;
    private final Instant occurredAt;

    public WorkCompletedEvent(UUID workId, String completedBy, Instant completedAt) {
        this.eventId = UUID.randomUUID();
        this.workId = Objects.requireNonNull(workId, "Work ID cannot be null");
        this.completedBy = Objects.requireNonNull(completedBy, "Completed by cannot be null");
        this.completedAt = Objects.requireNonNull(completedAt, "Completed at cannot be null");
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

    public String getCompletedBy() {
        return completedBy;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkCompletedEvent that = (WorkCompletedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "WorkCompletedEvent{" +
                "workId=" + workId +
                ", completedBy='" + completedBy + '\'' +
                ", completedAt=" + completedAt +
                ", occurredAt=" + occurredAt +
                '}';
    }
}