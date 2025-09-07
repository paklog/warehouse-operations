package com.paklog.warehouse.domain.shared;

import java.util.Objects;

public class Quantity {
    private final int value;

    public Quantity(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.value = value;
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public int getValue() {
        return value;
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        int result = this.value - other.value;
        if (result <= 0) {
            throw new IllegalArgumentException("Subtraction would result in non-positive quantity");
        }
        return new Quantity(result);
    }

    public Quantity multiply(int factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Multiplication factor must be positive");
        }
        return new Quantity(this.value * factor);
    }

    public boolean isLessThan(Quantity other) {
        return this.value < other.value;
    }

    public boolean isGreaterThan(Quantity other) {
        return this.value > other.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}