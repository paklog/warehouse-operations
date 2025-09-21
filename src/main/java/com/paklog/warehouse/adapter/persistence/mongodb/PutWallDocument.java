package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.putwall.*;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Document(collection = "putWalls")
public class PutWallDocument {

    @Id
    private UUID putWallId;
    private String location;
    private int capacity;
    private List<PutWallSlotDocument> slots;

    public PutWallDocument() {}

    public PutWallDocument(UUID putWallId, String location, int capacity, List<PutWallSlotDocument> slots) {
        this.putWallId = putWallId;
        this.location = location;
        this.capacity = capacity;
        this.slots = slots;
    }

    public static PutWallDocument fromDomain(PutWall putWall) {
        List<PutWallSlotDocument> slotDocuments = putWall.getAllSlots().entrySet().stream()
            .map(entry -> PutWallSlotDocument.fromDomain(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        return new PutWallDocument(
            putWall.getPutWallId().getValue(),
            putWall.getLocation(),
            putWall.getCapacity(),
            slotDocuments
        );
    }

    public PutWall toDomain() {
        List<PutWallSlotId> slotIds = slots.stream()
            .map(slot -> PutWallSlotId.of(slot.getSlotId()))
            .collect(Collectors.toList());

        PutWall putWall = new PutWall(PutWallId.of(putWallId), slotIds, location);

        // Restore slot states
        for (PutWallSlotDocument slotDoc : slots) {
            if (slotDoc.getAssignedOrderId() != null && !slotDoc.getItemsRequired().isEmpty()) {
                OrderId orderId = OrderId.of(slotDoc.getAssignedOrderId());
                Map<SkuCode, Quantity> requiredItems = slotDoc.getItemsRequired().entrySet().stream()
                    .collect(Collectors.toMap(
                        entry -> SkuCode.of(entry.getKey()),
                        entry -> Quantity.of(entry.getValue())
                    ));

                putWall.assignOrderToSlot(orderId, requiredItems);

                // Place existing items
                PutWallSlotId slotId = PutWallSlotId.of(slotDoc.getSlotId());
                for (Map.Entry<String, Integer> entry : slotDoc.getItemsPlaced().entrySet()) {
                    if (entry.getValue() > 0) {
                        putWall.placeItemInSlot(slotId, SkuCode.of(entry.getKey()), Quantity.of(entry.getValue()));
                    }
                }
            }
        }

        putWall.clearDomainEvents(); // Clear events from reconstruction
        return putWall;
    }

    // Getters and setters
    public UUID getPutWallId() { return putWallId; }
    public void setPutWallId(UUID putWallId) { this.putWallId = putWallId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public List<PutWallSlotDocument> getSlots() { return slots; }
    public void setSlots(List<PutWallSlotDocument> slots) { this.slots = slots; }

    public static class PutWallSlotDocument {
        private String slotId;
        private String assignedOrderId;
        private Map<String, Integer> itemsRequired;
        private Map<String, Integer> itemsPlaced;
        private String status;

        public PutWallSlotDocument() {
            this.itemsRequired = new HashMap<>();
            this.itemsPlaced = new HashMap<>();
        }

        public PutWallSlotDocument(String slotId, String assignedOrderId, Map<String, Integer> itemsRequired,
                                 Map<String, Integer> itemsPlaced, String status) {
            this.slotId = slotId;
            this.assignedOrderId = assignedOrderId;
            this.itemsRequired = itemsRequired != null ? itemsRequired : new HashMap<>();
            this.itemsPlaced = itemsPlaced != null ? itemsPlaced : new HashMap<>();
            this.status = status;
        }

        public static PutWallSlotDocument fromDomain(PutWallSlotId slotId, PutWallSlot slot) {
            Map<String, Integer> requiredItems = slot.getItemsRequired().entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().getValue(),
                    entry -> entry.getValue().getValue()
                ));

            Map<String, Integer> placedItems = slot.getItemsPlaced().entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().getValue(),
                    entry -> entry.getValue().getValue()
                ));

            return new PutWallSlotDocument(
                slotId.getValue(),
                slot.getAssignedOrderId() != null ? slot.getAssignedOrderId().toString() : null,
                requiredItems,
                placedItems,
                slot.getStatus().name()
            );
        }

        // Getters and setters
        public String getSlotId() { return slotId; }
        public void setSlotId(String slotId) { this.slotId = slotId; }
        public String getAssignedOrderId() { return assignedOrderId; }
        public void setAssignedOrderId(String assignedOrderId) { this.assignedOrderId = assignedOrderId; }
        public Map<String, Integer> getItemsRequired() { return itemsRequired; }
        public void setItemsRequired(Map<String, Integer> itemsRequired) { this.itemsRequired = itemsRequired; }
        public Map<String, Integer> getItemsPlaced() { return itemsPlaced; }
        public void setItemsPlaced(Map<String, Integer> itemsPlaced) { this.itemsPlaced = itemsPlaced; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}