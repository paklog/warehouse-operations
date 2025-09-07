package com.paklog.warehouse.domain.shared;

import java.util.Objects;
import java.util.UUID;

public class OrderId {
    private final UUID value;

    private OrderId(UUID value) {
        this.value = Objects.requireNonNull(value, "Order ID cannot be null");
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(String value) {
        return new OrderId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId = (OrderId) o;
        return value.equals(orderId.value);
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