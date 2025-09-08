package com.paklog.warehouse.domain.packaging;

import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.Objects;

public class PackedItem {
    private final SkuCode skuCode;
    private final int quantity;

    public PackedItem(SkuCode skuCode, int quantity) {
        if (skuCode == null) {
            throw new IllegalArgumentException("SKU code cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.skuCode = skuCode;
        this.quantity = quantity;
    }

    public SkuCode getSkuCode() {
        return skuCode;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackedItem that = (PackedItem) o;
        return quantity == that.quantity && Objects.equals(skuCode, that.skuCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skuCode, quantity);
    }

    @Override
    public String toString() {
        return "PackedItem{" +
                "skuCode=" + skuCode +
                ", quantity=" + quantity +
                '}';
    }
}