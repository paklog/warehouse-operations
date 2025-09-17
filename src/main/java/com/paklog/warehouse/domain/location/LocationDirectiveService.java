package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.work.WorkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LocationDirectiveService {
    private static final Logger logger = LoggerFactory.getLogger(LocationDirectiveService.class);
    
    private final LocationDirectiveRepository locationDirectiveRepository;

    public LocationDirectiveService(LocationDirectiveRepository locationDirectiveRepository) {
        this.locationDirectiveRepository = Objects.requireNonNull(locationDirectiveRepository, 
            "LocationDirectiveRepository cannot be null");
    }

    public BinLocation selectOptimalLocation(LocationQuery query) {
        logger.info("Selecting optimal location for query: {}", query);

        List<LocationDirective> applicableDirectives = getApplicableDirectives(query.getWorkType());
        
        if (applicableDirectives.isEmpty()) {
            logger.warn("No applicable location directives found for work type: {}", query.getWorkType());
            return null;
        }

        for (LocationDirective directive : applicableDirectives) {
            try {
                BinLocation location = directive.selectLocation(query);
                if (location != null) {
                    logger.info("Selected location {} using directive: {}", location, directive.getName());
                    return location;
                }
            } catch (Exception e) {
                logger.error("Error selecting location with directive {}: {}", directive.getName(), e.getMessage());
            }
        }

        logger.warn("No suitable location found for query: {}", query);
        return null;
    }

    public LocationEvaluationResult evaluateLocation(LocationQuery query, BinLocation location) {
        logger.debug("Evaluating location {} for query: {}", location, query);

        List<LocationDirective> applicableDirectives = getApplicableDirectives(query.getWorkType());
        
        if (applicableDirectives.isEmpty()) {
            return LocationEvaluationResult.noDirectivesAvailable();
        }

        double totalScore = 0.0;
        int applicableDirectiveCount = 0;
        List<String> violations = new java.util.ArrayList<>();

        for (LocationDirective directive : applicableDirectives) {
            LocationDirectiveResult result = directive.evaluateForLocation(query, location);
            
            if (result.isSuitable()) {
                totalScore += result.getScore();
                applicableDirectiveCount++;
            } else if (result.hasConstraintViolations()) {
                violations.addAll(result.getViolations());
            }
        }

        if (applicableDirectiveCount == 0) {
            return LocationEvaluationResult.unsuitable(violations);
        }

        double averageScore = totalScore / applicableDirectiveCount;
        return LocationEvaluationResult.suitable(averageScore, applicableDirectiveCount);
    }

    public List<BinLocation> findBestLocations(LocationQuery query, int maxResults) {
        logger.info("Finding best {} locations for query: {}", maxResults, query);

        List<BinLocation> candidateLocations = query.getCandidateLocations();
        
        if (candidateLocations == null || candidateLocations.isEmpty()) {
            // Generate candidate locations if none provided
            candidateLocations = generateCandidateLocations(query);
        }

        return candidateLocations.stream()
            .map(location -> new LocationScore(location, evaluateLocation(query, location)))
            .filter(score -> score.evaluation.isSuitable())
            .sorted(Comparator.comparingDouble(score -> -score.evaluation.getScore())) // Descending order
            .limit(maxResults)
            .map(score -> score.location)
            .toList();
    }

    public boolean canSatisfyQuery(LocationQuery query) {
        List<LocationDirective> applicableDirectives = getApplicableDirectives(query.getWorkType());
        return !applicableDirectives.isEmpty() && 
               applicableDirectives.stream().anyMatch(LocationDirective::isActive);
    }

    public List<LocationDirective> getApplicableDirectives(WorkType workType) {
        return locationDirectiveRepository.findByWorkTypeAndActive(workType, true)
                .stream()
                .sorted(Comparator.comparingInt(LocationDirective::getPriority))
                .toList();
    }

    public LocationDirectiveValidationResult validateDirective(LocationDirective directive) {
        logger.debug("Validating location directive: {}", directive.getName());

        List<String> issues = new java.util.ArrayList<>();

        // Check basic configuration
        if (!directive.isActive()) {
            issues.add("Directive is inactive");
        }

        if (directive.getConstraints().isEmpty() && 
            !directive.getStrategy().requiresFixedMapping()) {
            issues.add("No constraints defined for non-fixed strategy");
        }

        // Check strategy compatibility with constraints
        LocationStrategy strategy = directive.getStrategy();
        if (strategy.requiresInventoryData()) {
            boolean hasInventoryConstraint = directive.getConstraints().stream()
                .anyMatch(c -> c.getType() == LocationConstraintType.INVENTORY_AVAILABLE);
            if (!hasInventoryConstraint) {
                issues.add("Strategy requires inventory data but no inventory constraints defined");
            }
        }

        if (strategy.requiresZoneConfiguration()) {
            boolean hasZoneConstraint = directive.getConstraints().stream()
                .anyMatch(c -> c.getType() == LocationConstraintType.ZONE_RESTRICTION);
            if (!hasZoneConstraint) {
                issues.add("Strategy requires zone configuration but no zone constraints defined");
            }
        }

        boolean isValid = issues.isEmpty();
        return new LocationDirectiveValidationResult(isValid, issues);
    }

    public LocationDirective createDefaultDirective(WorkType workType, LocationStrategy strategy) {
        String name = String.format("Default %s %s", workType, strategy);
        String description = String.format("Default directive for %s operations using %s strategy", 
                                         workType, strategy);
        
        LocationDirective directive = new LocationDirective(name, description, workType, strategy, 100);
        
        // Add default constraints based on work type
        addDefaultConstraints(directive, workType);
        
        return directive;
    }

    private void addDefaultConstraints(LocationDirective directive, WorkType workType) {
        switch (workType) {
            case PICK:
                directive.addConstraint(new LocationConstraint(
                    LocationConstraintType.ACCESSIBILITY, "equals", "STANDARD"));
                directive.addConstraint(new LocationConstraint(
                    LocationConstraintType.INVENTORY_AVAILABLE, "gt", 0));
                break;
                
            case PUT:
                directive.addConstraint(new LocationConstraint(
                    LocationConstraintType.CAPACITY_REQUIREMENT, "gt", 0.0));
                directive.addConstraint(new LocationConstraint(
                    LocationConstraintType.ACCESSIBILITY, "equals", "STANDARD"));
                break;
                
            case COUNT:
                directive.addConstraint(new LocationConstraint(
                    LocationConstraintType.ACCESSIBILITY, "equals", "STANDARD"));
                break;
                
            default:
                // No default constraints for other work types
                break;
        }
    }

    private List<BinLocation> generateCandidateLocations(LocationQuery query) {
        // Generate a reasonable set of candidate locations
        // In real implementation, this would query the warehouse layout system
        
        List<BinLocation> candidates = new java.util.ArrayList<>();
        
        for (int aisle = 1; aisle <= 5; aisle++) {
            for (int rack = 1; rack <= 10; rack++) {
                for (int level = 1; level <= 3; level++) {
                    String aisleStr = "A" + aisle;
                    String rackStr = String.format("%02d", rack);
                    String levelStr = String.valueOf(level);
                    
                    candidates.add(new BinLocation(aisleStr, rackStr, levelStr));
                }
            }
        }
        
        return candidates.stream().limit(50).toList(); // Limit to reasonable number
    }

    private static class LocationScore {
        final BinLocation location;
        final LocationEvaluationResult evaluation;

        LocationScore(BinLocation location, LocationEvaluationResult evaluation) {
            this.location = location;
            this.evaluation = evaluation;
        }
    }

    public static class LocationEvaluationResult {
        private final boolean suitable;
        private final double score;
        private final int applicableDirectives;
        private final List<String> violations;

        private LocationEvaluationResult(boolean suitable, double score, int applicableDirectives, 
                                       List<String> violations) {
            this.suitable = suitable;
            this.score = score;
            this.applicableDirectives = applicableDirectives;
            this.violations = violations != null ? List.copyOf(violations) : List.of();
        }

        public static LocationEvaluationResult suitable(double score, int applicableDirectives) {
            return new LocationEvaluationResult(true, score, applicableDirectives, null);
        }

        public static LocationEvaluationResult unsuitable(List<String> violations) {
            return new LocationEvaluationResult(false, 0.0, 0, violations);
        }

        public static LocationEvaluationResult noDirectivesAvailable() {
            return new LocationEvaluationResult(false, 0.0, 0, 
                List.of("No applicable directives available"));
        }

        public boolean isSuitable() {
            return suitable;
        }

        public double getScore() {
            return score;
        }

        public int getApplicableDirectives() {
            return applicableDirectives;
        }

        public List<String> getViolations() {
            return violations;
        }
    }

    public static class LocationDirectiveValidationResult {
        private final boolean valid;
        private final List<String> issues;

        public LocationDirectiveValidationResult(boolean valid, List<String> issues) {
            this.valid = valid;
            this.issues = List.copyOf(issues);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getIssues() {
            return issues;
        }
    }
}