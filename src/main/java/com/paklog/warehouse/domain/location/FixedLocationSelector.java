package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

public class FixedLocationSelector implements LocationSelector {
    
    @Override
    public BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive) {
        // Fixed location selection based on item mapping
        // In real implementation, this would look up predefined item-location mappings
        
        String itemCode = query.getItem().getValue();
        String fixedLocation = query.getParameterAsString("fixed_location");
        
        if (fixedLocation != null) {
            try {
                return BinLocation.of(fixedLocation);
            } catch (IllegalArgumentException e) {
                return null; // Invalid format
            }
        }
        
        // Fallback: Generate fixed location based on item code hash
        return generateFixedLocationForItem(itemCode);
    }
    
    private BinLocation generateFixedLocationForItem(String itemCode) {
        // Simple hash-based location assignment
        int hash = Math.abs(itemCode.hashCode());
        
        String aisle = "A" + (hash % 10 + 1); // A1-A10
        String rack = String.format("%02d", hash % 20 + 1); // 01-20
        String level = String.valueOf(hash % 5 + 1); // 1-5
        
        return new BinLocation(aisle, rack, level);
    }
    
    @Override
    public boolean supportsStrategy(LocationStrategy strategy) {
        return strategy == LocationStrategy.FIXED;
    }
    
    @Override
    public String getStrategyDescription() {
        return "Selects predefined fixed locations for items";
    }
}