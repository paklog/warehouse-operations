package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;

import java.util.Comparator;
import java.util.List;

public class NearestEmptyLocationSelector implements LocationSelector {
    
    @Override
    public BinLocation selectOptimalLocation(LocationQuery query, LocationDirective directive) {
        BinLocation referenceLocation = query.getReferenceLocation();
        List<BinLocation> candidates = query.getCandidateLocations();
        
        if (referenceLocation == null) {
            // Use default starting location
            referenceLocation = new BinLocation("A", "01", "1");
        }
        
        if (candidates == null || candidates.isEmpty()) {
            // Generate candidate locations in nearby area
            candidates = generateNearbyCandidates(referenceLocation);
        }
        
        // Filter candidates that satisfy constraints and find nearest empty
        BinLocation finalRef = referenceLocation;
        return candidates.stream()
            .filter(candidate -> satisfiesDirectiveConstraints(candidate, query, directive))
            .filter(candidate -> isLocationEmpty(candidate, query))
            .min(Comparator.comparingDouble(candidate -> calculateDistance(finalRef, candidate)))
            .orElse(null);
    }
    
    private List<BinLocation> generateNearbyCandidates(BinLocation reference) {
        // Generate locations within a 3x3x3 grid around reference
        String refAisle = reference.getAisle();
        int refRack = Integer.parseInt(reference.getRack());
        int refLevel = Integer.parseInt(reference.getLevel());
        
        java.util.List<BinLocation> candidates = new java.util.ArrayList<>();
        
        for (int aisleOffset = -1; aisleOffset <= 1; aisleOffset++) {
            for (int rackOffset = -2; rackOffset <= 2; rackOffset++) {
                for (int levelOffset = -1; levelOffset <= 1; levelOffset++) {
                    try {
                        String aisle = refAisle; // Simplified - same aisle
                        int rack = Math.max(1, refRack + rackOffset);
                        int level = Math.max(1, refLevel + levelOffset);
                        
                        candidates.add(new BinLocation(aisle, String.format("%02d", rack), String.valueOf(level)));
                    } catch (Exception e) {
                        // Skip invalid locations
                    }
                }
            }
        }
        
        return candidates;
    }
    
    private boolean satisfiesDirectiveConstraints(BinLocation candidate, LocationQuery query, LocationDirective directive) {
        LocationContext context = query.createContextForLocation(candidate);
        return directive.satisfiesConstraints(context);
    }
    
    private boolean isLocationEmpty(BinLocation location, LocationQuery query) {
        // In real implementation, this would check inventory system
        // For now, assume locations ending in odd levels are empty
        int level = Integer.parseInt(location.getLevel());
        return level % 2 == 1;
    }
    
    private double calculateDistance(BinLocation from, BinLocation to) {
        // Simple Manhattan distance
        int aisleDistance = calculateAisleDistance(from.getAisle(), to.getAisle());
        int rackDistance = Math.abs(Integer.parseInt(from.getRack()) - Integer.parseInt(to.getRack()));
        int levelDistance = Math.abs(Integer.parseInt(from.getLevel()) - Integer.parseInt(to.getLevel()));
        
        return aisleDistance * 10 + rackDistance + levelDistance * 2;
    }
    
    private int calculateAisleDistance(String fromAisle, String toAisle) {
        // Handle aisles of different lengths safely
        if (fromAisle.equals(toAisle)) {
            return 0;
        }
        
        // For single character aisles like "A", "B", etc.
        if (fromAisle.length() == 1 && toAisle.length() == 1) {
            return Math.abs(fromAisle.charAt(0) - toAisle.charAt(0));
        }
        
        // For multi-character aisles, use the last character
        char fromChar = fromAisle.charAt(fromAisle.length() - 1);
        char toChar = toAisle.charAt(toAisle.length() - 1);
        return Math.abs(fromChar - toChar);
    }
    
    @Override
    public boolean supportsStrategy(LocationStrategy strategy) {
        return strategy == LocationStrategy.NEAREST_EMPTY;
    }
    
    @Override
    public String getStrategyDescription() {
        return "Selects the nearest available empty location";
    }
}