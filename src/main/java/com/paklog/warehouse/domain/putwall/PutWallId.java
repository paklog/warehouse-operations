package com.paklog.warehouse.domain.putwall;

import java.util.Objects;
import java.util.UUID;

public class PutWallId {
    private final UUID value;

    private PutWallId(UUID value) {
        this.value = Objects.requireNonNull(value, "PutWall ID cannot be null");
    }

    public static PutWallId generate() {
        return new PutWallId(UUID.randomUUID());
    }

    public static PutWallId of(String value) {
        return new PutWallId(UUID.fromString(value));
    }

    public static PutWallId of(UUID value) {
        return new PutWallId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PutWallId putWallId = (PutWallId) o;
        return value.equals(putWallId.value);
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