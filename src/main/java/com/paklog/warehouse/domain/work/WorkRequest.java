package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;

import java.util.Objects;

public class WorkRequest {
    private final BinLocation location;
    private final SkuCode item;
    private final Quantity quantity;
    private final String assignedTo;
    private final String priority;

    public WorkRequest(BinLocation location, SkuCode item, Quantity quantity, String assignedTo) {
        this(location, item, quantity, assignedTo, "NORMAL");
    }

    public WorkRequest(BinLocation location, SkuCode item, Quantity quantity, 
                      String assignedTo, String priority) {
        this.location = Objects.requireNonNull(location, "Location cannot be null");
        this.item = Objects.requireNonNull(item, "Item cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
        this.assignedTo = assignedTo; // Can be null for unassigned work
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
    }

    public BinLocation getLocation() {
        return location;
    }

    public SkuCode getItem() {
        return item;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkRequest that = (WorkRequest) o;
        return Objects.equals(location, that.location) &&
               Objects.equals(item, that.item) &&
               Objects.equals(quantity, that.quantity) &&
               Objects.equals(assignedTo, that.assignedTo) &&
               Objects.equals(priority, that.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, item, quantity, assignedTo, priority);
    }

    @Override
    public String toString() {
        return "WorkRequest{" +
                "location=" + location +
                ", item=" + item +
                ", quantity=" + quantity +
                ", assignedTo='" + assignedTo + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }
}