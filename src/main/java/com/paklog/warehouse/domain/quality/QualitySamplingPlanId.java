package com.paklog.warehouse.domain.quality;

import java.util.Objects;
import java.util.UUID;

public class QualitySamplingPlanId {
    private final String value;

    public QualitySamplingPlanId(String value) {
        this.value = Objects.requireNonNull(value, "Sampling plan ID cannot be null");
    }

    public static QualitySamplingPlanId generate() {
        return new QualitySamplingPlanId("SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static QualitySamplingPlanId of(String value) {
        return new QualitySamplingPlanId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualitySamplingPlanId that = (QualitySamplingPlanId) o;
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