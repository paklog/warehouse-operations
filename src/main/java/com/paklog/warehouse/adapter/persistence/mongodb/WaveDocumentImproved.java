package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.wave.Wave;
import com.paklog.warehouse.domain.wave.WaveId;
import com.paklog.warehouse.domain.wave.WaveStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Document(collection = "waves")
@CompoundIndexes({
    @CompoundIndex(name = "status_plannedDate_idx", def = "{'status': 1, 'plannedDate': 1}"),
    @CompoundIndex(name = "carrier_status_idx", def = "{'carrier': 1, 'status': 1}"),
    @CompoundIndex(name = "releaseDate_status_idx", def = "{'releaseDate': 1, 'status': 1}")
})
public class WaveDocumentImproved {
    
    @Id
    private String id;
    
    @Indexed
    private WaveStatus status;
    
    @Indexed
    private List<String> orderIds;
    
    @Indexed
    private Instant plannedDate;
    
    private Instant releaseDate;
    
    @Indexed
    private Instant cutoffTime;
    
    @Indexed
    private String carrier;
    
    @Indexed
    private String shippingSpeedCategory;
    
    private int maxOrders;
    
    @Version
    private Long version; // Use @Version for optimistic locking
    
    // Audit fields following MongoDB best practices
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public WaveDocumentImproved() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public WaveDocumentImproved(Wave wave) {
        this();
        this.id = wave.getId().getValue().toString();
        this.status = wave.getStatus();
        this.orderIds = wave.getOrderIds().stream()
                .map(orderId -> orderId.getValue().toString())
                .collect(Collectors.toList());
        this.plannedDate = wave.getPlannedDate();
        this.releaseDate = wave.getReleaseDate();
        this.cutoffTime = wave.getCutoffTime();
        this.carrier = wave.getCarrier();
        this.shippingSpeedCategory = wave.getShippingSpeedCategory();
        this.maxOrders = wave.getMaxOrders();
        this.version = wave.getVersion();
        this.updatedAt = Instant.now();
    }

    public Wave toDomain() {
        List<OrderId> domainOrderIds = orderIds.stream()
                .map(OrderId::of)
                .collect(Collectors.toList());

        Wave wave = new Wave(
                WaveId.of(id),
                domainOrderIds,
                plannedDate,
                cutoffTime,
                carrier,
                shippingSpeedCategory,
                maxOrders
        );
        
        if (releaseDate != null) {
            wave.setReleaseDate(releaseDate);
        }
        wave.setStatus(status);
        wave.setVersion(version != null ? version : 0L);
        
        return wave;
    }
    
    // Getters and setters with update timestamp handling
    public void setStatus(WaveStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
    
    public void setCarrier(String carrier) {
        this.carrier = carrier;
        this.updatedAt = Instant.now();
    }

    // ... other getters and setters with updatedAt handling
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaveDocumentImproved that = (WaveDocumentImproved) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}