package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.Map;
import java.util.Optional;

public class PutWallService {

    public SortationResult determineSortationTarget(PutWall putWall, SkuCode scannedSku) {
        if (putWall == null) {
            throw new IllegalArgumentException("PutWall cannot be null");
        }
        if (scannedSku == null) {
            throw new IllegalArgumentException("Scanned SKU cannot be null");
        }

        Optional<PutWallSlotId> targetSlot = putWall.getAllSlots().entrySet().stream()
            .filter(entry -> {
                PutWallSlot slot = entry.getValue();
                return slot.getStatus() == PutWallSlotStatus.IN_PROGRESS &&
                       slot.getItemsRequired().containsKey(scannedSku) &&
                       !isSkuCompleteForSlot(slot, scannedSku);
            })
            .map(Map.Entry::getKey)
            .findFirst();

        if (targetSlot.isPresent()) {
            PutWallSlot slot = putWall.getSlotById(targetSlot.get());
            Quantity required = slot.getItemsRequired().get(scannedSku);
            Quantity placed = slot.getItemsPlaced().getOrDefault(scannedSku, Quantity.of(0));
            Quantity remaining = required.subtract(placed);

            return SortationResult.found(targetSlot.get(), slot.getAssignedOrderId(), remaining);
        }

        return SortationResult.notFound("No active slot requires SKU: " + scannedSku);
    }

    private boolean isSkuCompleteForSlot(PutWallSlot slot, SkuCode skuCode) {
        Quantity required = slot.getItemsRequired().get(skuCode);
        Quantity placed = slot.getItemsPlaced().getOrDefault(skuCode, Quantity.of(0));
        return placed.getValue() >= required.getValue();
    }

    public void validateItemPlacement(PutWall putWall, PutWallSlotId slotId, SkuCode skuCode, Quantity quantity) {
        if (putWall == null) {
            throw new IllegalArgumentException("PutWall cannot be null");
        }
        if (slotId == null) {
            throw new IllegalArgumentException("Slot ID cannot be null");
        }
        if (skuCode == null) {
            throw new IllegalArgumentException("SKU code cannot be null");
        }
        if (quantity == null || quantity.getValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        PutWallSlot slot = putWall.getSlotById(slotId);

        if (slot.getStatus() != PutWallSlotStatus.IN_PROGRESS) {
            throw new IllegalStateException("Slot " + slotId + " is not in progress");
        }

        if (!slot.getItemsRequired().containsKey(skuCode)) {
            throw new IllegalArgumentException("SKU " + skuCode + " is not required for order in slot " + slotId);
        }

        Quantity required = slot.getItemsRequired().get(skuCode);
        Quantity currentPlaced = slot.getItemsPlaced().getOrDefault(skuCode, Quantity.of(0));
        Quantity newTotal = currentPlaced.add(quantity);

        if (newTotal.getValue() > required.getValue()) {
            throw new IllegalArgumentException(
                String.format("Cannot place %d items. Required: %d, Already placed: %d, Would exceed by: %d",
                    quantity.getValue(), required.getValue(), currentPlaced.getValue(),
                    newTotal.getValue() - required.getValue())
            );
        }
    }

    public static class SortationResult {
        private final boolean found;
        private final PutWallSlotId targetSlotId;
        private final OrderId orderId;
        private final Quantity quantityNeeded;
        private final String reason;

        private SortationResult(boolean found, PutWallSlotId targetSlotId, OrderId orderId,
                               Quantity quantityNeeded, String reason) {
            this.found = found;
            this.targetSlotId = targetSlotId;
            this.orderId = orderId;
            this.quantityNeeded = quantityNeeded;
            this.reason = reason;
        }

        public static SortationResult found(PutWallSlotId targetSlotId, OrderId orderId, Quantity quantityNeeded) {
            return new SortationResult(true, targetSlotId, orderId, quantityNeeded, null);
        }

        public static SortationResult notFound(String reason) {
            return new SortationResult(false, null, null, null, reason);
        }

        public boolean isFound() {
            return found;
        }

        public PutWallSlotId getTargetSlotId() {
            return targetSlotId;
        }

        public OrderId getOrderId() {
            return orderId;
        }

        public Quantity getQuantityNeeded() {
            return quantityNeeded;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            if (found) {
                return "SortationResult{found=true, targetSlotId=" + targetSlotId +
                       ", orderId=" + orderId + ", quantityNeeded=" + quantityNeeded + "}";
            } else {
                return "SortationResult{found=false, reason='" + reason + "'}";
            }
        }
    }
}