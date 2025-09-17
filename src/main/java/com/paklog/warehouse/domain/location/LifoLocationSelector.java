package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

public class LifoLocationSelector implements LocationSelector {
    
    @Override
    public BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive) {
        return new BinLocation("L", "01", "1");
    }
    
    @Override
    public boolean supportsStrategy(LocationStrategy strategy) {
        return strategy == LocationStrategy.LIFO;
    }
    
    @Override
    public String getStrategyDescription() {
        return "Last In, First Out location selection";
    }
}