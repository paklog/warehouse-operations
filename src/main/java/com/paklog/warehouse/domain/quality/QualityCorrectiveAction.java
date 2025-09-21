package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QualityCorrectiveAction extends AggregateRoot {
    private final QualityCorrectiveActionId actionId;
    private final QualityNonConformanceId nonConformanceId;
    private final SkuCode affectedItem;
    private final String description;
    private final QualityCorrectiveActionType actionType;
    private final QualityCorrectiveActionPriority priority;
    private final String assignedTo;
    private final Instant assignedAt;
    private final Instant dueDate;
    private QualityCorrectiveActionStatus status;
    private final List<QualityCorrectiveActionStep> steps;
    private String completedBy;
    private Instant completedAt;
    private String completionNotes;
    private QualityCorrectiveActionEffectiveness effectiveness;

    public QualityCorrectiveAction(QualityCorrectiveActionId actionId, 
                                 QualityNonConformanceId nonConformanceId,
                                 SkuCode affectedItem, String description,
                                 QualityCorrectiveActionType actionType,
                                 QualityCorrectiveActionPriority priority,
                                 String assignedTo, int dueDays) {
        this.actionId = Objects.requireNonNull(actionId, "Action ID cannot be null");
        this.nonConformanceId = Objects.requireNonNull(nonConformanceId, "Non-conformance ID cannot be null");
        this.affectedItem = affectedItem;
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.actionType = Objects.requireNonNull(actionType, "Action type cannot be null");
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
        this.assignedTo = Objects.requireNonNull(assignedTo, "Assigned to cannot be null");
        this.assignedAt = Instant.now();
        this.dueDate = assignedAt.plus(dueDays, ChronoUnit.DAYS);
        this.status = QualityCorrectiveActionStatus.ASSIGNED;
        this.steps = new ArrayList<>();

        addDomainEvent(new QualityCorrectiveActionCreatedEvent(actionId, nonConformanceId,
                      description, actionType, priority, assignedTo, assignedAt, dueDate));
    }

    public void startImplementation() {
        if (this.status != QualityCorrectiveActionStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot start implementation - status is " + status);
        }

        this.status = QualityCorrectiveActionStatus.IN_PROGRESS;
        addDomainEvent(new QualityCorrectiveActionStartedEvent(actionId, Instant.now()));
    }

    public void addStep(String description, String assignedTo, int dueDays) {
        if (this.status == QualityCorrectiveActionStatus.COMPLETED || 
            this.status == QualityCorrectiveActionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot add step - action is " + status);
        }

        QualityCorrectiveActionStep step = new QualityCorrectiveActionStep(
            steps.size() + 1, description, assignedTo, dueDays
        );
        this.steps.add(step);

        addDomainEvent(new QualityCorrectiveActionStepAddedEvent(actionId, step, Instant.now()));
    }

    public void completeStep(int stepNumber, String completedBy, String notes) {
        if (this.status == QualityCorrectiveActionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete step - action is cancelled");
        }

        QualityCorrectiveActionStep step = findStep(stepNumber);
        if (step.isCompleted()) {
            throw new IllegalStateException("Step " + stepNumber + " is already completed");
        }

        if (this.status == QualityCorrectiveActionStatus.ASSIGNED) {
            // Automatically transition to in-progress when the first step is worked on
            startImplementation();
        } else if (this.status != QualityCorrectiveActionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete step - status is " + status);
        }

        step.complete(completedBy, notes);
        addDomainEvent(new QualityCorrectiveActionStepCompletedEvent(actionId, stepNumber, 
                      completedBy, Instant.now(), notes));

        // Check if all steps are complete
        if (areAllStepsComplete()) {
            completeAction(completedBy, "All steps completed");
        }
    }

    public void completeAction(String completedBy, String notes) {
        if (this.status != QualityCorrectiveActionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete action - status is " + status);
        }

        this.status = QualityCorrectiveActionStatus.COMPLETED;
        this.completedBy = Objects.requireNonNull(completedBy, "Completed by cannot be null");
        this.completedAt = Instant.now();
        this.completionNotes = notes;

        addDomainEvent(new QualityCorrectiveActionCompletedEvent(actionId, completedBy, 
                      completedAt, notes));
    }

    public void verifyEffectiveness(QualityCorrectiveActionEffectiveness effectiveness, 
                                   String verifiedBy, String notes) {
        if (this.status != QualityCorrectiveActionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot verify effectiveness - action is not completed");
        }

        this.effectiveness = Objects.requireNonNull(effectiveness, "Effectiveness cannot be null");
        addDomainEvent(new QualityCorrectiveActionEffectivenessVerifiedEvent(actionId, 
                      effectiveness, verifiedBy, notes, Instant.now()));
    }

    public void cancel(String cancelledBy, String reason) {
        if (this.status == QualityCorrectiveActionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed action");
        }

        this.status = QualityCorrectiveActionStatus.CANCELLED;
        addDomainEvent(new QualityCorrectiveActionCancelledEvent(actionId, cancelledBy, 
                      reason, Instant.now()));
    }

    public boolean isOverdue() {
        return Instant.now().isAfter(dueDate) && 
               !isFinalStatus() && 
               effectiveness != QualityCorrectiveActionEffectiveness.VERIFIED;
    }

    private QualityCorrectiveActionStep findStep(int stepNumber) {
        return steps.stream()
                   .filter(step -> step.getStepNumber() == stepNumber)
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Step " + stepNumber + " not found"));
    }

    private boolean areAllStepsComplete() {
        return steps.stream().allMatch(QualityCorrectiveActionStep::isCompleted);
    }

    private boolean isFinalStatus() {
        return status == QualityCorrectiveActionStatus.COMPLETED || 
               status == QualityCorrectiveActionStatus.CANCELLED;
    }

    // Getters
    public QualityCorrectiveActionId getActionId() { return actionId; }
    public QualityNonConformanceId getNonConformanceId() { return nonConformanceId; }
    public SkuCode getAffectedItem() { return affectedItem; }
    public String getDescription() { return description; }
    public QualityCorrectiveActionType getActionType() { return actionType; }
    public QualityCorrectiveActionPriority getPriority() { return priority; }
    public String getAssignedTo() { return assignedTo; }
    public Instant getAssignedAt() { return assignedAt; }
    public Instant getDueDate() { return dueDate; }
    public QualityCorrectiveActionStatus getStatus() { return status; }
    public List<QualityCorrectiveActionStep> getSteps() { return new ArrayList<>(steps); }
    public String getCompletedBy() { return completedBy; }
    public Instant getCompletedAt() { return completedAt; }
    public String getCompletionNotes() { return completionNotes; }
    public QualityCorrectiveActionEffectiveness getEffectiveness() { return effectiveness; }

    // Corrective Action Step inner class
    public static class QualityCorrectiveActionStep {
        private final int stepNumber;
        private final String description;
        private final String assignedTo;
        private final Instant dueDate;
        private boolean completed;
        private String completedBy;
        private Instant completedAt;
        private String notes;

        public QualityCorrectiveActionStep(int stepNumber, String description, 
                                         String assignedTo, int dueDays) {
            this.stepNumber = stepNumber;
            this.description = Objects.requireNonNull(description, "Description cannot be null");
            this.assignedTo = Objects.requireNonNull(assignedTo, "Assigned to cannot be null");
            this.dueDate = Instant.now().plus(dueDays, ChronoUnit.DAYS);
            this.completed = false;
        }

        public void complete(String completedBy, String notes) {
            this.completed = true;
            this.completedBy = completedBy;
            this.completedAt = Instant.now();
            this.notes = notes;
        }

        public boolean isOverdue() {
            return !completed && Instant.now().isAfter(dueDate);
        }

        // Getters
        public int getStepNumber() { return stepNumber; }
        public String getDescription() { return description; }
        public String getAssignedTo() { return assignedTo; }
        public Instant getDueDate() { return dueDate; }
        public boolean isCompleted() { return completed; }
        public String getCompletedBy() { return completedBy; }
        public Instant getCompletedAt() { return completedAt; }
        public String getNotes() { return notes; }
    }
}
