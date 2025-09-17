package com.paklog.warehouse.domain.quality;

import java.util.Objects;
import java.util.UUID;

public class QualityNonConformanceId {
    private final UUID value;

    private QualityNonConformanceId(UUID value) {
        this.value = Objects.requireNonNull(value, "Value cannot be null");
    }

    public static QualityNonConformanceId generate() {
        return new QualityNonConformanceId(UUID.randomUUID());
    }

    public static QualityNonConformanceId of(String id) {
        return new QualityNonConformanceId(UUID.fromString(id));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityNonConformanceId that = (QualityNonConformanceId) o;
        return Objects.equals(value, that.value);
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