package com.paklog.warehouse.domain.quality;

import java.util.Objects;
import java.util.UUID;

public class QualitySampleId {
    private final String value;

    public QualitySampleId(String value) {
        this.value = Objects.requireNonNull(value, "Sample ID cannot be null");
    }

    public static QualitySampleId generate() {
        return new QualitySampleId("QS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static QualitySampleId of(String value) {
        return new QualitySampleId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualitySampleId that = (QualitySampleId) o;
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