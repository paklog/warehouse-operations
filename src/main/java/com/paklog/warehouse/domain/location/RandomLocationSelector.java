package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

import java.util.List;
import java.util.Random;

public class RandomLocationSelector implements LocationSelector {
    private final Random random = new Random();
    
    @Override
    public BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive) {
        List<BinLocation> candidates = query.getCandidateLocations();
        
        if (candidates != null && !candidates.isEmpty()) {
            return candidates.get(random.nextInt(candidates.size()));
        }
        
        // Generate random location
        String aisle = "A" + (random.nextInt(10) + 1);
        String rack = String.format("%02d", random.nextInt(20) + 1);
        String level = String.valueOf(random.nextInt(5) + 1);
        
        return new BinLocation(aisle, rack, level);
    }
    
    @Override
    public boolean supportsStrategy(LocationStrategy strategy) {
        return strategy == LocationStrategy.RANDOM;
    }
    
    @Override
    public String getStrategyDescription() {
        return "Random available location selection";
    }
}