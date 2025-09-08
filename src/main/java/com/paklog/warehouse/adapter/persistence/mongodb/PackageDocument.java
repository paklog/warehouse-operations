package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.packaging.Package;
import com.paklog.warehouse.domain.packaging.PackageStatus;
import com.paklog.warehouse.domain.packaging.PackedItem;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Document(collection = "packages")
@CompoundIndexes({
    @CompoundIndex(name = "orderId_status_idx", def = "{'orderId': 1, 'status': 1}"),
    @CompoundIndex(name = "status_createdAt_idx", def = "{'status': 1, 'createdAt': 1}"),
    @CompoundIndex(name = "carrier_status_idx", def = "{'carrier': 1, 'status': 1}")
})
public class PackageDocument {

    @Id
    private String id;
    
    @Indexed
    private String orderId;
    
    @Indexed
    private PackageStatus status;
    
    private List<PackedItemDocument> packedItems;
    
    // Summary fields
    private int totalItems;
    private int totalQuantity;
    private double totalWeight;
    private double totalVolume;
    
    // Shipping information
    @Indexed
    private String carrier;
    private String trackingNumber;
    private String shippingAddress;
    
    // Packaging details
    private String packageType; // BOX, ENVELOPE, TUBE, etc.
    private String packageSize; // SMALL, MEDIUM, LARGE, EXTRA_LARGE
    
    @Version
    private Long version;
    
    // Audit fields
    @CreatedDate
    private Date createdAt;
    
    @LastModifiedDate
    private Date updatedAt;
    
    private String createdBy;
    private String updatedBy;
    
    // Operational timestamps
    private Date packedAt;
    private Date shippedAt;
    private Date deliveredAt;

    public PackageDocument() {}

    public PackageDocument(Package pkg) {
        this.id = pkg.getPackageId().toString();
        // Note: We need to add getOrderId() method to Package domain class
        // For now, we'll leave it null and handle this in the adapter
        this.orderId = null; // TODO: Add orderId to Package domain
        this.status = pkg.getStatus();
        this.packedItems = pkg.getPackedItems().stream()
                .map(PackedItemDocument::new)
                .collect(Collectors.toList());
        
        // Calculate summary fields
        this.updateSummaryFields();
        
        // Audit fields will be set by Spring Data MongoDB
    }

    // Helper method to update summary fields
    public void updateSummaryFields() {
        if (packedItems != null && !packedItems.isEmpty()) {
            this.totalItems = packedItems.size();
            this.totalQuantity = packedItems.stream()
                    .mapToInt(PackedItemDocument::getQuantity)
                    .sum();
            this.totalWeight = packedItems.stream()
                    .mapToDouble(item -> item.getWeight() != null ? item.getWeight() : 0.0)
                    .sum();
            this.totalVolume = packedItems.stream()
                    .mapToDouble(item -> item.getVolume() != null ? item.getVolume() : 0.0)
                    .sum();
        } else {
            this.totalItems = 0;
            this.totalQuantity = 0;
            this.totalWeight = 0.0;
            this.totalVolume = 0.0;
        }
    }

    public Package toDomain() {
        List<PackedItem> domainPackedItems = packedItems.stream()
                .map(PackedItemDocument::toDomain)
                .collect(Collectors.toList());

        Package pkg = new Package(UUID.fromString(id), domainPackedItems);
        pkg.setStatus(status);
        return pkg;
    }

    // Inner class for PackedItem
    public static class PackedItemDocument {
        private String skuCode;
        private int quantity;
        
        // Additional fields for better warehouse operations
        private Double weight;
        private Double volume;
        private String productName;
        private String productCategory;
        private Double unitPrice;
        private String binLocation; // Where it was picked from

        public PackedItemDocument() {}

        public PackedItemDocument(PackedItem item) {
            this.skuCode = item.getSkuCode().getValue();
            this.quantity = item.getQuantity();
        }

        public PackedItem toDomain() {
            return new PackedItem(new SkuCode(skuCode), quantity);
        }

        // Getters and setters
        public String getSkuCode() {
            return skuCode;
        }

        public void setSkuCode(String skuCode) {
            this.skuCode = skuCode;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public Double getVolume() {
            return volume;
        }

        public void setVolume(Double volume) {
            this.volume = volume;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductCategory() {
            return productCategory;
        }

        public void setProductCategory(String productCategory) {
            this.productCategory = productCategory;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getBinLocation() {
            return binLocation;
        }

        public void setBinLocation(String binLocation) {
            this.binLocation = binLocation;
        }
    }

    // Getters and setters for main class
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public void setStatus(PackageStatus status) {
        this.status = status;
    }

    public List<PackedItemDocument> getPackedItems() {
        return packedItems;
    }

    public void setPackedItems(List<PackedItemDocument> packedItems) {
        this.packedItems = packedItems;
        // Update summary fields when items change
        this.updateSummaryFields();
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(String packageSize) {
        this.packageSize = packageSize;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    public Date getPackedAt() {
        return packedAt;
    }

    public void setPackedAt(Date packedAt) {
        this.packedAt = packedAt;
    }

    public Date getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(Date shippedAt) {
        this.shippedAt = shippedAt;
    }

    public Date getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Date deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageDocument that = (PackageDocument) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}