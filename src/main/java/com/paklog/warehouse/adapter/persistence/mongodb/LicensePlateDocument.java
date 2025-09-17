package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.licenseplate.*;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Document(collection = "license_plates")
public class LicensePlateDocument {
    @Id
    private String id;
    private String licensePlateId;
    private String status;
    private String type;
    private BinLocationDocument currentLocation;
    private String parentLicensePlateId;
    private Map<String, Integer> inventory; // SKU code to quantity mapping
    private Set<String> childLicensePlates;
    private String receivingReference;
    private String shipmentReference;
    private Instant createdAt;
    private Instant receivedAt;
    private Instant shippedAt;
    private String createdBy;
    private String lastMovedBy;
    private Instant lastMovedAt;
    private Map<String, Object> attributes;
    private int version;

    // Constructors
    public LicensePlateDocument() {}

    public LicensePlateDocument(LicensePlate licensePlate) {
        this.id = licensePlate.getLicensePlateId().getValue().toString();
        this.licensePlateId = licensePlate.getLicensePlateId().getValue().toString();
        this.status = licensePlate.getStatus().name();
        this.type = licensePlate.getType().name();
        this.currentLocation = licensePlate.getCurrentLocation() != null ? 
            new BinLocationDocument(licensePlate.getCurrentLocation()) : null;
        this.parentLicensePlateId = licensePlate.getParentLicensePlateId();
        this.inventory = licensePlate.getInventory().entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().getValue(),
                entry -> entry.getValue().getValue()
            ));
        this.childLicensePlates = licensePlate.getChildLicensePlates();
        this.receivingReference = licensePlate.getReceivingReference();
        this.shipmentReference = licensePlate.getShipmentReference();
        this.createdAt = licensePlate.getCreatedAt();
        this.receivedAt = licensePlate.getReceivedAt();
        this.shippedAt = licensePlate.getShippedAt();
        this.createdBy = licensePlate.getCreatedBy();
        this.lastMovedBy = licensePlate.getLastMovedBy();
        this.lastMovedAt = licensePlate.getLastMovedAt();
        this.attributes = licensePlate.getAttributes();
        this.version = licensePlate.getVersion();
    }

    public LicensePlate toDomain() {
        LicensePlateId licensePlateId = LicensePlateId.of(this.licensePlateId);
        LicensePlateStatus status = LicensePlateStatus.valueOf(this.status);
        LicensePlateType type = LicensePlateType.valueOf(this.type);
        BinLocation location = this.currentLocation != null ? this.currentLocation.toDomain() : null;
        
        Map<SkuCode, Quantity> domainInventory = this.inventory.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> SkuCode.of(entry.getKey()),
                entry -> new Quantity(entry.getValue())
            ));

        return new LicensePlate(
            licensePlateId, status, type, location, this.parentLicensePlateId,
            domainInventory, this.childLicensePlates, this.receivingReference,
            this.shipmentReference, this.createdAt, this.receivedAt, this.shippedAt,
            this.createdBy, this.lastMovedBy, this.lastMovedAt, this.attributes, this.version
        );
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLicensePlateId() { return licensePlateId; }
    public void setLicensePlateId(String licensePlateId) { this.licensePlateId = licensePlateId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BinLocationDocument getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(BinLocationDocument currentLocation) { this.currentLocation = currentLocation; }

    public String getParentLicensePlateId() { return parentLicensePlateId; }
    public void setParentLicensePlateId(String parentLicensePlateId) { this.parentLicensePlateId = parentLicensePlateId; }

    public Map<String, Integer> getInventory() { return inventory; }
    public void setInventory(Map<String, Integer> inventory) { this.inventory = inventory; }

    public Set<String> getChildLicensePlates() { return childLicensePlates; }
    public void setChildLicensePlates(Set<String> childLicensePlates) { this.childLicensePlates = childLicensePlates; }

    public String getReceivingReference() { return receivingReference; }
    public void setReceivingReference(String receivingReference) { this.receivingReference = receivingReference; }

    public String getShipmentReference() { return shipmentReference; }
    public void setShipmentReference(String shipmentReference) { this.shipmentReference = shipmentReference; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }

    public Instant getShippedAt() { return shippedAt; }
    public void setShippedAt(Instant shippedAt) { this.shippedAt = shippedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getLastMovedBy() { return lastMovedBy; }
    public void setLastMovedBy(String lastMovedBy) { this.lastMovedBy = lastMovedBy; }

    public Instant getLastMovedAt() { return lastMovedAt; }
    public void setLastMovedAt(Instant lastMovedAt) { this.lastMovedAt = lastMovedAt; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    // Nested document class for BinLocation
    public static class BinLocationDocument {
        private String aisle;
        private String rack;
        private String level;

        public BinLocationDocument() {}

        public BinLocationDocument(BinLocation location) {
            this.aisle = location.getAisle();
            this.rack = location.getRack();
            this.level = location.getLevel();
        }

        public BinLocation toDomain() {
            return BinLocation.of(aisle, rack, level);
        }

        // Getters and setters
        public String getAisle() { return aisle; }
        public void setAisle(String aisle) { this.aisle = aisle; }

        public String getRack() { return rack; }
        public void setRack(String rack) { this.rack = rack; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }
}