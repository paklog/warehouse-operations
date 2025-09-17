package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class QualityInspection extends AggregateRoot {
    private final QualityInspectionId inspectionId;
    private QualityInspectionType inspectionType;
    private QualityInspectionStatus status;
    private SkuCode item;
    private Quantity inspectionQuantity;
    private BinLocation location;
    private String lotNumber;
    private String serialNumber;
    private String supplierReference;
    private QualityInspectionPlan inspectionPlan;
    private final List<QualityInspectionStep> inspectionSteps;
    private final List<QualityTestResult> testResults;
    private final List<QualityNonConformance> nonConformances;
    private QualityDecision finalDecision;
    private String inspectorId;
    private String supervisorId;
    private Instant scheduledDate;
    private Instant startedAt;
    private Instant completedAt;
    private String comments;
    private final Map<String, Object> attributes;
    private int version;

    // Simplified constructor for testing
    public QualityInspection(QualityInspectionId inspectionId, QualityInspectionType inspectionType,
                           SkuCode item, String batchNumber, int quantity, String inspectorId) {
        this(inspectionId, inspectionType, item, new Quantity(quantity), 
             BinLocation.of("A01-B01-L01"), new QualityInspectionPlan("default-plan-001", 
             "Default Plan", "Default inspection plan", List.of(), true), inspectorId);
        this.lotNumber = batchNumber;
    }

    public QualityInspection(QualityInspectionId inspectionId, QualityInspectionType inspectionType,
                           SkuCode item, Quantity inspectionQuantity, BinLocation location,
                           QualityInspectionPlan inspectionPlan, String inspectorId) {
        this.inspectionId = Objects.requireNonNull(inspectionId, "Inspection ID cannot be null");
        this.inspectionType = Objects.requireNonNull(inspectionType, "Inspection type cannot be null");
        this.item = Objects.requireNonNull(item, "Item cannot be null");
        this.inspectionQuantity = Objects.requireNonNull(inspectionQuantity, "Inspection quantity cannot be null");
        this.location = Objects.requireNonNull(location, "Location cannot be null");
        this.inspectionPlan = Objects.requireNonNull(inspectionPlan, "Inspection plan cannot be null");
        this.inspectorId = Objects.requireNonNull(inspectorId, "Inspector ID cannot be null");
        
        this.status = QualityInspectionStatus.SCHEDULED;
        this.inspectionSteps = new ArrayList<>();
        this.testResults = new ArrayList<>();
        this.nonConformances = new ArrayList<>();
        this.attributes = new HashMap<>();
        this.scheduledDate = Instant.now();
        this.version = 1;

        // Create inspection steps from plan
        createInspectionStepsFromPlan();

        registerEvent(new QualityInspectionScheduledEvent(this.inspectionId, this.inspectionType,
                                                        this.item, this.inspectorId, this.scheduledDate));
    }

    // Full constructor for loading from persistence
    public QualityInspection(QualityInspectionId inspectionId, QualityInspectionType inspectionType,
                           QualityInspectionStatus status, SkuCode item, Quantity inspectionQuantity,
                           BinLocation location, String lotNumber, String serialNumber,
                           String supplierReference, QualityInspectionPlan inspectionPlan,
                           List<QualityInspectionStep> inspectionSteps, List<QualityTestResult> testResults,
                           List<QualityNonConformance> nonConformances, QualityDecision finalDecision,
                           String inspectorId, String supervisorId, Instant scheduledDate,
                           Instant startedAt, Instant completedAt, String comments,
                           Map<String, Object> attributes, int version) {
        this.inspectionId = inspectionId;
        this.inspectionType = inspectionType;
        this.status = status;
        this.item = item;
        this.inspectionQuantity = inspectionQuantity;
        this.location = location;
        this.lotNumber = lotNumber;
        this.serialNumber = serialNumber;
        this.supplierReference = supplierReference;
        this.inspectionPlan = inspectionPlan;
        this.inspectionSteps = new ArrayList<>(inspectionSteps != null ? inspectionSteps : List.of());
        this.testResults = new ArrayList<>(testResults != null ? testResults : List.of());
        this.nonConformances = new ArrayList<>(nonConformances != null ? nonConformances : List.of());
        this.finalDecision = finalDecision;
        this.inspectorId = inspectorId;
        this.supervisorId = supervisorId;
        this.scheduledDate = scheduledDate;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.comments = comments;
        this.attributes = new HashMap<>(attributes != null ? attributes : Map.of());
        this.version = version;
    }

    public void startInspection(String actualInspectorId) {
        if (status != QualityInspectionStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot start inspection in status: " + status);
        }

        this.status = QualityInspectionStatus.IN_PROGRESS;
        this.inspectorId = actualInspectorId;
        this.startedAt = Instant.now();

        registerEvent(new QualityInspectionStartedEvent(this.inspectionId, actualInspectorId, this.startedAt));
    }

    public void completeInspectionStep(int stepNumber, QualityTestResult testResult, String notes) {
        if (status != QualityInspectionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete step when inspection is not in progress");
        }

        QualityInspectionStep step = findStepByNumber(stepNumber);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepNumber);
        }

        step.complete(testResult, notes);
        testResults.add(testResult);

        // Check if test failed and create non-conformance
        if (!testResult.isPassed()) {
            createNonConformance(step, testResult);
        }

        registerEvent(new QualityInspectionStepCompletedEvent(this.inspectionId, stepNumber,
                                                            testResult, step.getCompletedAt()));
    }

    public void completeInspection(QualityDecision decision, String supervisorId, String comments) {
        if (status != QualityInspectionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete inspection in status: " + status);
        }

        // Validate all mandatory steps are completed
        List<QualityInspectionStep> incompleteSteps = inspectionSteps.stream()
            .filter(step -> step.isMandatory() && !step.isCompleted())
            .collect(Collectors.toList());

        if (!incompleteSteps.isEmpty()) {
            throw new IllegalStateException("Cannot complete inspection with incomplete mandatory steps: " +
                                          incompleteSteps.stream()
                                                  .map(s -> String.valueOf(s.getStepNumber()))
                                                  .collect(Collectors.joining(", ")));
        }

        this.status = QualityInspectionStatus.COMPLETED;
        this.finalDecision = Objects.requireNonNull(decision, "Final decision cannot be null");
        this.supervisorId = supervisorId;
        this.comments = comments;
        this.completedAt = Instant.now();

        registerEvent(new QualityInspectionCompletedEvent(this.inspectionId, decision, supervisorId,
                                                         this.completedAt, hasNonConformances()));
    }

    public void putOnHold(QualityHoldReason reason, String notes, String heldBy) {
        if (status == QualityInspectionStatus.COMPLETED || status == QualityInspectionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot put on hold inspection in status: " + status);
        }

        this.status = QualityInspectionStatus.ON_HOLD;
        
        registerEvent(new QualityInspectionHeldEvent(this.inspectionId, reason, notes, heldBy, Instant.now()));
    }

    public void releaseFromHold(String notes, String releasedBy) {
        if (status != QualityInspectionStatus.ON_HOLD) {
            throw new IllegalStateException("Cannot release from hold inspection in status: " + status);
        }

        this.status = startedAt != null ? QualityInspectionStatus.IN_PROGRESS : QualityInspectionStatus.SCHEDULED;
        
        registerEvent(new QualityInspectionReleasedEvent(this.inspectionId, notes, releasedBy, Instant.now()));
    }

    public void cancel(String reason, String cancelledBy) {
        if (status == QualityInspectionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed inspection");
        }

        QualityInspectionStatus previousStatus = this.status;
        this.status = QualityInspectionStatus.CANCELLED;

        registerEvent(new QualityInspectionCancelledEvent(this.inspectionId, previousStatus, 
                                                        reason, cancelledBy, Instant.now()));
    }

    public void addNonConformance(QualityNonConformanceType type, String description, 
                                 QualitySeverity severity, String identifiedBy) {
        QualityNonConformance nonConformance = new QualityNonConformance(
            QualityNonConformanceId.generate(), type, description, severity, identifiedBy);
        
        nonConformances.add(nonConformance);
        
        registerEvent(new QualityNonConformanceAddedEvent(this.inspectionId, nonConformance.getId(),
                                                         type, severity, identifiedBy, Instant.now()));
    }

    public void assignSupervisor(String supervisorId) {
        this.supervisorId = Objects.requireNonNull(supervisorId, "Supervisor ID cannot be null");
        registerEvent(new QualityInspectionSupervisorAssignedEvent(this.inspectionId, supervisorId, Instant.now()));
    }

    public void updateSchedule(Instant newScheduledDate) {
        if (status != QualityInspectionStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot reschedule inspection in status: " + status);
        }

        Instant previousDate = this.scheduledDate;
        this.scheduledDate = Objects.requireNonNull(newScheduledDate, "Scheduled date cannot be null");

        registerEvent(new QualityInspectionRescheduledEvent(this.inspectionId, previousDate, 
                                                          newScheduledDate, Instant.now()));
    }

    // Business logic methods
    public boolean isCompleted() {
        return status == QualityInspectionStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == QualityInspectionStatus.IN_PROGRESS;
    }

    public boolean isOnHold() {
        return status == QualityInspectionStatus.ON_HOLD;
    }

    public boolean hasNonConformances() {
        return !nonConformances.isEmpty();
    }

    public boolean hasCriticalNonConformances() {
        return nonConformances.stream()
            .anyMatch(nc -> nc.getSeverity() == QualitySeverity.CRITICAL);
    }

    public double getCompletionPercentage() {
        if (inspectionSteps.isEmpty()) {
            return 0.0;
        }
        
        long completedSteps = inspectionSteps.stream()
            .mapToLong(step -> step.isCompleted() ? 1 : 0)
            .sum();
        
        return (double) completedSteps / inspectionSteps.size() * 100.0;
    }

    public List<QualityInspectionStep> getMandatorySteps() {
        return inspectionSteps.stream()
            .filter(QualityInspectionStep::isMandatory)
            .collect(Collectors.toList());
    }

    public List<QualityInspectionStep> getFailedSteps() {
        return inspectionSteps.stream()
            .filter(step -> step.isCompleted() && step.getTestResult() != null && !step.getTestResult().isPassed())
            .collect(Collectors.toList());
    }

    public boolean isOverdue() {
        return scheduledDate != null && Instant.now().isAfter(scheduledDate) && 
               status == QualityInspectionStatus.SCHEDULED;
    }

    private void createInspectionStepsFromPlan() {
        if (inspectionPlan == null || inspectionPlan.getStepTemplates() == null) {
            return;
        }

        for (QualityStepTemplate template : inspectionPlan.getStepTemplates()) {
            QualityInspectionStep step = new QualityInspectionStep(
                template.getStepNumber(),
                template.getName(),
                template.getDescription(),
                template.getTestType(),
                template.isMandatory(),
                template.getExpectedValue(),
                template.getToleranceRange(),
                template.getUnit()
            );
            inspectionSteps.add(step);
        }
    }

    private QualityInspectionStep findStepByNumber(int stepNumber) {
        return inspectionSteps.stream()
            .filter(step -> step.getStepNumber() == stepNumber)
            .findFirst()
            .orElse(null);
    }

    private void createNonConformance(QualityInspectionStep step, QualityTestResult testResult) {
        QualityNonConformanceType type = determineNonConformanceType(step, testResult);
        QualitySeverity severity = determineSeverity(step, testResult);
        
        String description = String.format("Step %d (%s) failed: Expected %s, Got %s",
            step.getStepNumber(), step.getName(), step.getExpectedValue(), testResult.getActualValue());
        
        addNonConformance(type, description, severity, inspectorId);
    }

    private QualityNonConformanceType determineNonConformanceType(QualityInspectionStep step, 
                                                                 QualityTestResult testResult) {
        // Logic to determine non-conformance type based on step and result
        return QualityNonConformanceType.SPECIFICATION_DEVIATION;
    }

    private QualitySeverity determineSeverity(QualityInspectionStep step, QualityTestResult testResult) {
        // Logic to determine severity based on deviation from expected values
        return step.isMandatory() ? QualitySeverity.HIGH : QualitySeverity.MEDIUM;
    }

    // Getters
    public QualityInspectionId getInspectionId() { return inspectionId; }
    public QualityInspectionType getInspectionType() { return inspectionType; }
    public QualityInspectionStatus getStatus() { return status; }
    public SkuCode getItem() { return item; }
    public Quantity getInspectionQuantity() { return inspectionQuantity; }
    public BinLocation getLocation() { return location; }
    public String getLotNumber() { return lotNumber; }
    public String getSerialNumber() { return serialNumber; }
    public String getSupplierReference() { return supplierReference; }
    public QualityInspectionPlan getInspectionPlan() { return inspectionPlan; }
    public List<QualityInspectionStep> getInspectionSteps() { return Collections.unmodifiableList(inspectionSteps); }
    public List<QualityTestResult> getTestResults() { return Collections.unmodifiableList(testResults); }
    public List<QualityNonConformance> getNonConformances() { return Collections.unmodifiableList(nonConformances); }
    public QualityDecision getFinalDecision() { return finalDecision; }
    public String getInspectorId() { return inspectorId; }
    public String getSupervisorId() { return supervisorId; }
    public Instant getScheduledDate() { return scheduledDate; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getComments() { return comments; }
    public Map<String, Object> getAttributes() { return Collections.unmodifiableMap(attributes); }
    public int getVersion() { return version; }

    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setSupplierReference(String supplierReference) { this.supplierReference = supplierReference; }

    // Convenience methods for tests
    public String getBatchNumber() { return lotNumber; }
    public int getQuantity() { return inspectionQuantity != null ? inspectionQuantity.getValue() : 0; }
    public Instant getScheduledAt() { return scheduledDate; }
    public List<QualityInspectionStep> getSteps() { return getInspectionSteps(); }
    public void start(String inspectorId) { startInspection(inspectorId); }
    
    public void addStep(QualityStepTemplate stepTemplate) {
        QualityInspectionStep step = new QualityInspectionStep(
            stepTemplate.getStepNumber(),
            stepTemplate.getName(),
            stepTemplate.getDescription(),
            stepTemplate.getTestType(),
            stepTemplate.isMandatory(),
            stepTemplate.getExpectedValue(),
            stepTemplate.getToleranceRange(),
            stepTemplate.getUnit()
        );
        inspectionSteps.add(step);
    }
    
    public void completeStep(int stepNumber, QualityTestResult testResult, String notes) {
        completeInspectionStep(stepNumber, testResult, notes);
    }
    
    public void complete(QualityDecision decision, String supervisorId, boolean approved) {
        completeInspection(decision, supervisorId, "Completed");
    }
    
    public void hold(QualityHoldReason reason, String notes, String heldBy) {
        putOnHold(reason, notes, heldBy);
    }
    
    public void release(String notes, String releasedBy) {
        releaseFromHold(notes, releasedBy);
    }
    
    public void reschedule(Instant newDate) {
        updateSchedule(newDate);
    }
    
    public List<QualityNonConformanceId> getNonConformanceIds() {
        return nonConformances.stream()
            .map(QualityNonConformance::getId)
            .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityInspection that = (QualityInspection) o;
        return Objects.equals(inspectionId, that.inspectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inspectionId);
    }

    @Override
    public String toString() {
        return "QualityInspection{" +
                "inspectionId=" + inspectionId +
                ", inspectionType=" + inspectionType +
                ", status=" + status +
                ", item=" + item +
                ", location=" + location +
                ", inspectorId='" + inspectorId + '\'' +
                '}';
    }
}