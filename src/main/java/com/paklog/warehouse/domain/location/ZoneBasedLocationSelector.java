package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

public class ZoneBasedLocationSelector implements LocationSelector {
    
    @Override
    public BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive) {
        return new BinLocation("Z", "01", "1");
    }
    
    @Override
    public boolean supportsStrategy(LocationStrategy strategy) {
        return strategy == LocationStrategy.ZONE_BASED;
    }
    
    @Override
    public String getStrategyDescription() {
        return "Selects locations based on product zone classification";
    }
}