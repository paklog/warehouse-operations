package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.wave.WaveStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Alternative approach using reference pattern for large order arrays
 * This prevents document size issues when waves contain many orders
 */
@Document(collection = "waves")
public class WaveDocumentWithReferences {
    
    @Id
    private String id;
    
    @Indexed
    private WaveStatus status;
    
    // Reference to separate OrderBucket documents instead of embedding all orders
    private List<String> orderBucketIds;
    
    private int totalOrderCount; // Denormalized for quick access
    
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
    private Long version;
    
    private Instant createdAt;
    private Instant updatedAt;
    
    // Constructors, getters, setters...
}

/**
 * Separate collection for order buckets to handle large order arrays
 */
@Document(collection = "order_buckets")
class OrderBucketDocument {
    
    @Id
    private String id;
    
    @Indexed
    private String waveId; // Reference back to wave
    
    private List<String> orderIds; // Limit to ~1000 orders per bucket
    
    private int bucketSequence; // For ordering buckets within a wave
    
    private Instant createdAt;
    
    // Constructors, getters, setters...
}