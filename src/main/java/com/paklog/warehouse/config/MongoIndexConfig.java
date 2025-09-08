package com.paklog.warehouse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

import com.paklog.warehouse.adapter.persistence.mongodb.WaveDocument;
import com.paklog.warehouse.adapter.persistence.mongodb.PickListDocument;
import com.paklog.warehouse.adapter.persistence.mongodb.PackageDocument;

import org.bson.Document;

/**
 * Configuration class to ensure MongoDB indexes are created at application startup
 * This complements the @Indexed annotations in the document classes
 */
@Component
public class MongoIndexConfig implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        createWaveIndexes();
        createPickListIndexes();
        createPackageIndexes();
    }

    private void createWaveIndexes() {
        // Single field indexes (complement @Indexed annotations)
        mongoTemplate.indexOps(WaveDocument.class)
            .ensureIndex(new Index("status", Sort.Direction.ASC));
        
        mongoTemplate.indexOps(WaveDocument.class)
            .ensureIndex(new Index("orderIds", Sort.Direction.ASC));
        
        mongoTemplate.indexOps(WaveDocument.class)
            .ensureIndex(new Index("carrier", Sort.Direction.ASC));
        
        // Compound indexes for complex queries
        mongoTemplate.indexOps(WaveDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("status", 1)
                .append("plannedDate", 1)));
        
        mongoTemplate.indexOps(WaveDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("carrier", 1)
                .append("status", 1)
                .append("createdAt", -1)));
        
        mongoTemplate.indexOps(WaveDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("cutoffTime", 1)
                .append("status", 1)));
        
        // Text index for search functionality
        mongoTemplate.indexOps(WaveDocument.class)
            .ensureIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("carrier")
                .onField("shippingSpeedCategory")
                .build());
    }

    private void createPickListIndexes() {
        // Single field indexes
        mongoTemplate.indexOps(PickListDocument.class)
            .ensureIndex(new Index("orderId", Sort.Direction.ASC));
        
        mongoTemplate.indexOps(PickListDocument.class)
            .ensureIndex(new Index("assignedPickerId", Sort.Direction.ASC));
        
        mongoTemplate.indexOps(PickListDocument.class)
            .ensureIndex(new Index("warehouseZone", Sort.Direction.ASC));
        
        // Compound indexes for picker optimization
        mongoTemplate.indexOps(PickListDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("status", 1)
                .append("priority", -1)
                .append("createdAt", 1)));
        
        mongoTemplate.indexOps(PickListDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("assignedPickerId", 1)
                .append("status", 1)
                .append("priority", -1)));
        
        mongoTemplate.indexOps(PickListDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("warehouseZone", 1)
                .append("status", 1)
                .append("priority", -1)));
        
        // Performance tracking indexes
        mongoTemplate.indexOps(PickListDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("completedAt", -1)
                .append("allInstructionsCompleted", 1)));
    }

    private void createPackageIndexes() {
        // Single field indexes
        mongoTemplate.indexOps(PackageDocument.class)
            .ensureIndex(new Index("orderId", Sort.Direction.ASC));
        
        mongoTemplate.indexOps(PackageDocument.class)
            .ensureIndex(new Index("trackingNumber", Sort.Direction.ASC)
                .unique());
        
        mongoTemplate.indexOps(PackageDocument.class)
            .ensureIndex(new Index("carrier", Sort.Direction.ASC));
        
        // Compound indexes for shipping operations
        mongoTemplate.indexOps(PackageDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("carrier", 1)
                .append("status", 1)
                .append("createdAt", -1)));
        
        mongoTemplate.indexOps(PackageDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("status", 1)
                .append("totalWeight", 1)
                .append("totalVolume", 1)));
        
        // Time-based indexes for analytics
        mongoTemplate.indexOps(PackageDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("shippedAt", -1)
                .append("carrier", 1)));
        
        mongoTemplate.indexOps(PackageDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("deliveredAt", -1)
                .append("carrier", 1)));
        
        // Package characteristics index
        mongoTemplate.indexOps(PackageDocument.class)
            .ensureIndex(new CompoundIndexDefinition(new Document()
                .append("packageType", 1)
                .append("packageSize", 1)
                .append("status", 1)));
    }
}