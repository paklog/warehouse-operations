package com.paklog.warehouse.domain.wave;

import com.paklog.warehouse.domain.shared.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Wave extends AggregateRoot {
    private final WaveId id;
    private WaveStatus status;
    private final List<OrderId> orderIds;
    private Instant plannedDate;
    private Instant releaseDate;
    private Instant closedDate;
    private String carrier;
    private Instant cutoffTime;
    private String shippingSpeedCategory;
    private int maxOrders;
    private long version;
    
    public Wave(List<OrderId> orderIds) {
        this.id = WaveId.generate();
        this.status = WaveStatus.PLANNED;
        this.orderIds = new ArrayList<>(Objects.requireNonNull(orderIds, "Order IDs cannot be null"));
        this.plannedDate = Instant.now();
        this.version = 0;
        
        if (orderIds.isEmpty()) {
            throw new IllegalArgumentException("Wave must contain at least one order");
        }
    }

    // Constructor for repository adapter
    public Wave(WaveId id, List<OrderId> orderIds, Instant plannedDate, Instant cutoffTime, 
                String carrier, String shippingSpeedCategory, int maxOrders) {
        this.id = Objects.requireNonNull(id, "Wave ID cannot be null");
        this.status = WaveStatus.PLANNED;
        this.orderIds = new ArrayList<>(Objects.requireNonNull(orderIds, "Order IDs cannot be null"));
        this.plannedDate = plannedDate;
        this.cutoffTime = cutoffTime;
        this.carrier = carrier;
        this.shippingSpeedCategory = shippingSpeedCategory;
        this.maxOrders = maxOrders;
        this.version = 0;
        
        if (orderIds.isEmpty()) {
            throw new IllegalArgumentException("Wave must contain at least one order");
        }
    }

    public void release() {
        if (status != WaveStatus.PLANNED) {
            throw new IllegalStateException("Wave must be in PLANNED state to release, current state: " + status);
        }
        
        this.status = WaveStatus.RELEASED;
        this.releaseDate = Instant.now();
        
        // Publish domain event
        registerEvent(new WaveReleasedEvent(this.id, this.orderIds, this.releaseDate));
    }

    public void close() {
        if (status != WaveStatus.RELEASED) {
            throw new IllegalStateException("Wave must be in RELEASED state to close, current state: " + status);
        }
        
        this.status = WaveStatus.CLOSED;
        this.closedDate = Instant.now();
        
        // Publish domain event  
        registerEvent(new WaveClosedEvent(this.id, this.closedDate));
    }

    public void cancel() {
        if (status == WaveStatus.CLOSED) {
            throw new IllegalStateException("Cannot cancel a closed wave");
        }
        
        this.status = WaveStatus.CANCELLED;
        
        // Publish domain event
        registerEvent(new WaveCancelledEvent(this.id, Instant.now()));
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public void setCutoffTime(Instant cutoffTime) {
        this.cutoffTime = cutoffTime;
    }

    // Additional setters for repository adapter
    public void setStatus(WaveStatus status) {
        this.status = status;
    }

    public void setReleaseDate(Instant releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    // Getters
    public WaveId getId() {
        return id;
    }

    public WaveStatus getStatus() {
        return status;
    }

    public List<OrderId> getOrderIds() {
        return Collections.unmodifiableList(orderIds);
    }

    public int getOrderCount() {
        return orderIds.size();
    }

    public Instant getPlannedDate() {
        return plannedDate;
    }

    public Instant getReleaseDate() {
        return releaseDate;
    }

    public Instant getClosedDate() {
        return closedDate;
    }

    public String getCarrier() {
        return carrier;
    }

    public Instant getCutoffTime() {
        return cutoffTime;
    }

    public boolean isReleased() {
        return status == WaveStatus.RELEASED;
    }

    public boolean isClosed() {
        return status == WaveStatus.CLOSED;
    }

    public boolean isCancelled() {
        return status == WaveStatus.CANCELLED;
    }

    public String getShippingSpeedCategory() {
        return shippingSpeedCategory;
    }

    public int getMaxOrders() {
        return maxOrders;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wave wave = (Wave) o;
        return Objects.equals(id, wave.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Wave{" +
                "id=" + id +
                ", status=" + status +
                ", orderCount=" + orderIds.size() +
                ", plannedDate=" + plannedDate +
                ", releaseDate=" + releaseDate +
                '}';
    }
}