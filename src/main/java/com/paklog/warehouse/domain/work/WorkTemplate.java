package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.quality.QualityInspectionType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WorkTemplate extends AggregateRoot {
    private final WorkTemplateId id;
    private String name;
    private String description;
    private WorkType workType;
    private final List<WorkStep> steps;
    private boolean active;
    private boolean qualityInspectionRequired;
    private QualityInspectionType requiredInspectionType;
    private Instant createdAt;
    private Instant lastModifiedAt;
    private String createdBy;
    private int version;

    public WorkTemplate(String name, String description, WorkType workType) {
        this.id = WorkTemplateId.generate();
        this.name = Objects.requireNonNull(name, "Template name cannot be null");
        this.description = description;
        this.workType = Objects.requireNonNull(workType, "Work type cannot be null");
        this.steps = new ArrayList<>();
        this.active = true;
        this.qualityInspectionRequired = false;
        this.createdAt = Instant.now();
        this.lastModifiedAt = Instant.now();
        this.version = 1;

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be empty");
        }
    }

    // Constructor for repository adapter
    public WorkTemplate(WorkTemplateId id, String name, String description, WorkType workType,
                       List<WorkStep> steps, boolean active, boolean qualityInspectionRequired,
                       QualityInspectionType requiredInspectionType, Instant createdAt, 
                       Instant lastModifiedAt, String createdBy, int version) {
        this.id = Objects.requireNonNull(id, "WorkTemplateId cannot be null");
        this.name = Objects.requireNonNull(name, "Template name cannot be null");
        this.description = description;
        this.workType = Objects.requireNonNull(workType, "Work type cannot be null");
        this.steps = new ArrayList<>(Objects.requireNonNull(steps, "Steps cannot be null"));
        this.active = active;
        this.qualityInspectionRequired = qualityInspectionRequired;
        this.requiredInspectionType = requiredInspectionType;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.createdBy = createdBy;
        this.version = version;
    }

    public void addStep(WorkStep step) {
        Objects.requireNonNull(step, "Work step cannot be null");
        
        // Validate sequence uniqueness
        if (steps.stream().anyMatch(s -> s.getSequence() == step.getSequence())) {
            throw new IllegalArgumentException("Step sequence " + step.getSequence() + " already exists");
        }

        steps.add(step);
        steps.sort((s1, s2) -> Integer.compare(s1.getSequence(), s2.getSequence()));
        this.lastModifiedAt = Instant.now();
    }

    public void removeStep(int sequence) {
        boolean removed = steps.removeIf(step -> step.getSequence() == sequence);
        if (removed) {
            this.lastModifiedAt = Instant.now();
        } else {
            throw new IllegalArgumentException("No step found with sequence: " + sequence);
        }
    }

    public void updateStep(WorkStep updatedStep) {
        Objects.requireNonNull(updatedStep, "Updated step cannot be null");
        
        int index = -1;
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getSequence() == updatedStep.getSequence()) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            throw new IllegalArgumentException("No step found with sequence: " + updatedStep.getSequence());
        }
        
        steps.set(index, updatedStep);
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
        Objects.requireNonNull(newName, "Template name cannot be null");
        if (newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be empty");
        }
        this.name = newName.trim();
        this.lastModifiedAt = Instant.now();
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
        this.lastModifiedAt = Instant.now();
    }

    public void enableQualityInspection(QualityInspectionType inspectionType) {
        this.qualityInspectionRequired = true;
        this.requiredInspectionType = Objects.requireNonNull(inspectionType, "Inspection type cannot be null");
        this.lastModifiedAt = Instant.now();
    }

    public void disableQualityInspection() {
        this.qualityInspectionRequired = false;
        this.requiredInspectionType = null;
        this.lastModifiedAt = Instant.now();
    }

    public Work generateWork(WorkRequest request) {
        if (!active) {
            throw new IllegalStateException("Cannot generate work from inactive template");
        }
        
        if (steps.isEmpty()) {
            throw new IllegalStateException("Cannot generate work from template with no steps");
        }

        return WorkBuilder.fromTemplate(this)
                .withLocation(request.getLocation())
                .withItem(request.getItem())
                .withQuantity(request.getQuantity())
                .withAssignedTo(request.getAssignedTo())
                .build();
    }

    public boolean isValid() {
        if (steps.isEmpty()) {
            return false;
        }

        // Check for sequence gaps
        List<Integer> sequences = steps.stream()
                .map(WorkStep::getSequence)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < sequences.size(); i++) {
            if (sequences.get(i) != i + 1) {
                return false; // Sequence gap found
            }
        }

        // Validate work type consistency
        return steps.stream().allMatch(this::isStepValidForWorkType);
    }

    private boolean isStepValidForWorkType(WorkStep step) {
        switch (workType) {
            case PICK:
                return isValidPickStep(step);
            case PUT:
                return isValidPutStep(step);
            case COUNT:
                return isValidCountStep(step);
            case PACK:
                return isValidPackStep(step);
            default:
                return true; // Allow all steps for other work types
        }
    }

    private boolean isValidPickStep(WorkStep step) {
        // Pick operations must include location navigation and item scanning
        WorkAction action = step.getAction();
        return action != WorkAction.PUT_ITEM; // Can't put during pick operation
    }

    private boolean isValidPutStep(WorkStep step) {
        // Put operations must include location navigation and item placement
        WorkAction action = step.getAction();
        return action != WorkAction.PICK_ITEM; // Can't pick during put operation
    }

    private boolean isValidCountStep(WorkStep step) {
        // Count operations require quantity confirmation
        return true; // Most actions are valid for counting
    }

    private boolean isValidPackStep(WorkStep step) {
        // Pack operations focus on container preparation
        return true; // Most actions are valid for packing
    }

    // Getters
    public WorkTemplateId getId() {
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

    public List<WorkStep> getSteps() {
        return Collections.unmodifiableList(steps);
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

    public int getStepCount() {
        return steps.size();
    }

    public boolean isQualityInspectionRequired() {
        return qualityInspectionRequired;
    }

    public QualityInspectionType getRequiredInspectionType() {
        return requiredInspectionType;
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
        WorkTemplate that = (WorkTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WorkTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", workType=" + workType +
                ", stepCount=" + steps.size() +
                ", active=" + active +
                '}';
    }
}