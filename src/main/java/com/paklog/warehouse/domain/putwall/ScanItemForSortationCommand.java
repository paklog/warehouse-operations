package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.Objects;

public class ScanItemForSortationCommand {
    private final PutWallId putWallId;
    private final SkuCode skuCode;
    private final Quantity quantity;

    public ScanItemForSortationCommand(PutWallId putWallId, SkuCode skuCode, Quantity quantity) {
        this.putWallId = Objects.requireNonNull(putWallId, "PutWall ID cannot be null");
        this.skuCode = Objects.requireNonNull(skuCode, "SKU code cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");

        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public PutWallId getPutWallId() {
        return putWallId;
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
        ScanItemForSortationCommand that = (ScanItemForSortationCommand) o;
        return Objects.equals(putWallId, that.putWallId) &&
                Objects.equals(skuCode, that.skuCode) &&
                Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(putWallId, skuCode, quantity);
    }

    @Override
    public String toString() {
        return "ScanItemForSortationCommand{" +
                "putWallId=" + putWallId +
                ", skuCode=" + skuCode +
                ", quantity=" + quantity +
                '}';
    }
}