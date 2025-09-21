package com.paklog.warehouse.domain.location;

import java.util.Objects;
import java.util.UUID;

public class LocationDirectiveId {
    private final String value;

    private LocationDirectiveId(String value) {
        this.value = Objects.requireNonNull(value, "LocationDirectiveId value cannot be null");
    }

    public static LocationDirectiveId generate() {
        return new LocationDirectiveId(UUID.randomUUID().toString());
    }

    public static LocationDirectiveId fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("LocationDirectiveId value cannot be null or empty");
        }
        return new LocationDirectiveId(value.trim());
    }

    public static LocationDirectiveId of(String value) {
        return fromString(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationDirectiveId that = (LocationDirectiveId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "LocationDirectiveId{" + value + '}';
    }
}