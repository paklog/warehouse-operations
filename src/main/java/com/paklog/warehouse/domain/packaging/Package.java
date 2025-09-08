package com.paklog.warehouse.domain.packaging;

import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Package {
    private final UUID packageId;
    private PackageStatus status;
    private final List<PackedItem> packedItems;

    public Package(UUID packageId, List<PackedItem> packedItems) {
        if (packageId == null) {
            throw new IllegalArgumentException("Package ID cannot be null");
        }
        if (packedItems == null || packedItems.isEmpty()) {
            throw new IllegalArgumentException("Package must contain at least one packed item");
        }
        this.packageId = packageId;
        this.packedItems = new ArrayList<>(packedItems);
        this.status = PackageStatus.PENDING;
    }

    public Package(FulfillmentOrder order, PickList pickList) {
        if (order == null) {
            throw new IllegalArgumentException("Fulfillment order cannot be null");
        }
        if (pickList == null) {
            throw new IllegalArgumentException("PickList cannot be null");
        }
        this.packageId = UUID.randomUUID();
        this.packedItems = new ArrayList<>();
        this.status = PackageStatus.PENDING;
        // Convert PickList instructions to PackedItems would be done here
        // For now, leaving it as empty list - this would be implemented based on business logic
    }

    public UUID getPackageId() {
        return packageId;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public List<PackedItem> getPackedItems() {
        return new ArrayList<>(packedItems);
    }

    public void addPackedItem(PackedItem newItem) {
        // Check if an item with the same SKU already exists
        boolean skuExists = packedItems.stream()
            .anyMatch(item -> item.getSkuCode().equals(newItem.getSkuCode()));

        if (skuExists) {
            throw new IllegalArgumentException("Packed item with this SKU already exists in the package");
        }

        packedItems.add(newItem);
    }

    public int getTotalQuantity() {
        return packedItems.stream()
            .mapToInt(PackedItem::getQuantity)
            .sum();
    }

    public void confirmPacking() {
        if (this.status == PackageStatus.CONFIRMED) {
            throw new IllegalStateException("Package is already confirmed");
        }
        this.status = PackageStatus.CONFIRMED;
    }

    // Setter for repository adapter
    public void setStatus(PackageStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Package aPackage = (Package) o;
        return Objects.equals(packageId, aPackage.packageId) &&
               status == aPackage.status &&
               Objects.equals(packedItems, aPackage.packedItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageId, status, packedItems);
    }

    @Override
    public String toString() {
        return "Package{" +
               "packageId=" + packageId +
               ", status=" + status +
               ", packedItems=" + packedItems +
               '}';
    }
}