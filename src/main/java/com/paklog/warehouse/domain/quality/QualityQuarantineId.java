package com.paklog.warehouse.domain.quality;

import java.util.Objects;
import java.util.UUID;

public class QualityQuarantineId {
    private final String value;

    public QualityQuarantineId(String value) {
        this.value = Objects.requireNonNull(value, "Quarantine ID cannot be null");
    }

    public static QualityQuarantineId generate() {
        return new QualityQuarantineId("QT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static QualityQuarantineId of(String value) {
        return new QualityQuarantineId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityQuarantineId that = (QualityQuarantineId) o;
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