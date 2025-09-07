package com.paklog.warehouse.domain.shared;

import java.util.Objects;

public class OrderItem {
    private final SkuCode skuCode;
    private final Quantity quantity;

    public OrderItem(SkuCode skuCode, Quantity quantity) {
        this.skuCode = Objects.requireNonNull(skuCode, "SKU code cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
    }

    public SkuCode getSkuCode() {
        return skuCode;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return skuCode.equals(orderItem.skuCode) && 
               quantity.equals(orderItem.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skuCode, quantity);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
               "skuCode=" + skuCode +
               ", quantity=" + quantity +
               '}';
    }
}