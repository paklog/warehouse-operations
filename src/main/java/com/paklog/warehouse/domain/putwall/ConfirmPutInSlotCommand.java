package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.Objects;

public class ConfirmPutInSlotCommand {
    private final PutWallId putWallId;
    private final PutWallSlotId slotId;
    private final SkuCode skuCode;
    private final Quantity quantity;

    public ConfirmPutInSlotCommand(PutWallId putWallId, PutWallSlotId slotId, SkuCode skuCode, Quantity quantity) {
        this.putWallId = Objects.requireNonNull(putWallId, "PutWall ID cannot be null");
        this.slotId = Objects.requireNonNull(slotId, "Slot ID cannot be null");
        this.skuCode = Objects.requireNonNull(skuCode, "SKU code cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");

        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public PutWallId getPutWallId() {
        return putWallId;
    }

    public PutWallSlotId getSlotId() {
        return slotId;
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
        ConfirmPutInSlotCommand that = (ConfirmPutInSlotCommand) o;
        return Objects.equals(putWallId, that.putWallId) &&
                Objects.equals(slotId, that.slotId) &&
                Objects.equals(skuCode, that.skuCode) &&
                Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(putWallId, slotId, skuCode, quantity);
    }

    @Override
    public String toString() {
        return "ConfirmPutInSlotCommand{" +
                "putWallId=" + putWallId +
                ", slotId=" + slotId +
                ", skuCode=" + skuCode +
                ", quantity=" + quantity +
                '}';
    }
}