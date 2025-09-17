package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.location.LocationDirectiveService;
import com.paklog.warehouse.domain.location.LocationQuery;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LocationAwareWorkCreationService {
    private static final Logger logger = LoggerFactory.getLogger(LocationAwareWorkCreationService.class);
    
    private final WorkCreationService workCreationService;
    private final LocationDirectiveService locationDirectiveService;

    public LocationAwareWorkCreationService(WorkCreationService workCreationService,
                                          LocationDirectiveService locationDirectiveService) {
        this.workCreationService = Objects.requireNonNull(workCreationService, 
            "WorkCreationService cannot be null");
        this.locationDirectiveService = Objects.requireNonNull(locationDirectiveService, 
            "LocationDirectiveService cannot be null");
    }

    public Work createWorkWithOptimalLocation(WorkType workType, SkuCode item, 
                                            Quantity quantity, String assignedTo) {
        logger.info("Creating work with optimal location for type: {}, item: {}", workType, item);

        // Create location query
        LocationQuery query = new LocationQuery(workType, item, quantity);
        
        // Select optimal location using location directives
        BinLocation optimalLocation = locationDirectiveService.selectOptimalLocation(query);
        
        if (optimalLocation == null) {
            throw new IllegalStateException("No suitable location found for work type: " + workType + 
                                          ", item: " + item);
        }

        logger.info("Selected optimal location: {} for work", optimalLocation);
        
        // Create work using the selected location
        return workCreationService.createWork(workType, optimalLocation, item, quantity, assignedTo);
    }

    public Work createWorkWithLocationPreferences(WorkType workType, SkuCode item, 
                                                 Quantity quantity, String assignedTo,
                                                 Map<String, Object> locationPreferences) {
        logger.info("Creating work with location preferences for type: {}, item: {}", workType, item);

        // Create location query with preferences
        LocationQuery query = new LocationQuery(workType, item, quantity, null, 
                                               locationPreferences, null);
        
        // Select optimal location using location directives and preferences
        BinLocation optimalLocation = locationDirectiveService.selectOptimalLocation(query);
        
        if (optimalLocation == null) {
            throw new IllegalStateException("No suitable location found for work type: " + workType + 
                                          ", item: " + item + " with preferences: " + locationPreferences);
        }

        logger.info("Selected optimal location: {} for work with preferences", optimalLocation);
        
        return workCreationService.createWork(workType, optimalLocation, item, quantity, assignedTo);
    }

    public Work createWorkWithCandidateLocations(WorkType workType, SkuCode item, 
                                               Quantity quantity, String assignedTo,
                                               List<BinLocation> candidateLocations) {
        logger.info("Creating work with {} candidate locations for type: {}, item: {}", 
                   candidateLocations.size(), workType, item);

        // Create location query with candidate locations
        LocationQuery query = new LocationQuery(workType, item, quantity, null, 
                                               new HashMap<>(), candidateLocations);
        
        // Select best location from candidates
        BinLocation bestLocation = locationDirectiveService.selectOptimalLocation(query);
        
        if (bestLocation == null) {
            logger.warn("No suitable location found from candidates, using first candidate");
            bestLocation = candidateLocations.get(0);
        }

        logger.info("Selected location: {} from candidates for work", bestLocation);
        
        return workCreationService.createWork(workType, bestLocation, item, quantity, assignedTo);
    }

    public List<Work> createBatchWorkWithOptimalLocations(WorkType workType, 
                                                        List<WorkItemRequest> itemRequests) {
        logger.info("Creating batch work with optimal locations for {} items of type: {}", 
                   itemRequests.size(), workType);

        return itemRequests.stream()
            .map(request -> createWorkWithOptimalLocation(workType, request.item, 
                                                        request.quantity, request.assignedTo))
            .toList();
    }

    public LocationValidationResult validateLocationForWork(WorkType workType, BinLocation location, 
                                                          SkuCode item, Quantity quantity) {
        logger.debug("Validating location {} for work type: {}, item: {}", location, workType, item);

        LocationQuery query = new LocationQuery(workType, item, quantity);
        
        LocationDirectiveService.LocationEvaluationResult evaluation = 
            locationDirectiveService.evaluateLocation(query, location);

        if (evaluation.isSuitable()) {
            return LocationValidationResult.suitable(evaluation.getScore(), 
                                                   evaluation.getApplicableDirectives());
        } else {
            return LocationValidationResult.unsuitable(evaluation.getViolations());
        }
    }

    public List<BinLocation> findBestLocationsForWork(WorkType workType, SkuCode item, 
                                                    Quantity quantity, int maxResults) {
        logger.info("Finding best {} locations for work type: {}, item: {}", maxResults, workType, item);

        LocationQuery query = new LocationQuery(workType, item, quantity);
        return locationDirectiveService.findBestLocations(query, maxResults);
    }

    public boolean canCreateWorkAtLocation(WorkType workType, BinLocation location, 
                                         SkuCode item, Quantity quantity) {
        LocationValidationResult validation = validateLocationForWork(workType, location, item, quantity);
        return validation.isSuitable();
    }

    public EnhancedWorkCreationResult createWorkWithLocationValidation(WorkType workType, BinLocation location,
                                                             SkuCode item, Quantity quantity, 
                                                             String assignedTo) {
        logger.info("Creating work with location validation for type: {}, location: {}, item: {}", 
                   workType, location, item);

        // Validate the proposed location first
        LocationValidationResult locationValidation = validateLocationForWork(workType, location, item, quantity);
        
        if (!locationValidation.isSuitable()) {
            String message = "Location not suitable: " + String.join(", ", locationValidation.getViolations());
            return EnhancedWorkCreationResult.locationValidationFailed(message);
        }

        // Validate work creation
        WorkCreationService.WorkCreationResult workValidation = workCreationService.validateWorkCreation(workType, location, item, quantity);
        if (!workValidation.isSuccess()) {
            return EnhancedWorkCreationResult.failure(workValidation.getErrorMessage());
        }

        try {
            Work work = workCreationService.createWork(workType, location, item, quantity, assignedTo);
            return EnhancedWorkCreationResult.success(work);
        } catch (Exception e) {
            logger.error("Failed to create work: {}", e.getMessage());
            return EnhancedWorkCreationResult.failure("Work creation failed: " + e.getMessage());
        }
    }

    // Helper classes
    public static class WorkItemRequest {
        public final SkuCode item;
        public final Quantity quantity;
        public final String assignedTo;

        public WorkItemRequest(SkuCode item, Quantity quantity, String assignedTo) {
            this.item = item;
            this.quantity = quantity;
            this.assignedTo = assignedTo;
        }
    }

    public static class LocationValidationResult {
        private final boolean suitable;
        private final double score;
        private final int applicableDirectives;
        private final List<String> violations;

        private LocationValidationResult(boolean suitable, double score, int applicableDirectives, 
                                       List<String> violations) {
            this.suitable = suitable;
            this.score = score;
            this.applicableDirectives = applicableDirectives;
            this.violations = violations != null ? List.copyOf(violations) : List.of();
        }

        public static LocationValidationResult suitable(double score, int applicableDirectives) {
            return new LocationValidationResult(true, score, applicableDirectives, null);
        }

        public static LocationValidationResult unsuitable(List<String> violations) {
            return new LocationValidationResult(false, 0.0, 0, violations);
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

    public static class EnhancedWorkCreationResult {
        private final boolean success;
        private final String errorMessage;
        private final Work work;

        private EnhancedWorkCreationResult(boolean success, String errorMessage, Work work) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.work = work;
        }

        public static EnhancedWorkCreationResult success(Work work) {
            return new EnhancedWorkCreationResult(true, null, work);
        }

        public static EnhancedWorkCreationResult locationValidationFailed(String message) {
            return new EnhancedWorkCreationResult(false, message, null);
        }

        public static EnhancedWorkCreationResult failure(String message) {
            return new EnhancedWorkCreationResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Work getWork() {
            return work;
        }

        public boolean hasWork() {
            return work != null;
        }
    }
}