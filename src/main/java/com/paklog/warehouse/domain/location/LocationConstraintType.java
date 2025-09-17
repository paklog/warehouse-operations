package com.paklog.warehouse.domain.location;

public enum LocationConstraintType {
    ZONE_RESTRICTION("Restrict to specific warehouse zones"),
    CAPACITY_REQUIREMENT("Minimum capacity requirement"),
    ACCESSIBILITY("Location accessibility level"),
    EQUIPMENT_REQUIREMENT("Required equipment at location"),
    SAFETY_RESTRICTION("Safety classification requirement"),
    TEMPERATURE_RANGE("Temperature control requirement"),
    HAZMAT_COMPATIBLE("Hazardous material compatibility"),
    INVENTORY_AVAILABLE("Minimum available inventory"),
    HEIGHT_RESTRICTION("Height limitation constraint"),
    WEIGHT_RESTRICTION("Weight capacity constraint");

    private final String description;

    LocationConstraintType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPhysicalConstraint() {
        return this == HEIGHT_RESTRICTION || this == WEIGHT_RESTRICTION || 
               this == CAPACITY_REQUIREMENT;
    }

    public boolean isEnvironmentalConstraint() {
        return this == TEMPERATURE_RANGE || this == HAZMAT_COMPATIBLE || 
               this == SAFETY_RESTRICTION;
    }

    public boolean isOperationalConstraint() {
        return this == EQUIPMENT_REQUIREMENT || this == ACCESSIBILITY || 
               this == INVENTORY_AVAILABLE;
    }

    public boolean isZoneConstraint() {
        return this == ZONE_RESTRICTION;
    }
}