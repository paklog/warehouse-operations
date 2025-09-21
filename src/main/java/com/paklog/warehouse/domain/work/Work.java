package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.quality.QualityInspectionId;
import com.paklog.warehouse.domain.quality.QualityInspectionType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Work extends AggregateRoot {
    private final WorkId workId;
    private final WorkTemplateId templateId;
    private final WorkType workType;
    private WorkStatus status;
    private final BinLocation location;
    private final SkuCode item;
    private final Quantity quantity;
    private String assignedTo;
    private final List<WorkStep> steps;
    private int currentStepIndex;
    private Instant createdAt;
    private Instant assignedAt;
    private Instant startedAt;
    private Instant completedAt;
    private String priority;
    private boolean qualityInspectionRequired;
    private QualityInspectionType requiredInspectionType;
    private QualityInspectionId qualityInspectionId;
    private boolean qualityApproved;

    public Work(WorkTemplateId templateId, WorkType workType, BinLocation location, SkuCode item, 
               Quantity quantity, List<WorkStep> steps) {
        this(templateId, workType, location, item, quantity, steps, false, null);
    }

    public Work(WorkTemplateId templateId, WorkType workType, BinLocation location, SkuCode item, 
               Quantity quantity, List<WorkStep> steps, boolean qualityInspectionRequired,
               QualityInspectionType requiredInspectionType) {
        this.workId = WorkId.generate();
        this.templateId = Objects.requireNonNull(templateId, "Template ID cannot be null");
        this.workType = Objects.requireNonNull(workType, "Work Type cannot be null");
        this.location = Objects.requireNonNull(location, "Location cannot be null");
        this.item = Objects.requireNonNull(item, "Item cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
        this.steps = new ArrayList<>(Objects.requireNonNull(steps, "Steps cannot be null"));
        this.status = WorkStatus.CREATED;
        this.currentStepIndex = 0;
        this.createdAt = Instant.now();
        this.priority = "NORMAL";
        this.qualityInspectionRequired = qualityInspectionRequired;
        this.requiredInspectionType = requiredInspectionType;
        this.qualityApproved = !qualityInspectionRequired; // Only approved if no inspection required

        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Work must have at least one step");
        }
    }

    public void assignTo(String workerId) {
        if (workerId == null || workerId.isBlank()) {
            if (status == WorkStatus.ASSIGNED) {
                this.assignedTo = null;
                this.status = WorkStatus.RELEASED;
                return;
            }

            if (status == WorkStatus.CREATED || status == WorkStatus.RELEASED) {
                this.assignedTo = null;
                return;
            }

            throw new IllegalStateException("Cannot unassign work in status: " + status);
        }

        if (status != WorkStatus.CREATED && status != WorkStatus.RELEASED && status != WorkStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot assign work in status: " + status);
        }

        this.assignedTo = workerId;
        this.status = WorkStatus.ASSIGNED;
        this.assignedAt = Instant.now();

        registerEvent(new WorkAssignedEvent(this.workId.getValue(), this.assignedTo));
    }

    public void start() {
        if (status != WorkStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot start work in status: " + status);
        }
        
        this.status = WorkStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
        
        registerEvent(new WorkStartedEvent(this.workId.getValue(), this.assignedTo));
    }

    public WorkStep getCurrentStep() {
        if (currentStepIndex >= steps.size()) {
            return null; // All steps completed
        }
        return steps.get(currentStepIndex);
    }

    public boolean completeCurrentStep() {
        if (status != WorkStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete step when work is not in progress");
        }
        
        if (currentStepIndex >= steps.size()) {
            throw new IllegalStateException("No more steps to complete");
        }
        
        currentStepIndex++;
        
        if (currentStepIndex >= steps.size()) {
            // All steps completed
            complete();
            return true;
        }
        
        return false; // More steps remaining
    }

    public void complete() {
        if (status != WorkStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete work in status: " + status);
        }
        
        this.status = WorkStatus.COMPLETED;
        this.completedAt = Instant.now();
        
        registerEvent(new WorkCompletedEvent(this.workId.getValue(), this.assignedTo, this.completedAt));
    }

    public void cancel(String reason) {
        if (status == WorkStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed work");
        }
        
        this.status = WorkStatus.CANCELLED;
        
        registerEvent(new WorkCancelledEvent(this.workId.getValue(), reason, Instant.now()));
    }

    public void release() {
        if (status != WorkStatus.CREATED) {
            throw new IllegalStateException("Cannot release work in status: " + status);
        }
        
        this.status = WorkStatus.RELEASED;
        
        registerEvent(new WorkReleasedEvent(this.workId.getValue()));
    }

    public boolean isComplete() {
        return status == WorkStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == WorkStatus.IN_PROGRESS;
    }

    public boolean isAssigned() {
        return status == WorkStatus.ASSIGNED;
    }

    public double getProgress() {
        if (steps.isEmpty()) {
            return 0.0;
        }
        return (double) currentStepIndex / steps.size();
    }

    public int getRemainingSteps() {
        return Math.max(0, steps.size() - currentStepIndex);
    }

    // Getters
    public WorkId getWorkId() {
        return workId;
    }

    public WorkTemplateId getTemplateId() {
        return templateId;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public WorkStatus getStatus() {
        return status;
    }

    public BinLocation getLocation() {
        return location;
    }

    public SkuCode getItem() {
        return item;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public List<WorkStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isQualityInspectionRequired() {
        return qualityInspectionRequired;
    }

    public QualityInspectionType getRequiredInspectionType() {
        return requiredInspectionType;
    }

    public QualityInspectionId getQualityInspectionId() {
        return qualityInspectionId;
    }

    public boolean isQualityApproved() {
        return qualityApproved;
    }

    public void assignQualityInspection(QualityInspectionId inspectionId) {
        if (!qualityInspectionRequired) {
            throw new IllegalStateException("Quality inspection is not required for this work");
        }
        this.qualityInspectionId = Objects.requireNonNull(inspectionId, "Inspection ID cannot be null");
        this.qualityApproved = false;
    }

    public void approveQuality() {
        if (qualityInspectionRequired && qualityInspectionId == null) {
            throw new IllegalStateException("Cannot approve quality without assigned inspection");
        }
        this.qualityApproved = true;
    }

    public void rejectQuality() {
        this.qualityApproved = false;
    }

    public boolean canBeCompleted() {
        return isComplete() && qualityApproved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Work work = (Work) o;
        return Objects.equals(workId, work.workId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId);
    }

    @Override
    public String toString() {
        return "Work{" +
                "workId=" + workId +
                ", status=" + status +
                ", location=" + location +
                ", item=" + item +
                ", assignedTo='" + assignedTo + '\'' +
                ", progress=" + getProgress() +
                '}';
    }
}
