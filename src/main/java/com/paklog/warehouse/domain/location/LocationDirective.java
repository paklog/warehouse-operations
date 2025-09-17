package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.work.WorkType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LocationDirective extends AggregateRoot {
    private final LocationDirectiveId id;
    private String name;
    private String description;
    private WorkType workType;
    private LocationStrategy strategy;
    private final List<LocationConstraint> constraints;
    private int priority;
    private boolean active;
    private Instant createdAt;
    private Instant lastModifiedAt;
    private String createdBy;
    private int version;

    public LocationDirective(String name, String description, WorkType workType, 
                           LocationStrategy strategy, int priority) {
        this.id = LocationDirectiveId.generate();
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = description;
        this.workType = Objects.requireNonNull(workType, "Work type cannot be null");
        this.strategy = Objects.requireNonNull(strategy, "Strategy cannot be null");
        this.constraints = new ArrayList<>();
        this.priority = priority;
        this.active = true;
        this.createdAt = Instant.now();
        this.lastModifiedAt = Instant.now();
        this.version = 1;

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (priority < 1) {
            throw new IllegalArgumentException("Priority must be positive");
        }
    }

    // Constructor for repository adapter
    public LocationDirective(LocationDirectiveId id, String name, String description,
                           WorkType workType, LocationStrategy strategy, 
                           List<LocationConstraint> constraints, int priority,
                           boolean active, Instant createdAt, Instant lastModifiedAt,
                           String createdBy, int version) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = description;
        this.workType = Objects.requireNonNull(workType, "Work type cannot be null");
        this.strategy = Objects.requireNonNull(strategy, "Strategy cannot be null");
        this.constraints = new ArrayList<>(Objects.requireNonNull(constraints, "Constraints cannot be null"));
        this.priority = priority;
        this.active = active;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.createdBy = createdBy;
        this.version = version;
    }

    public void addConstraint(LocationConstraint constraint) {
        Objects.requireNonNull(constraint, "Constraint cannot be null");
        constraints.add(constraint);
        this.lastModifiedAt = Instant.now();
    }

    public void removeConstraint(LocationConstraint constraint) {
        boolean removed = constraints.remove(constraint);
        if (removed) {
            this.lastModifiedAt = Instant.now();
        }
    }

    public void updateStrategy(LocationStrategy newStrategy) {
        this.strategy = Objects.requireNonNull(newStrategy, "Strategy cannot be null");
        this.lastModifiedAt = Instant.now();
    }

    public void updatePriority(int newPriority) {
        if (newPriority < 1) {
            throw new IllegalArgumentException("Priority must be positive");
        }
        this.priority = newPriority;
        this.lastModifiedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.lastModifiedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.lastModifiedAt = Instant.now();
    }

    public void updateName(String newName) {
        Objects.requireNonNull(newName, "Name cannot be null");
        if (newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = newName.trim();
        this.lastModifiedAt = Instant.now();
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
        this.lastModifiedAt = Instant.now();
    }

    public boolean isApplicableFor(WorkType queryWorkType) {
        return this.workType == queryWorkType && this.active;
    }

    public boolean satisfiesConstraints(LocationContext context) {
        if (!active) {
            return false;
        }

        return constraints.stream().allMatch(constraint -> constraint.evaluate(context));
    }

    public BinLocation selectLocation(LocationQuery query) {
        if (!isApplicableFor(query.getWorkType())) {
            return null;
        }

        // Delegate to strategy-specific location selection
        LocationSelector selector = createSelector();
        return selector.selectOptimalLocation(query, this);
    }

    private LocationSelector createSelector() {
        switch (strategy) {
            case FIXED:
                return new FixedLocationSelector();
            case NEAREST_EMPTY:
                return new NearestEmptyLocationSelector();
            case BULK_LOCATION:
                return new BulkLocationSelector();
            case FAST_MOVING:
                return new FastMovingLocationSelector();
            case ZONE_BASED:
                return new ZoneBasedLocationSelector();
            case CAPACITY_OPTIMIZED:
                return new CapacityOptimizedLocationSelector();
            case FIFO:
                return new FifoLocationSelector();
            case LIFO:
                return new LifoLocationSelector();
            case LOWEST_LEVEL:
                return new LowestLevelLocationSelector();
            case HIGHEST_LEVEL:
                return new HighestLevelLocationSelector();
            case RANDOM:
            default:
                return new RandomLocationSelector();
        }
    }

    public LocationDirectiveResult evaluateForLocation(LocationQuery query, BinLocation location) {
        if (!isApplicableFor(query.getWorkType())) {
            return LocationDirectiveResult.notApplicable("Work type not supported");
        }

        LocationContext context = query.createContextForLocation(location);
        
        if (!satisfiesConstraints(context)) {
            List<String> violations = new ArrayList<>();
            for (LocationConstraint constraint : constraints) {
                if (!constraint.evaluate(context)) {
                    violations.add(constraint.toString());
                }
            }
            return LocationDirectiveResult.constraintViolation(violations);
        }

        return LocationDirectiveResult.suitable(calculateScore(context));
    }

    private double calculateScore(LocationContext context) {
        // Base score from priority (higher priority = higher base score)
        double score = priority * 100.0;

        // Add strategy-specific scoring
        switch (strategy) {
            case NEAREST_EMPTY:
                score += calculateDistanceScore(context);
                break;
            case CAPACITY_OPTIMIZED:
                score += calculateCapacityScore(context);
                break;
            case FAST_MOVING:
                score += calculateVelocityScore(context);
                break;
            default:
                // Use default scoring
                break;
        }

        return Math.max(0, score);
    }

    private double calculateDistanceScore(LocationContext context) {
        // Simple distance calculation - in real implementation would use actual coordinates
        BinLocation location = context.getLocation();
        int aisleDistance = parseAisleDistance(location.getAisle());
        int rackDistance = Math.abs(Integer.parseInt(location.getRack()) - 1);
        int levelDistance = Math.abs(Integer.parseInt(location.getLevel()) - 1);
        int distance = aisleDistance + rackDistance + levelDistance;
        
        return Math.max(0, 100 - distance * 10); // Closer locations get higher scores
    }
    
    private int parseAisleDistance(String aisle) {
        try {
            // Try parsing as number first
            return Math.abs(Integer.parseInt(aisle) - 1);
        } catch (NumberFormatException e) {
            // Handle alphabetic aisles (A, B, C...)
            if (aisle.length() == 1 && Character.isLetter(aisle.charAt(0))) {
                return Math.abs(Character.toUpperCase(aisle.charAt(0)) - 'A');
            }
            // Default fallback
            return 0;
        }
    }

    private double calculateCapacityScore(LocationContext context) {
        Double capacity = context.getAvailableCapacity();
        return capacity != null ? Math.min(100, capacity * 10) : 0;
    }

    private double calculateVelocityScore(LocationContext context) {
        String zone = context.getLocationZone();
        // Fast-moving zones get higher scores
        if ("FAST_PICK".equals(zone)) {
            return 50.0;
        } else if ("MEDIUM_PICK".equals(zone)) {
            return 25.0;
        }
        return 0.0;
    }

    // Getters
    public LocationDirectiveId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public LocationStrategy getStrategy() {
        return strategy;
    }

    public List<LocationConstraint> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    public int getPriority() {
        return priority;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getVersion() {
        return version;
    }

    public int getConstraintCount() {
        return constraints.size();
    }

    // Setters for repository adapter
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationDirective that = (LocationDirective) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LocationDirective{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", workType=" + workType +
                ", strategy=" + strategy +
                ", priority=" + priority +
                ", active=" + active +
                '}';
    }
}