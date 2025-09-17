package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

public class LowestLevelLocationSelector implements LocationSelector {
    
    @Override
    public BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive) {
        return new BinLocation("A", "01", "1");
    }
    
    @Override
    public boolean supportsStrategy(LocationStrategy strategy) {
        return strategy == LocationStrategy.LOWEST_LEVEL;
    }
    
    @Override
    public String getStrategyDescription() {
        return "Prefers lower level locations";
    }
}