package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

public class BulkLocationSelector implements LocationSelector {
    
    @Override
    public BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive) {
        // TODO: Implement bulk location selection logic
        return new BinLocation("B", "01", "1"); // Placeholder
    }
    
    @Override
    public boolean supportsStrategy(LocationStrategy strategy) {
        return strategy == LocationStrategy.BULK_LOCATION;
    }
    
    @Override
    public String getStrategyDescription() {
        return "Selects bulk storage locations for high-volume items";
    }
}