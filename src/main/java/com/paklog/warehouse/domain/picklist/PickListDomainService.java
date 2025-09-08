package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.picklist.PickInstruction;
import com.paklog.warehouse.domain.picklist.PickList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PickListDomainService {
    private static final Logger logger = LoggerFactory.getLogger(PickListDomainService.class);
    
    private final PickRouteOptimizer routeOptimizer;

    public PickListDomainService(PickRouteOptimizer routeOptimizer) {
        this.routeOptimizer = routeOptimizer;
    }

    /**
     * Validates that a pick list can be assigned to a picker
     */
    public boolean canAssignToPicker(PickList pickList, String pickerId) {
        logger.debug("Validating pick list assignment: {} to picker: {}", pickList.getId(), pickerId);
        
        if (pickList == null) {
            logger.warn("Cannot assign null pick list");
            return false;
        }
        
        if (pickerId == null || pickerId.trim().isEmpty()) {
            logger.warn("Cannot assign pick list to empty picker ID");
            return false;
        }
        
        if (pickList.getStatus() != com.paklog.warehouse.domain.picklist.PickListStatus.PENDING) {
            logger.warn("Cannot assign pick list {} - status is {}", pickList.getId(), pickList.getStatus());
            return false;
        }
        
        return true;
    }

    /**
     * Optimizes the pick route for a given pick list
     */
    public PickList optimizePickRoute(PickList pickList) {
        logger.info("Optimizing pick route for pick list: {}", pickList.getId());
        
        if (pickList.getInstructions().isEmpty()) {
            logger.warn("No instructions to optimize for pick list: {}", pickList.getId());
            return pickList;
        }
        
        try {
            List<PickInstruction> optimizedInstructions = routeOptimizer.optimizePickRoute(pickList);
            
            // Create a new pick list with optimized instructions
            PickList optimizedPickList = new PickList(pickList.getOrderId());
            optimizedInstructions.forEach(optimizedPickList::addInstruction);
            
            logger.info("Pick route optimized for pick list: {} - {} instructions reordered", 
                       pickList.getId(), optimizedInstructions.size());
            
            return optimizedPickList;
        } catch (Exception e) {
            logger.error("Failed to optimize pick route for pick list: {}", pickList.getId(), e);
            return pickList; // Return original if optimization fails
        }
    }

    /**
     * Validates pick completion business rules
     */
    public boolean isPickValid(PickList pickList, String skuCode, int quantity, BinLocation binLocation) {
        logger.debug("Validating pick for pick list: {}, SKU: {}, quantity: {}, location: {}", 
                    pickList.getId(), skuCode, quantity, binLocation);
        
        // Business rule: Picker must be assigned
        if (pickList.getPickerId() == null) {
            logger.warn("Cannot confirm pick - no picker assigned to pick list: {}", pickList.getId());
            return false;
        }
        
        // Business rule: Pick list must be assigned status
        if (pickList.getStatus() != com.paklog.warehouse.domain.picklist.PickListStatus.ASSIGNED) {
            logger.warn("Cannot confirm pick - pick list {} status is {}", pickList.getId(), pickList.getStatus());
            return false;
        }
        
        // Business rule: Quantity must be positive
        if (quantity <= 0) {
            logger.warn("Cannot confirm pick - invalid quantity: {}", quantity);
            return false;
        }
        
        // Business rule: Bin location must be valid
        if (binLocation == null || binLocation.getLocation() == null || binLocation.getLocation().trim().isEmpty()) {
            logger.warn("Cannot confirm pick - invalid bin location");
            return false;
        }
        
        return true;
    }

    /**
     * Calculates pick list progress
     */
    public PickListProgress calculateProgress(PickList pickList) {
        List<PickInstruction> instructions = pickList.getInstructions();
        
        if (instructions.isEmpty()) {
            return new PickListProgress(0, 0, 100.0);
        }
        
        int totalInstructions = instructions.size();
        int completedInstructions = (int) instructions.stream()
                .filter(PickInstruction::isCompleted)
                .count();
        
        double progressPercentage = (double) completedInstructions / totalInstructions * 100.0;
        
        return new PickListProgress(completedInstructions, totalInstructions, progressPercentage);
    }

    public static class PickListProgress {
        private final int completed;
        private final int total;
        private final double percentage;

        public PickListProgress(int completed, int total, double percentage) {
            this.completed = completed;
            this.total = total;
            this.percentage = percentage;
        }

        public int getCompleted() { return completed; }
        public int getTotal() { return total; }
        public double getPercentage() { return percentage; }
        
        public boolean isComplete() { return completed == total && total > 0; }
    }
}