package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.quality.*;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "quality_inspections")
public class QualityInspectionDocument {
    @Id
    private String id;
    private String inspectionId;
    private String inspectionType;
    private String status;
    private String itemSkuCode;
    private int inspectionQuantity;
    private BinLocationDocument location;
    private String lotNumber;
    private String serialNumber;
    private String supplierReference;
    private QualityInspectionPlanDocument inspectionPlan;
    private List<QualityInspectionStepDocument> inspectionSteps;
    private List<QualityTestResultDocument> testResults;
    private List<QualityNonConformanceDocument> nonConformances;
    private String finalDecision;
    private String inspectorId;
    private String supervisorId;
    private Instant scheduledDate;
    private Instant startedAt;
    private Instant completedAt;
    private String comments;
    private Map<String, Object> attributes;
    private int version;

    // Constructors
    public QualityInspectionDocument() {}

    public QualityInspectionDocument(QualityInspection inspection) {
        this.id = inspection.getInspectionId().getValue().toString();
        this.inspectionId = inspection.getInspectionId().getValue().toString();
        this.inspectionType = inspection.getInspectionType().name();
        this.status = inspection.getStatus().name();
        this.itemSkuCode = inspection.getItem().getValue();
        this.inspectionQuantity = inspection.getInspectionQuantity().getValue();
        this.location = new BinLocationDocument(inspection.getLocation());
        this.lotNumber = inspection.getLotNumber();
        this.serialNumber = inspection.getSerialNumber();
        this.supplierReference = inspection.getSupplierReference();
        this.inspectionPlan = new QualityInspectionPlanDocument(inspection.getInspectionPlan());
        this.inspectionSteps = inspection.getInspectionSteps().stream()
            .map(QualityInspectionStepDocument::new)
            .toList();
        this.testResults = inspection.getTestResults().stream()
            .map(QualityTestResultDocument::new)
            .toList();
        this.nonConformances = inspection.getNonConformances().stream()
            .map(QualityNonConformanceDocument::new)
            .toList();
        this.finalDecision = inspection.getFinalDecision() != null ? 
            inspection.getFinalDecision().name() : null;
        this.inspectorId = inspection.getInspectorId();
        this.supervisorId = inspection.getSupervisorId();
        this.scheduledDate = inspection.getScheduledDate();
        this.startedAt = inspection.getStartedAt();
        this.completedAt = inspection.getCompletedAt();
        this.comments = inspection.getComments();
        this.attributes = inspection.getAttributes();
        this.version = inspection.getVersion();
    }

    public QualityInspection toDomain() {
        QualityInspectionId inspectionId = QualityInspectionId.of(this.inspectionId);
        QualityInspectionType type = QualityInspectionType.valueOf(this.inspectionType);
        QualityInspectionStatus status = QualityInspectionStatus.valueOf(this.status);
        SkuCode item = SkuCode.of(this.itemSkuCode);
        Quantity quantity = new Quantity(this.inspectionQuantity);
        BinLocation location = this.location.toDomain();
        QualityInspectionPlan plan = this.inspectionPlan.toDomain();
        
        List<QualityInspectionStep> steps = this.inspectionSteps.stream()
            .map(QualityInspectionStepDocument::toDomain)
            .toList();
        
        List<QualityTestResult> results = this.testResults.stream()
            .map(QualityTestResultDocument::toDomain)
            .toList();
        
        List<QualityNonConformance> nonConformances = this.nonConformances.stream()
            .map(QualityNonConformanceDocument::toDomain)
            .toList();
        
        QualityDecision decision = this.finalDecision != null ? 
            QualityDecision.valueOf(this.finalDecision) : null;

        return new QualityInspection(
            inspectionId, type, status, item, quantity, location,
            this.lotNumber, this.serialNumber, this.supplierReference,
            plan, steps, results, nonConformances, decision,
            this.inspectorId, this.supervisorId, this.scheduledDate,
            this.startedAt, this.completedAt, this.comments,
            this.attributes, this.version
        );
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }

    public String getInspectionType() { return inspectionType; }
    public void setInspectionType(String inspectionType) { this.inspectionType = inspectionType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getItemSkuCode() { return itemSkuCode; }
    public void setItemSkuCode(String itemSkuCode) { this.itemSkuCode = itemSkuCode; }

    public int getInspectionQuantity() { return inspectionQuantity; }
    public void setInspectionQuantity(int inspectionQuantity) { this.inspectionQuantity = inspectionQuantity; }

    public BinLocationDocument getLocation() { return location; }
    public void setLocation(BinLocationDocument location) { this.location = location; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getSupplierReference() { return supplierReference; }
    public void setSupplierReference(String supplierReference) { this.supplierReference = supplierReference; }

    public QualityInspectionPlanDocument getInspectionPlan() { return inspectionPlan; }
    public void setInspectionPlan(QualityInspectionPlanDocument inspectionPlan) { this.inspectionPlan = inspectionPlan; }

    public List<QualityInspectionStepDocument> getInspectionSteps() { return inspectionSteps; }
    public void setInspectionSteps(List<QualityInspectionStepDocument> inspectionSteps) { this.inspectionSteps = inspectionSteps; }

    public List<QualityTestResultDocument> getTestResults() { return testResults; }
    public void setTestResults(List<QualityTestResultDocument> testResults) { this.testResults = testResults; }

    public List<QualityNonConformanceDocument> getNonConformances() { return nonConformances; }
    public void setNonConformances(List<QualityNonConformanceDocument> nonConformances) { this.nonConformances = nonConformances; }

    public String getFinalDecision() { return finalDecision; }
    public void setFinalDecision(String finalDecision) { this.finalDecision = finalDecision; }

    public String getInspectorId() { return inspectorId; }
    public void setInspectorId(String inspectorId) { this.inspectorId = inspectorId; }

    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }

    public Instant getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Instant scheduledDate) { this.scheduledDate = scheduledDate; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    // Nested document classes
    public static class BinLocationDocument {
        private String aisle;
        private String rack;
        private String level;

        public BinLocationDocument() {}

        public BinLocationDocument(BinLocation location) {
            this.aisle = location.getAisle();
            this.rack = location.getRack();
            this.level = location.getLevel();
        }

        public BinLocation toDomain() {
            return BinLocation.of(aisle, rack, level);
        }

        // Getters and setters
        public String getAisle() { return aisle; }
        public void setAisle(String aisle) { this.aisle = aisle; }

        public String getRack() { return rack; }
        public void setRack(String rack) { this.rack = rack; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }

    public static class QualityInspectionPlanDocument {
        private String planId;
        private String name;
        private String description;
        private List<QualityStepTemplateDocument> stepTemplates;
        private boolean active;

        public QualityInspectionPlanDocument() {}

        public QualityInspectionPlanDocument(QualityInspectionPlan plan) {
            this.planId = plan.getPlanId();
            this.name = plan.getName();
            this.description = plan.getDescription();
            this.stepTemplates = plan.getStepTemplates().stream()
                .map(QualityStepTemplateDocument::new)
                .toList();
            this.active = plan.isActive();
        }

        public QualityInspectionPlan toDomain() {
            List<QualityStepTemplate> templates = this.stepTemplates.stream()
                .map(QualityStepTemplateDocument::toDomain)
                .toList();
            return new QualityInspectionPlan(planId, name, description, templates, active);
        }

        // Getters and setters
        public String getPlanId() { return planId; }
        public void setPlanId(String planId) { this.planId = planId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<QualityStepTemplateDocument> getStepTemplates() { return stepTemplates; }
        public void setStepTemplates(List<QualityStepTemplateDocument> stepTemplates) { this.stepTemplates = stepTemplates; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static class QualityStepTemplateDocument {
        private int stepNumber;
        private String name;
        private String description;
        private String testType;
        private boolean mandatory;
        private String expectedValue;
        private QualityToleranceRangeDocument toleranceRange;
        private String unit;

        public QualityStepTemplateDocument() {}

        public QualityStepTemplateDocument(QualityStepTemplate template) {
            this.stepNumber = template.getStepNumber();
            this.name = template.getName();
            this.description = template.getDescription();
            this.testType = template.getTestType().name();
            this.mandatory = template.isMandatory();
            this.expectedValue = template.getExpectedValue();
            this.toleranceRange = new QualityToleranceRangeDocument(template.getToleranceRange());
            this.unit = template.getUnit();
        }

        public QualityStepTemplate toDomain() {
            return new QualityStepTemplate(
                stepNumber, name, description,
                QualityTestType.valueOf(testType), mandatory,
                expectedValue, toleranceRange.toDomain(), unit
            );
        }

        // Getters and setters
        public int getStepNumber() { return stepNumber; }
        public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }

        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

        public String getExpectedValue() { return expectedValue; }
        public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

        public QualityToleranceRangeDocument getToleranceRange() { return toleranceRange; }
        public void setToleranceRange(QualityToleranceRangeDocument toleranceRange) { this.toleranceRange = toleranceRange; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    public static class QualityToleranceRangeDocument {
        private double minValue;
        private double maxValue;
        private String unit;

        public QualityToleranceRangeDocument() {}

        public QualityToleranceRangeDocument(QualityToleranceRange range) {
            this.minValue = range.getMinValue();
            this.maxValue = range.getMaxValue();
            this.unit = range.getUnit();
        }

        public QualityToleranceRange toDomain() {
            return new QualityToleranceRange(minValue, maxValue, unit);
        }

        // Getters and setters
        public double getMinValue() { return minValue; }
        public void setMinValue(double minValue) { this.minValue = minValue; }

        public double getMaxValue() { return maxValue; }
        public void setMaxValue(double maxValue) { this.maxValue = maxValue; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    public static class QualityInspectionStepDocument {
        private int stepNumber;
        private String name;
        private String description;
        private String testType;
        private boolean mandatory;
        private String expectedValue;
        private QualityToleranceRangeDocument toleranceRange;
        private String unit;
        private boolean completed;
        private QualityTestResultDocument testResult;
        private String notes;
        private Instant completedAt;

        public QualityInspectionStepDocument() {}

        public QualityInspectionStepDocument(QualityInspectionStep step) {
            this.stepNumber = step.getStepNumber();
            this.name = step.getName();
            this.description = step.getDescription();
            this.testType = step.getTestType().name();
            this.mandatory = step.isMandatory();
            this.expectedValue = step.getExpectedValue();
            this.toleranceRange = new QualityToleranceRangeDocument(step.getToleranceRange());
            this.unit = step.getUnit();
            this.completed = step.isCompleted();
            this.testResult = step.getTestResult() != null ? 
                new QualityTestResultDocument(step.getTestResult()) : null;
            this.notes = step.getNotes();
            this.completedAt = step.getCompletedAt();
        }

        public QualityInspectionStep toDomain() {
            QualityInspectionStep step = new QualityInspectionStep(
                stepNumber, name, description,
                QualityTestType.valueOf(testType), mandatory,
                expectedValue, toleranceRange.toDomain(), unit
            );
            
            if (completed && testResult != null) {
                step.complete(testResult.toDomain(), notes);
            }
            
            return step;
        }

        // Getters and setters (similar pattern as above)
        public int getStepNumber() { return stepNumber; }
        public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }

        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

        public String getExpectedValue() { return expectedValue; }
        public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

        public QualityToleranceRangeDocument getToleranceRange() { return toleranceRange; }
        public void setToleranceRange(QualityToleranceRangeDocument toleranceRange) { this.toleranceRange = toleranceRange; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }

        public QualityTestResultDocument getTestResult() { return testResult; }
        public void setTestResult(QualityTestResultDocument testResult) { this.testResult = testResult; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public Instant getCompletedAt() { return completedAt; }
        public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    }

    public static class QualityTestResultDocument {
        private String testType;
        private String actualValue;
        private String expectedValue;
        private boolean passed;
        private String unit;
        private String notes;
        private Instant testedAt;

        public QualityTestResultDocument() {}

        public QualityTestResultDocument(QualityTestResult result) {
            this.testType = result.getTestType().name();
            this.actualValue = result.getActualValue();
            this.expectedValue = result.getExpectedValue();
            this.passed = result.isPassed();
            this.unit = result.getUnit();
            this.notes = result.getNotes();
            this.testedAt = result.getTestedAt();
        }

        public QualityTestResult toDomain() {
            return new QualityTestResult(
                QualityTestType.valueOf(testType),
                actualValue, expectedValue, passed,
                unit, notes, testedAt
            );
        }

        // Getters and setters
        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }

        public String getActualValue() { return actualValue; }
        public void setActualValue(String actualValue) { this.actualValue = actualValue; }

        public String getExpectedValue() { return expectedValue; }
        public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public Instant getTestedAt() { return testedAt; }
        public void setTestedAt(Instant testedAt) { this.testedAt = testedAt; }
    }

    public static class QualityNonConformanceDocument {
        private String id;
        private String type;
        private String description;
        private String severity;
        private String status;
        private String identifiedBy;
        private Instant identifiedAt;

        public QualityNonConformanceDocument() {}

        public QualityNonConformanceDocument(QualityNonConformance nonConformance) {
            this.id = nonConformance.getId().getValue().toString();
            this.type = nonConformance.getType().name();
            this.description = nonConformance.getDescription();
            this.severity = nonConformance.getSeverity().name();
            this.status = nonConformance.getStatus().name();
            this.identifiedBy = nonConformance.getIdentifiedBy();
            this.identifiedAt = nonConformance.getIdentifiedAt();
        }

        public QualityNonConformance toDomain() {
            return new QualityNonConformance(
                QualityNonConformanceId.of(id),
                QualityNonConformanceType.valueOf(type),
                description,
                QualitySeverity.valueOf(severity),
                identifiedBy
            );
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getIdentifiedBy() { return identifiedBy; }
        public void setIdentifiedBy(String identifiedBy) { this.identifiedBy = identifiedBy; }

        public Instant getIdentifiedAt() { return identifiedAt; }
        public void setIdentifiedAt(Instant identifiedAt) { this.identifiedAt = identifiedAt; }
    }
}