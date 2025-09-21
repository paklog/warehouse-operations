package com.paklog.warehouse.domain.putwall;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class PutWallMetrics {
    private final PutWallId putWallId;
    private int totalOrdersProcessed;
    private int totalItemsPlaced;
    private double averageOrderCompletionTimeMinutes;
    private double throughputOrdersPerHour;
    private double utilizationPercentage;
    private int currentActiveOrders;
    private Instant lastUpdated;

    public PutWallMetrics(PutWallId putWallId) {
        this.putWallId = Objects.requireNonNull(putWallId, "PutWall ID cannot be null");
        this.totalOrdersProcessed = 0;
        this.totalItemsPlaced = 0;
        this.averageOrderCompletionTimeMinutes = 0.0;
        this.throughputOrdersPerHour = 0.0;
        this.utilizationPercentage = 0.0;
        this.currentActiveOrders = 0;
        this.lastUpdated = Instant.now();
    }

    public void recordOrderAssignment() {
        this.currentActiveOrders++;
        this.lastUpdated = Instant.now();
    }

    public void recordItemPlacement() {
        this.totalItemsPlaced++;
        this.lastUpdated = Instant.now();
    }

    public void recordOrderCompletion(Duration completionTime) {
        this.totalOrdersProcessed++;
        this.currentActiveOrders = Math.max(0, this.currentActiveOrders - 1);

        // Update average completion time using running average
        if (totalOrdersProcessed == 1) {
            this.averageOrderCompletionTimeMinutes = completionTime.toMinutes();
        } else {
            double totalMinutes = this.averageOrderCompletionTimeMinutes * (totalOrdersProcessed - 1);
            this.averageOrderCompletionTimeMinutes = (totalMinutes + completionTime.toMinutes()) / totalOrdersProcessed;
        }

        this.lastUpdated = Instant.now();
    }

    public void updateUtilization(int activeSlots, int totalSlots) {
        if (totalSlots > 0) {
            this.utilizationPercentage = (double) activeSlots / totalSlots * 100;
        }
        this.lastUpdated = Instant.now();
    }

    public void updateThroughput(Duration timeWindow) {
        if (timeWindow.toHours() > 0) {
            this.throughputOrdersPerHour = (double) totalOrdersProcessed / timeWindow.toHours();
        }
        this.lastUpdated = Instant.now();
    }

    // Getters
    public PutWallId getPutWallId() {
        return putWallId;
    }

    public int getTotalOrdersProcessed() {
        return totalOrdersProcessed;
    }

    public int getTotalItemsPlaced() {
        return totalItemsPlaced;
    }

    public double getAverageOrderCompletionTimeMinutes() {
        return averageOrderCompletionTimeMinutes;
    }

    public double getThroughputOrdersPerHour() {
        return throughputOrdersPerHour;
    }

    public double getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public int getCurrentActiveOrders() {
        return currentActiveOrders;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PutWallMetrics that = (PutWallMetrics) o;
        return Objects.equals(putWallId, that.putWallId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(putWallId);
    }

    @Override
    public String toString() {
        return "PutWallMetrics{" +
                "putWallId=" + putWallId +
                ", totalOrdersProcessed=" + totalOrdersProcessed +
                ", totalItemsPlaced=" + totalItemsPlaced +
                ", averageOrderCompletionTimeMinutes=" + averageOrderCompletionTimeMinutes +
                ", throughputOrdersPerHour=" + throughputOrdersPerHour +
                ", utilizationPercentage=" + utilizationPercentage +
                ", currentActiveOrders=" + currentActiveOrders +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}