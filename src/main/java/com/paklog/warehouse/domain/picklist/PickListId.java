package com.paklog.warehouse.domain.picklist;

import java.util.Objects;
import java.util.UUID;

public class PickListId {
    private final UUID value;

    private PickListId(UUID value) {
        this.value = Objects.requireNonNull(value, "PickList ID cannot be null");
    }

    public static PickListId generate() {
        return new PickListId(UUID.randomUUID());
    }

    public static PickListId of(String value) {
        return new PickListId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PickListId that = (PickListId) o;
        return value.equals(that.value);
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