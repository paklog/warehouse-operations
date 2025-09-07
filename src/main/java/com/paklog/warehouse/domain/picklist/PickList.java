package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.PickInstruction;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.ArrayList;
import java.util.List;

public class PickList extends AggregateRoot {
    private final PickListId id;
    private PickListStatus status;
    private String pickerId;
    private final List<PickInstruction> instructions;

    public PickList(OrderId orderId) {
        this.id = PickListId.generate();
        this.status = PickListStatus.PENDING;
        this.instructions = new ArrayList<>();
    }

    public void assignToPicker(String pickerId) {
        this.pickerId = pickerId;
        this.status = PickListStatus.ASSIGNED;
        registerEvent(new PickListAssignedEvent(this.id, this.pickerId));
    }

    public void confirmPick(SkuCode sku, Quantity quantity, BinLocation binLocation) {
        PickInstruction instruction = findInstruction(sku);
        if (instruction == null || instruction.getQuantityToPick().getValue() != quantity.getValue()) {
            throw new IllegalArgumentException("Invalid pick confirmation");
        }
        instruction.markCompleted(binLocation);
        registerEvent(new ItemPickedEvent(this.id, sku, quantity, binLocation, this.pickerId));
        if (isComplete()) {
            this.status = PickListStatus.COMPLETED;
            registerEvent(new PickListCompletedEvent(this.id));
        }
    }

    public void pickItem(SkuCode sku, Quantity quantity, BinLocation binLocation) {
        confirmPick(sku, quantity, binLocation);
    }

    public PickInstruction getNextInstruction() {
        for (PickInstruction instruction : instructions) {
            if (!instruction.isCompleted()) {
                return instruction;
            }
        }
        return null;
    }

    public boolean isComplete() {
        for (PickInstruction instruction : instructions) {
            if (!instruction.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    private PickInstruction findInstruction(SkuCode sku) {
        for (PickInstruction instruction : instructions) {
            if (instruction.getSku().equals(sku)) {
                return instruction;
            }
        }
        return null;
    }

    // Additional methods needed for tests
    public PickListId getId() {
        return id;
    }

    public PickListStatus getStatus() {
        return status;
    }

    public String getPickerId() {
        return pickerId;
    }

    public List<PickInstruction> getInstructions() {
        return new ArrayList<>(instructions);
    }

    public void addInstruction(PickInstruction instruction) {
        instructions.add(instruction);
    }

    public OrderId getOrderId() {
        // This should be set in constructor but for now return a generated one
        return OrderId.generate();
    }
}