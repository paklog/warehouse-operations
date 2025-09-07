package com.paklog.warehouse.domain.shared;

import java.util.Objects;

public class BinLocation {
    private final String aisle;
    private final String rack;
    private final String level;

    public BinLocation(String aisle, String rack, String level) {
        this.aisle = Objects.requireNonNull(aisle, "Aisle cannot be null");
        this.rack = Objects.requireNonNull(rack, "Rack cannot be null");
        this.level = Objects.requireNonNull(level, "Level cannot be null");
    }

    public static BinLocation of(String location) {
        // Assuming location format is "Aisle-Rack-Level"
        String[] parts = location.split("-");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid bin location format. Use 'Aisle-Rack-Level'");
        }
        return new BinLocation(parts[0], parts[1], parts[2]);
    }

    public static BinLocation of(String aisle, String rack, String level) {
        return new BinLocation(aisle, rack, level);
    }

    public String getAisle() {
        return aisle;
    }

    public String getRack() {
        return rack;
    }

    public String getLevel() {
        return level;
    }

    public String getLocation() {
        return toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinLocation that = (BinLocation) o;
        return aisle.equals(that.aisle) && 
               rack.equals(that.rack) && 
               level.equals(that.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aisle, rack, level);
    }

    @Override
    public String toString() {
        return aisle + "-" + rack + "-" + level;
    }
}