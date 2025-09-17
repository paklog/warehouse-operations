package com.paklog.warehouse.domain.shared;

import java.util.Objects;

public class Location {
    private final String zone;
    private final String aisle;
    private final String shelf;

    public Location(String zone, String aisle, String shelf) {
        this.zone = Objects.requireNonNull(zone, "Zone cannot be null");
        this.aisle = Objects.requireNonNull(aisle, "Aisle cannot be null");
        this.shelf = Objects.requireNonNull(shelf, "Shelf cannot be null");
    }

    public String getZone() { return zone; }
    public String getAisle() { return aisle; }
    public String getShelf() { return shelf; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(zone, location.zone) &&
               Objects.equals(aisle, location.aisle) &&
               Objects.equals(shelf, location.shelf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zone, aisle, shelf);
    }

    @Override
    public String toString() {
        return zone + "-" + aisle + "-" + shelf;
    }
}