package com.paklog.warehouse.domain.work;

public enum ValidationType {
    NONE("No validation required"),
    BARCODE_SCAN("Barcode scan validation"),
    LOCATION_SCAN("Location barcode validation"),
    NUMERIC_INPUT("Numeric value validation"),
    TEXT_INPUT("Text input validation"),
    WEIGHT_CHECK("Weight validation"),
    DIMENSION_CHECK("Dimension validation"),
    PHOTO_CAPTURE("Photo documentation"),
    SUPERVISOR_CODE("Supervisor authorization code"),
    QUANTITY_RANGE("Quantity within acceptable range"),
    CONDITION_CHECK("Visual condition inspection");

    private final String description;

    ValidationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresInput() {
        return this != NONE;
    }

    public boolean requiresSpecialEquipment() {
        return this == WEIGHT_CHECK || this == DIMENSION_CHECK || this == PHOTO_CAPTURE;
    }

    public boolean requiresAuthorization() {
        return this == SUPERVISOR_CODE;
    }
}