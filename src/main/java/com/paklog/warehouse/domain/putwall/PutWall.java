package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.*;
import java.util.stream.Collectors;

public class PutWall extends AggregateRoot {
    private final PutWallId putWallId;
    private final Map<PutWallSlotId, PutWallSlot> slots;
    private final int capacity;
    private final String location;

    public PutWall(PutWallId putWallId, List<PutWallSlotId> slotIds, String location) {
        this.putWallId = Objects.requireNonNull(putWallId, "PutWall ID cannot be null");
        this.location = Objects.requireNonNull(location, "Location cannot be null");

        if (slotIds == null || slotIds.isEmpty()) {
            throw new IllegalArgumentException("PutWall must have at least one slot");
        }

        this.capacity = slotIds.size();
        this.slots = new HashMap<>();

        for (PutWallSlotId slotId : slotIds) {
            this.slots.put(slotId, new PutWallSlot(slotId));
        }
    }

    public Optional<PutWallSlotId> assignOrderToSlot(OrderId orderId, Map<SkuCode, Quantity> requiredItems) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (requiredItems == null || requiredItems.isEmpty()) {
            throw new IllegalArgumentException("Required items cannot be null or empty");
        }

        Optional<PutWallSlot> freeSlot = slots.values().stream()
            .filter(PutWallSlot::isFree)
            .findFirst();

        if (freeSlot.isEmpty()) {
            throw new PutWallException.PutWallCapacityExceededException();
        }

        PutWallSlot slot = freeSlot.get();
        slot.assignToOrder(orderId, requiredItems);

        registerEvent(new OrderAssignedToSlotEvent(
            putWallId,
            slot.getSlotId(),
            orderId,
            requiredItems
        ));

        return Optional.of(slot.getSlotId());
    }

    public void placeItemInSlot(PutWallSlotId slotId, SkuCode skuCode, Quantity quantity) {
        PutWallSlot slot = getSlot(slotId);

        slot.placeItem(skuCode, quantity);

        registerEvent(new ItemPlacedInSlotEvent(
            putWallId,
            slotId,
            slot.getAssignedOrderId(),
            skuCode,
            quantity
        ));

        if (slot.isOrderComplete()) {
            slot.markReadyForPack();
            registerEvent(new OrderConsolidatedInSlotEvent(
                putWallId,
                slotId,
                slot.getAssignedOrderId()
            ));
        }
    }

    public void releaseSlot(PutWallSlotId slotId) {
        PutWallSlot slot = getSlot(slotId);

        if (!slot.isReadyForPack()) {
            throw new IllegalStateException("Slot must be ready for pack before release");
        }

        OrderId releasedOrderId = slot.getAssignedOrderId();
        slot.release();

        registerEvent(new SlotReleasedEvent(
            putWallId,
            slotId,
            releasedOrderId
        ));
    }

    public Optional<PutWallSlotId> findSlotForOrder(OrderId orderId) {
        return slots.values().stream()
            .filter(slot -> orderId.equals(slot.getAssignedOrderId()))
            .map(PutWallSlot::getSlotId)
            .findFirst();
    }

    public List<PutWallSlotId> getReadyForPackSlots() {
        return slots.values().stream()
            .filter(PutWallSlot::isReadyForPack)
            .map(PutWallSlot::getSlotId)
            .collect(Collectors.toList());
    }

    public List<PutWallSlotId> getFreeSlots() {
        return slots.values().stream()
            .filter(PutWallSlot::isFree)
            .map(PutWallSlot::getSlotId)
            .collect(Collectors.toList());
    }

    public int getAvailableCapacity() {
        return (int) slots.values().stream()
            .filter(PutWallSlot::isFree)
            .count();
    }

    public boolean isFull() {
        return getAvailableCapacity() == 0;
    }

    private PutWallSlot getSlot(PutWallSlotId slotId) {
        PutWallSlot slot = slots.get(slotId);
        if (slot == null) {
            throw new PutWallException.SlotNotFoundException(slotId);
        }
        return slot;
    }

    // Getters
    public PutWallId getPutWallId() {
        return putWallId;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getLocation() {
        return location;
    }

    public PutWallSlot getSlotById(PutWallSlotId slotId) {
        return getSlot(slotId);
    }

    public Map<PutWallSlotId, PutWallSlot> getAllSlots() {
        return new HashMap<>(slots);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PutWall putWall = (PutWall) o;
        return Objects.equals(putWallId, putWall.putWallId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(putWallId);
    }

    @Override
    public String toString() {
        return "PutWall{" +
                "putWallId=" + putWallId +
                ", capacity=" + capacity +
                ", location='" + location + '\'' +
                ", availableSlots=" + getAvailableCapacity() +
                '}';
    }
}