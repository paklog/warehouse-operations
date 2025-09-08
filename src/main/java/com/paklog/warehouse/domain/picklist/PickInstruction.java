package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.*;

import java.util.Objects;

public class PickInstruction {
    private final SkuCode sku;
    private final Quantity quantity;
    private final BinLocation binLocation;
    private boolean completed;

    public PickInstruction(SkuCode sku, Quantity quantity, BinLocation binLocation) {
        this.sku = Objects.requireNonNull(sku, "SKU cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
        this.binLocation = Objects.requireNonNull(binLocation, "Bin location cannot be null");
        this.completed = false;
    }

    public SkuCode getSku() {
        return sku;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public BinLocation getBinLocation() {
        return binLocation;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markCompleted() {
        this.completed = true;
    }

    public void markCompleted(BinLocation binLocation) {
        this.completed = true;
    }

    public Quantity getQuantityToPick() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PickInstruction that = (PickInstruction) o;
        return sku.equals(that.sku) && 
               quantity.equals(that.quantity) && 
               binLocation.equals(that.binLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, quantity, binLocation);
    }

    @Override
    public String toString() {
        return "PickInstruction{" +
               "sku=" + sku +
               ", quantity=" + quantity +
               ", binLocation=" + binLocation +
               ", completed=" + completed +
               '}';
    }
}