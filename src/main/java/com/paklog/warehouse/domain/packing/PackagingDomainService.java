package com.paklog.warehouse.domain.packing;

import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.OrderItem;
import com.paklog.warehouse.domain.shared.PickList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PackagingDomainService {
    private static final Logger logger = LoggerFactory.getLogger(PackagingDomainService.class);
    
    private static final int MAX_PACKAGE_WEIGHT_GRAMS = 22000; // 22kg limit
    private static final int MAX_ITEMS_PER_PACKAGE = 50;

    /**
     * Validates if an order can be packaged
     */
    public boolean canPackageOrder(FulfillmentOrder order, PickList pickList) {
        logger.debug("Validating packaging for order: {}", order.getOrderId());
        
        if (order == null) {
            logger.warn("Cannot package null order");
            return false;
        }
        
        if (pickList == null) {
            logger.warn("Cannot package order {} - no associated pick list", order.getOrderId());
            return false;
        }
        
        if (!pickList.isComplete()) {
            logger.warn("Cannot package order {} - pick list is not complete", order.getOrderId());
            return false;
        }
        
        return true;
    }

    /**
     * Validates package contents against business rules
     */
    public PackageValidationResult validatePackageContents(Package pkg) {
        logger.debug("Validating package contents: {}", pkg.getPackageId());
        
        List<String> violations = pkg.getPackedItems().stream()
                .filter(item -> item.getQuantity() <= 0)
                .map(item -> "Invalid quantity for SKU: " + item.getSkuCode())
                .collect(Collectors.toList());
        
        // Check item count limit
        if (pkg.getPackedItems().size() > MAX_ITEMS_PER_PACKAGE) {
            violations.add("Package exceeds maximum items limit: " + MAX_ITEMS_PER_PACKAGE);
        }
        
        // Check if package has any items
        if (pkg.getPackedItems().isEmpty()) {
            violations.add("Package cannot be empty");
        }
        
        // Check for duplicate SKUs
        long uniqueSkuCount = pkg.getPackedItems().stream()
                .map(PackedItem::getSkuCode)
                .distinct()
                .count();
        
        if (uniqueSkuCount != pkg.getPackedItems().size()) {
            violations.add("Package contains duplicate SKU codes");
        }
        
        boolean isValid = violations.isEmpty();
        
        if (!isValid) {
            logger.warn("Package validation failed for package: {} - violations: {}", 
                       pkg.getPackageId(), violations);
        } else {
            logger.debug("Package validation passed for package: {}", pkg.getPackageId());
        }
        
        return new PackageValidationResult(isValid, violations);
    }

    /**
     * Determines if a package is ready for shipment
     */
    public boolean isReadyForShipment(Package pkg) {
        logger.debug("Checking shipment readiness for package: {}", pkg.getPackageId());
        
        if (pkg.getStatus() != PackageStatus.CONFIRMED) {
            logger.debug("Package {} not ready - status: {}", pkg.getPackageId(), pkg.getStatus());
            return false;
        }
        
        PackageValidationResult validation = validatePackageContents(pkg);
        if (!validation.isValid()) {
            logger.warn("Package {} not ready - validation failed: {}", 
                       pkg.getPackageId(), validation.getViolations());
            return false;
        }
        
        logger.debug("Package {} is ready for shipment", pkg.getPackageId());
        return true;
    }

    /**
     * Creates packed items from fulfilled order items
     */
    public List<PackedItem> createPackedItemsFromOrder(List<OrderItem> orderItems) {
        logger.debug("Creating packed items from {} order items", orderItems.size());
        
        return orderItems.stream()
                .map(orderItem -> new PackedItem(
                    orderItem.getSkuCode(),
                    orderItem.getQuantity().getValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Calculates packaging efficiency metrics
     */
    public PackagingMetrics calculatePackagingMetrics(Package pkg) {
        int totalItems = pkg.getTotalQuantity();
        int uniqueSkus = pkg.getPackedItems().size();
        double packingDensity = (double) totalItems / Math.max(1, uniqueSkus);
        
        return new PackagingMetrics(totalItems, uniqueSkus, packingDensity);
    }

    public static class PackageValidationResult {
        private final boolean valid;
        private final List<String> violations;

        public PackageValidationResult(boolean valid, List<String> violations) {
            this.valid = valid;
            this.violations = violations;
        }

        public boolean isValid() { return valid; }
        public List<String> getViolations() { return violations; }
    }

    public static class PackagingMetrics {
        private final int totalItems;
        private final int uniqueSkus;
        private final double packingDensity;

        public PackagingMetrics(int totalItems, int uniqueSkus, double packingDensity) {
            this.totalItems = totalItems;
            this.uniqueSkus = uniqueSkus;
            this.packingDensity = packingDensity;
        }

        public int getTotalItems() { return totalItems; }
        public int getUniqueSkus() { return uniqueSkus; }
        public double getPackingDensity() { return packingDensity; }
    }
}