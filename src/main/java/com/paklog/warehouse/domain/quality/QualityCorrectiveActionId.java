package com.paklog.warehouse.domain.quality;

import java.util.Objects;
import java.util.UUID;

public class QualityCorrectiveActionId {
    private final String value;

    public QualityCorrectiveActionId(String value) {
        this.value = Objects.requireNonNull(value, "Corrective action ID cannot be null");
    }

    public static QualityCorrectiveActionId generate() {
        return new QualityCorrectiveActionId("CA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static QualityCorrectiveActionId of(String value) {
        return new QualityCorrectiveActionId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityCorrectiveActionId that = (QualityCorrectiveActionId) o;
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