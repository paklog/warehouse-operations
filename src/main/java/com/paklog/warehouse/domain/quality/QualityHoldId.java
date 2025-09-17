package com.paklog.warehouse.domain.quality;

import java.util.Objects;
import java.util.UUID;

public class QualityHoldId {
    private final String value;

    public QualityHoldId(String value) {
        this.value = Objects.requireNonNull(value, "Hold ID cannot be null");
    }

    public static QualityHoldId generate() {
        return new QualityHoldId("QH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static QualityHoldId of(String value) {
        return new QualityHoldId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityHoldId that = (QualityHoldId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}