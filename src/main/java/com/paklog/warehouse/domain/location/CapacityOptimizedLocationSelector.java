package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

public class CapacityOptimizedLocationSelector implements LocationSelector {
    
    @Override
    public BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive) {
        return new BinLocation("C", "01", "1");
    }
    
    @Override
    public boolean supportsStrategy(LocationStrategy strategy) {
        return strategy == LocationStrategy.CAPACITY_OPTIMIZED;
    }
    
    @Override
    public String getStrategyDescription() {
        return "Selects locations based on available capacity";
    }
}