package com.paklog.warehouse.domain.work;

import java.util.Objects;
import java.util.UUID;

public class WorkId {
    private final UUID value;

    public WorkId(UUID value) {
        this.value = Objects.requireNonNull(value, "WorkId value cannot be null");
    }

    public WorkId(String value) {
        this.value = UUID.fromString(Objects.requireNonNull(value, "WorkId value cannot be null"));
    }

    public static WorkId generate() {
        return new WorkId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkId workId = (WorkId) o;
        return Objects.equals(value, workId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}