package com.paklog.warehouse.domain.work;

public enum WorkAction {
    NAVIGATE_TO_LOCATION("Navigate to specified location"),
    SCAN_LOCATION("Scan location barcode for verification"),
    SCAN_ITEM("Scan item barcode for verification"),
    CONFIRM_QUANTITY("Confirm quantity picked/put"),
    PICK_ITEM("Pick specified quantity from location"),
    PUT_ITEM("Put specified quantity to location"),
    VALIDATE_CONDITION("Validate item condition"),
    WEIGH_ITEM("Weigh item for verification"),
    TAKE_PHOTO("Take photo for documentation"),
    SUPERVISOR_APPROVAL("Require supervisor approval"),
    PRINT_LABEL("Print location or item label"),
    UPDATE_SYSTEM("Update system with completion");

    private final String description;

    WorkAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresUserInput() {
        return this == CONFIRM_QUANTITY || this == VALIDATE_CONDITION || 
               this == WEIGH_ITEM || this == TAKE_PHOTO;
    }

    public boolean requiresScanning() {
        return this == SCAN_LOCATION || this == SCAN_ITEM;
    }

    public boolean requiresSupervisor() {
        return this == SUPERVISOR_APPROVAL;
    }

    public boolean isSystemAction() {
        return this == UPDATE_SYSTEM || this == PRINT_LABEL;
    }
}