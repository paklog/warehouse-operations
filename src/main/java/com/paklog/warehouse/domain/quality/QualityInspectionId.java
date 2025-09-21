package com.paklog.warehouse.domain.quality;

import java.util.Objects;
import java.util.UUID;

public class QualityInspectionId {
    private final String value;

    public QualityInspectionId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Quality inspection ID cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static QualityInspectionId generate() {
        return new QualityInspectionId("QI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static QualityInspectionId fromString(String value) {
        return new QualityInspectionId(value);
    }

    public static QualityInspectionId of(String value) {
        return new QualityInspectionId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityInspectionId that = (QualityInspectionId) o;
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