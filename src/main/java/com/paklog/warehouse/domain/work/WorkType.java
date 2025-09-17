package com.paklog.warehouse.domain.work;

public enum WorkType {
    PICK("Pick items from inventory locations"),
    PUT("Put items into designated locations"), 
    MOVE("Move items between locations"),
    COUNT("Count inventory for cycle counting"),
    PACK("Pack items into containers"),
    REPLENISH("Replenish picking locations");

    private final String description;

    WorkType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresInventoryMovement() {
        return this == PICK || this == PUT || this == MOVE || this == REPLENISH;
    }

    public boolean requiresQuantityValidation() {
        return this == PICK || this == COUNT || this == PACK;
    }

    public boolean requiresLocationValidation() {
        return this == PICK || this == PUT || this == MOVE || this == REPLENISH;
    }
}