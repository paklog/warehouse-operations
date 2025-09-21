package com.paklog.warehouse.domain.putwall;

import java.util.Objects;

public class PutWallSlotId {
    private final String value;

    private PutWallSlotId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("PutWall slot ID cannot be null or empty");
        }
        this.value = value.trim();
    }

    public static PutWallSlotId of(String value) {
        return new PutWallSlotId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PutWallSlotId that = (PutWallSlotId) o;
        return value.equals(that.value);
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