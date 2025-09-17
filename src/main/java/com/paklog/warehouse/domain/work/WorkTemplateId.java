package com.paklog.warehouse.domain.work;

import java.util.Objects;
import java.util.UUID;

public class WorkTemplateId {
    private final String value;

    private WorkTemplateId(String value) {
        this.value = Objects.requireNonNull(value, "WorkTemplateId value cannot be null");
    }

    public static WorkTemplateId generate() {
        return new WorkTemplateId(UUID.randomUUID().toString());
    }

    public static WorkTemplateId fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("WorkTemplateId value cannot be null or empty");
        }
        return new WorkTemplateId(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkTemplateId that = (WorkTemplateId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "WorkTemplateId{" + value + '}';
    }
}