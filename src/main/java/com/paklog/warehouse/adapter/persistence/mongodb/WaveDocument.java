package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.wave.Wave;
import com.paklog.warehouse.domain.wave.WaveId;
import com.paklog.warehouse.domain.wave.WaveStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Document(collection = "waves")
@CompoundIndexes({
    @CompoundIndex(name = "status_plannedDate_idx", def = "{'status': 1, 'plannedDate': 1}"),
    @CompoundIndex(name = "carrier_status_idx", def = "{'carrier': 1, 'status': 1}"),
    @CompoundIndex(name = "releaseDate_status_idx", def = "{'releaseDate': 1, 'status': 1}"),
    @CompoundIndex(name = "cutoffTime_status_idx", def = "{'cutoffTime': 1, 'status': 1}")
})
public class WaveDocument {
    
    @Id
    private String id;
    
    @Indexed
    private WaveStatus status;
    
    @Indexed
    private List<String> orderIds;
    
    @Indexed
    private Date plannedDate;
    
    private Date releaseDate;
    
    @Indexed
    private Date cutoffTime;
    
    @Indexed
    private String carrier;
    
    @Indexed
    private String shippingSpeedCategory;
    
    private int maxOrders;
    
    @Version
    private Long version;
    
    // Summary fields for quick access without scanning orderIds array
    private int totalOrders;
    private int maxOrdersPerWave;
    
    // Audit fields
    @CreatedDate
    private Date createdAt;
    
    @LastModifiedDate
    private Date updatedAt;
    
    private String createdBy;
    private String updatedBy;

    public WaveDocument() {}

    public WaveDocument(Wave wave) {
        this.id = wave.getId().getValue().toString();
        this.status = wave.getStatus();
        this.orderIds = wave.getOrderIds().stream()
                .map(orderId -> orderId.getValue().toString())
                .collect(Collectors.toList());
        this.plannedDate = wave.getPlannedDate() != null ? Date.from(wave.getPlannedDate()) : null;
        this.releaseDate = wave.getReleaseDate() != null ? Date.from(wave.getReleaseDate()) : null;
        this.cutoffTime = wave.getCutoffTime() != null ? Date.from(wave.getCutoffTime()) : null;
        this.carrier = wave.getCarrier();
        this.shippingSpeedCategory = wave.getShippingSpeedCategory();
        this.maxOrders = wave.getMaxOrders();
        this.version = wave.getVersion();
        
        // Set summary fields
        this.totalOrders = wave.getOrderCount();
        this.maxOrdersPerWave = wave.getMaxOrders();
        
        // Audit fields will be set by Spring Data MongoDB
    }

    public Wave toDomain() {
        List<OrderId> domainOrderIds = orderIds.stream()
                .map(OrderId::of)
                .collect(Collectors.toList());

        Wave wave = new Wave(
                WaveId.of(id),
                domainOrderIds,
                plannedDate != null ? plannedDate.toInstant() : null,
                cutoffTime != null ? cutoffTime.toInstant() : null,
                carrier,
                shippingSpeedCategory,
                maxOrders
        );
        
        // Set additional fields that might be set after construction
        if (releaseDate != null) {
            wave.setReleaseDate(releaseDate.toInstant());
        }
        wave.setStatus(status);
        wave.setVersion(version != null ? version : 0L);
        
        return wave;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WaveStatus getStatus() {
        return status;
    }

    public void setStatus(WaveStatus status) {
        this.status = status;
    }

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    public Date getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(Date plannedDate) {
        this.plannedDate = plannedDate;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Date getCutoffTime() {
        return cutoffTime;
    }

    public void setCutoffTime(Date cutoffTime) {
        this.cutoffTime = cutoffTime;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getShippingSpeedCategory() {
        return shippingSpeedCategory;
    }

    public void setShippingSpeedCategory(String shippingSpeedCategory) {
        this.shippingSpeedCategory = shippingSpeedCategory;
    }

    public int getMaxOrders() {
        return maxOrders;
    }

    public void setMaxOrders(int maxOrders) {
        this.maxOrders = maxOrders;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getMaxOrdersPerWave() {
        return maxOrdersPerWave;
    }

    public void setMaxOrdersPerWave(int maxOrdersPerWave) {
        this.maxOrdersPerWave = maxOrdersPerWave;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaveDocument that = (WaveDocument) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}