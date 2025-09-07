package com.paklog.warehouse.domain.shared;

import java.util.Objects;

public class SkuCode {
    private final String value;

    public SkuCode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("SKU code cannot be null");
        }
        this.value = value.trim();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkuCode skuCode = (SkuCode) o;
        return Objects.equals(value, skuCode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

    public static SkuCode of(String value) {
        return new SkuCode(value);
    }
}