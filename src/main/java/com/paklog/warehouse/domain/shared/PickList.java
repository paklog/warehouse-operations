package com.paklog.warehouse.domain.shared;

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

    public PickList(OrderId orderId) {
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.id = PickListId.generate();
        this.status = PickListStatus.PENDING;
        this.instructions = new ArrayList<>();
    }

    public void assignToPicker(String pickerId) {
        this.pickerId = Objects.requireNonNull(pickerId, "Picker ID cannot be null");
        this.status = PickListStatus.ASSIGNED;
        registerEvent(new PickListAssignedEvent(this.id, this.pickerId));
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
}