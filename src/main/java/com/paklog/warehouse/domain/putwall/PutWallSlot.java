package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PutWallSlot {
    private final PutWallSlotId slotId;
    private OrderId assignedOrderId;
    private final Map<SkuCode, Quantity> itemsRequired;
    private final Map<SkuCode, Quantity> itemsPlaced;
    private PutWallSlotStatus status;

    public PutWallSlot(PutWallSlotId slotId) {
        this.slotId = Objects.requireNonNull(slotId, "Slot ID cannot be null");
        this.itemsRequired = new HashMap<>();
        this.itemsPlaced = new HashMap<>();
        this.status = PutWallSlotStatus.FREE;
    }

    public void assignToOrder(OrderId orderId, Map<SkuCode, Quantity> requiredItems) {
        if (this.status != PutWallSlotStatus.FREE) {
            throw new IllegalStateException("Slot is not free for assignment");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (requiredItems == null || requiredItems.isEmpty()) {
            throw new IllegalArgumentException("Required items cannot be null or empty");
        }

        this.assignedOrderId = orderId;
        this.itemsRequired.clear();
        this.itemsRequired.putAll(requiredItems);
        this.itemsPlaced.clear();
        this.status = PutWallSlotStatus.IN_PROGRESS;
    }

    public void placeItem(SkuCode skuCode, Quantity quantity) {
        if (this.status != PutWallSlotStatus.IN_PROGRESS) {
            throw new IllegalStateException("Slot is not in progress");
        }
        if (!itemsRequired.containsKey(skuCode)) {
            throw new IllegalArgumentException("SKU " + skuCode + " is not required for this order");
        }

        Quantity currentPlaced = itemsPlaced.getOrDefault(skuCode, Quantity.of(0));
        Quantity newTotal = currentPlaced.add(quantity);
        Quantity required = itemsRequired.get(skuCode);

        if (newTotal.getValue() > required.getValue()) {
            throw new IllegalArgumentException("Cannot place more items than required. Required: " +
                required.getValue() + ", attempting to place: " + newTotal.getValue());
        }

        itemsPlaced.put(skuCode, newTotal);

        if (isOrderComplete()) {
            this.status = PutWallSlotStatus.COMPLETE;
        }
    }

    public boolean isOrderComplete() {
        return itemsRequired.entrySet().stream()
            .allMatch(entry -> {
                SkuCode sku = entry.getKey();
                Quantity required = entry.getValue();
                Quantity placed = itemsPlaced.getOrDefault(sku, Quantity.of(0));
                return placed.getValue() >= required.getValue();
            });
    }

    public void markReadyForPack() {
        if (this.status != PutWallSlotStatus.COMPLETE) {
            throw new IllegalStateException("Slot must be complete before marking ready for pack");
        }
        this.status = PutWallSlotStatus.READY_FOR_PACK;
    }

    public void release() {
        this.assignedOrderId = null;
        this.itemsRequired.clear();
        this.itemsPlaced.clear();
        this.status = PutWallSlotStatus.FREE;
    }

    public boolean isFree() {
        return this.status == PutWallSlotStatus.FREE;
    }

    public boolean isReadyForPack() {
        return this.status == PutWallSlotStatus.READY_FOR_PACK;
    }

    // Getters
    public PutWallSlotId getSlotId() {
        return slotId;
    }

    public OrderId getAssignedOrderId() {
        return assignedOrderId;
    }

    public Map<SkuCode, Quantity> getItemsRequired() {
        return new HashMap<>(itemsRequired);
    }

    public Map<SkuCode, Quantity> getItemsPlaced() {
        return new HashMap<>(itemsPlaced);
    }

    public PutWallSlotStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PutWallSlot that = (PutWallSlot) o;
        return Objects.equals(slotId, that.slotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slotId);
    }

    @Override
    public String toString() {
        return "PutWallSlot{" +
                "slotId=" + slotId +
                ", assignedOrderId=" + assignedOrderId +
                ", status=" + status +
                '}';
    }
}