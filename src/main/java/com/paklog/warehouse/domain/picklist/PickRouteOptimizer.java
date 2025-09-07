package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.PickList;
import com.paklog.warehouse.domain.shared.PickInstruction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PickRouteOptimizer {
    public List<PickInstruction> optimizePickRoute(PickList pickList) {
        // Start from a default location
        BinLocation currentLocation = new BinLocation("A", "0", "0");
        
        // Get remaining instructions that are not completed
        List<PickInstruction> remainingInstructions = pickList.getInstructions().stream()
            .filter(instruction -> !instruction.isCompleted())
            .collect(Collectors.toList());

        // Sort instructions based on proximity to current location
        return remainingInstructions.stream()
            .sorted(Comparator.comparing(instruction -> 
                calculateDistance(currentLocation, instruction.getBinLocation())
            ))
            .collect(Collectors.toList());
    }

    private double calculateDistance(BinLocation from, BinLocation to) {
        // Simple Manhattan distance calculation
        return Math.abs(Integer.parseInt(from.getAisle()) - Integer.parseInt(to.getAisle())) +
               Math.abs(Integer.parseInt(from.getRack()) - Integer.parseInt(to.getRack())) +
               Math.abs(Integer.parseInt(from.getLevel()) - Integer.parseInt(to.getLevel()));
    }
}