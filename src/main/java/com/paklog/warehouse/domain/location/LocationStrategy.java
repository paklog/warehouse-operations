package com.paklog.warehouse.domain.location;

public enum LocationStrategy {
    FIXED("Use pre-defined fixed location"),
    NEAREST_EMPTY("Select nearest available empty location"),
    BULK_LOCATION("Prefer bulk storage locations"),
    FAST_MOVING("Prefer fast-moving pick locations"),
    ZONE_BASED("Select based on product zone classification"),
    CAPACITY_OPTIMIZED("Select based on available capacity"),
    FIFO("First In, First Out location selection"),
    LIFO("Last In, First Out location selection"),
    RANDOM("Random available location selection"),
    LOWEST_LEVEL("Prefer lower level locations"),
    HIGHEST_LEVEL("Prefer higher level locations");

    private final String description;

    LocationStrategy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresInventoryData() {
        return this == FIFO || this == LIFO || this == CAPACITY_OPTIMIZED;
    }

    public boolean requiresZoneConfiguration() {
        return this == ZONE_BASED || this == FAST_MOVING || this == BULK_LOCATION;
    }

    public boolean isDistanceBased() {
        return this == NEAREST_EMPTY;
    }

    public boolean isLevelBased() {
        return this == LOWEST_LEVEL || this == HIGHEST_LEVEL;
    }

    public boolean requiresFixedMapping() {
        return this == FIXED;
    }
}