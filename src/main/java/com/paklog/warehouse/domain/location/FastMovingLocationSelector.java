package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

public class FastMovingLocationSelector implements LocationSelector {
    
    @Override
    public BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive) {
        return new BinLocation("F", "01", "1");
    }
    
    @Override
    public boolean supportsStrategy(LocationStrategy strategy) {
        return strategy == LocationStrategy.FAST_MOVING;
    }
    
    @Override
    public String getStrategyDescription() {
        return "Selects fast-moving pick locations";
    }
}