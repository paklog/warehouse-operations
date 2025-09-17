package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WorkCancelledEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID workId;
    private final String reason;
    private final Instant cancelledAt;
    private final Instant occurredAt;

    public WorkCancelledEvent(UUID workId, String reason, Instant cancelledAt) {
        this.eventId = UUID.randomUUID();
        this.workId = Objects.requireNonNull(workId, "Work ID cannot be null");
        this.reason = reason;
        this.cancelledAt = Objects.requireNonNull(cancelledAt, "Cancelled at cannot be null");
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

    public String getReason() {
        return reason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkCancelledEvent that = (WorkCancelledEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "WorkCancelledEvent{" +
                "workId=" + workId +
                ", reason='" + reason + '\'' +
                ", cancelledAt=" + cancelledAt +
                ", occurredAt=" + occurredAt +
                '}';
    }
}