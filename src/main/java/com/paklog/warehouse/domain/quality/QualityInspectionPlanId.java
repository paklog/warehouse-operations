package com.paklog.warehouse.domain.quality;

import java.util.Objects;
import java.util.UUID;

public class QualityInspectionPlanId {
    private final UUID value;

    public QualityInspectionPlanId(UUID value) {
        this.value = Objects.requireNonNull(value, "QualityInspectionPlanId value cannot be null");
    }

    public QualityInspectionPlanId(String value) {
        this.value = UUID.fromString(Objects.requireNonNull(value, "QualityInspectionPlanId value cannot be null"));
    }

    public static QualityInspectionPlanId generate() {
        return new QualityInspectionPlanId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityInspectionPlanId that = (QualityInspectionPlanId) o;
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