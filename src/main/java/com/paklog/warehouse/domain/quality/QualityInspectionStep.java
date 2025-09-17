package com.paklog.warehouse.domain.quality;

import java.time.Instant;
import java.util.Objects;

public class QualityInspectionStep {
    private final int stepNumber;
    private final String name;
    private final String description;
    private final QualityTestType testType;
    private final boolean mandatory;
    private final String expectedValue;
    private final QualityToleranceRange toleranceRange;
    private final String unit;
    private boolean completed;
    private QualityTestResult testResult;
    private String notes;
    private Instant completedAt;
    private String completedBy;

    public QualityInspectionStep(int stepNumber, String name, String description, 
                               QualityTestType testType, boolean mandatory, 
                               String expectedValue, QualityToleranceRange toleranceRange, 
                               String unit) {
        if (stepNumber < 1) {
            throw new IllegalArgumentException("Step number must be positive");
        }
        
        this.stepNumber = stepNumber;
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = description;
        this.testType = Objects.requireNonNull(testType, "Test type cannot be null");
        this.mandatory = mandatory;
        this.expectedValue = expectedValue;
        this.toleranceRange = toleranceRange;
        this.unit = unit;
        this.completed = false;
    }

    public void complete(QualityTestResult testResult, String notes) {
        if (this.completed) {
            throw new IllegalStateException("Step is already completed");
        }
        
        this.testResult = Objects.requireNonNull(testResult, "Test result cannot be null");
        this.notes = notes;
        this.completed = true;
        this.completedAt = Instant.now();
    }

    public void markCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    public boolean isWithinTolerance(String actualValue) {
        if (toleranceRange == null || expectedValue == null || actualValue == null) {
            return Objects.equals(expectedValue, actualValue);
        }
        
        return toleranceRange.isWithinTolerance(expectedValue, actualValue);
    }

    public boolean requiresNumericComparison() {
        return testType == QualityTestType.MEASUREMENT || testType == QualityTestType.WEIGHT ||
               testType == QualityTestType.DIMENSION;
    }

    public boolean requiresVisualInspection() {
        return testType == QualityTestType.VISUAL_INSPECTION || testType == QualityTestType.APPEARANCE;
    }

    // Getters
    public int getStepNumber() { return stepNumber; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public QualityTestType getTestType() { return testType; }
    public boolean isMandatory() { return mandatory; }
    public String getExpectedValue() { return expectedValue; }
    public QualityToleranceRange getToleranceRange() { return toleranceRange; }
    public String getUnit() { return unit; }
    
    // Convenience method for tests
    public int getSequence() { return stepNumber; }
    public boolean isCompleted() { return completed; }
    public QualityTestResult getTestResult() { return testResult; }
    public String getNotes() { return notes; }
    public Instant getCompletedAt() { return completedAt; }
    public String getCompletedBy() { return completedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityInspectionStep that = (QualityInspectionStep) o;
        return stepNumber == that.stepNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepNumber);
    }

    @Override
    public String toString() {
        return "QualityInspectionStep{" +
                "stepNumber=" + stepNumber +
                ", name='" + name + '\'' +
                ", testType=" + testType +
                ", mandatory=" + mandatory +
                ", completed=" + completed +
                '}';
    }
}