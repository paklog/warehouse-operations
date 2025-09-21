package com.paklog.warehouse.adapter.rest;

import com.paklog.warehouse.domain.putwall.PutWall;
import com.paklog.warehouse.domain.putwall.PutWallSlot;
import com.paklog.warehouse.domain.putwall.PutWallSlotId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PutWallResponse {
    private final String putWallId;
    private final String location;
    private final int capacity;
    private final int availableCapacity;
    private final List<SlotResponse> slots;

    private PutWallResponse(String putWallId, String location, int capacity,
                           int availableCapacity, List<SlotResponse> slots) {
        this.putWallId = putWallId;
        this.location = location;
        this.capacity = capacity;
        this.availableCapacity = availableCapacity;
        this.slots = slots;
    }

    public static PutWallResponse fromDomain(PutWall putWall) {
        List<SlotResponse> slotResponses = putWall.getAllSlots().entrySet().stream()
            .map(entry -> SlotResponse.fromDomain(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        return new PutWallResponse(
            putWall.getPutWallId().toString(),
            putWall.getLocation(),
            putWall.getCapacity(),
            putWall.getAvailableCapacity(),
            slotResponses
        );
    }

    // Getters
    public String getPutWallId() { return putWallId; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public int getAvailableCapacity() { return availableCapacity; }
    public List<SlotResponse> getSlots() { return slots; }

    public static class SlotResponse {
        private final String slotId;
        private final String status;
        private final String assignedOrderId;
        private final Map<String, Integer> itemsRequired;
        private final Map<String, Integer> itemsPlaced;

        private SlotResponse(String slotId, String status, String assignedOrderId,
                           Map<String, Integer> itemsRequired, Map<String, Integer> itemsPlaced) {
            this.slotId = slotId;
            this.status = status;
            this.assignedOrderId = assignedOrderId;
            this.itemsRequired = itemsRequired;
            this.itemsPlaced = itemsPlaced;
        }

        public static SlotResponse fromDomain(PutWallSlotId slotId, PutWallSlot slot) {
            Map<String, Integer> required = slot.getItemsRequired().entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().getValue(),
                    entry -> entry.getValue().getValue()
                ));

            Map<String, Integer> placed = slot.getItemsPlaced().entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().getValue(),
                    entry -> entry.getValue().getValue()
                ));

            return new SlotResponse(
                slotId.getValue(),
                slot.getStatus().name(),
                slot.getAssignedOrderId() != null ? slot.getAssignedOrderId().toString() : null,
                required,
                placed
            );
        }

        // Getters
        public String getSlotId() { return slotId; }
        public String getStatus() { return status; }
        public String getAssignedOrderId() { return assignedOrderId; }
        public Map<String, Integer> getItemsRequired() { return itemsRequired; }
        public Map<String, Integer> getItemsPlaced() { return itemsPlaced; }
    }
}