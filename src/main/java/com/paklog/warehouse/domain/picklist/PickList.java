package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PickList extends AggregateRoot {
    private final PickListId id;
    private PickListStatus status;
    private final List<PickInstruction> instructions;
    private String pickerId;
    private final OrderId orderId;
    private Instant assignedAt;
    private Instant createdAt;
    private Instant completedAt;

    public PickList(OrderId orderId) {
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.id = PickListId.generate();
        this.status = PickListStatus.PENDING;
        this.instructions = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    // Constructor for repository adapter
    public PickList(PickListId id, OrderId orderId, List<PickInstruction> instructions) {
        this.id = Objects.requireNonNull(id, "PickList ID cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.status = PickListStatus.PENDING;
        this.instructions = new ArrayList<>(Objects.requireNonNull(instructions, "Instructions cannot be null"));
        this.createdAt = Instant.now();
    }

    public void assignToPicker(String pickerId) {
        this.pickerId = Objects.requireNonNull(pickerId, "Picker ID cannot be null");
        this.status = PickListStatus.ASSIGNED;
        this.assignedAt = Instant.now();
        registerEvent(new PickListAssignedEvent(this.id, this.pickerId));
    }

    // Method for adapter
    public void assignTo(String pickerId) {
        this.pickerId = pickerId;
        if (pickerId != null) {
            this.status = PickListStatus.ASSIGNED;
            this.assignedAt = Instant.now();
        }
    }

    public void addInstruction(PickInstruction instruction) {
        Objects.requireNonNull(instruction, "Pick instruction cannot be null");
        instructions.add(instruction);
    }

    public void pickItem(SkuCode sku, Quantity quantity, BinLocation binLocation) {
        PickInstruction instruction = findInstruction(sku);
        
        if (instruction == null || instruction.getQuantity().getValue() != quantity.getValue()) {
            throw new IllegalArgumentException("Invalid pick instruction");
        }
        
        instruction.markCompleted();
        registerEvent(new ItemPickedEvent(this.id, sku, quantity, binLocation, this.pickerId));
        
        if (areAllInstructionsCompleted()) {
            this.status = PickListStatus.COMPLETED;
            this.completedAt = Instant.now();
            registerEvent(new PickListCompletedEvent(this.id));
        }
    }

    private PickInstruction findInstruction(SkuCode sku) {
        return instructions.stream()
            .filter(instruction -> instruction.getSku().equals(sku))
            .findFirst()
            .orElse(null);
    }

    private boolean areAllInstructionsCompleted() {
        return instructions.stream().allMatch(PickInstruction::isCompleted);
    }

    public PickListId getId() {
        return id;
    }

    public PickListStatus getStatus() {
        return status;
    }

    public List<PickInstruction> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }

    public String getPickerId() {
        return pickerId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public boolean isComplete() {
        return status == PickListStatus.COMPLETED;
    }

    // Additional getters for repository adapter
    public String getAssignedPickerId() {
        return pickerId;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    // Setters for repository adapter
    public void setStatus(PickListStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}