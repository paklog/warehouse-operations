package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

public interface LocationSelector {
    BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive);
    
    boolean supportsStrategy(LocationStrategy strategy);
    
    String getStrategyDescription();
}