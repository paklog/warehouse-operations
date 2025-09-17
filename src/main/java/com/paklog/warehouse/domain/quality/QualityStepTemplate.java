package com.paklog.warehouse.domain.quality;

import java.util.Objects;

public class QualityStepTemplate {
    private final int stepNumber;
    private final String name;
    private final String description;
    private final QualityTestType testType;
    private final boolean mandatory;
    private final String expectedValue;
    private final QualityToleranceRange toleranceRange;
    private final String unit;

    public QualityStepTemplate(int stepNumber, String name, String description,
                              QualityTestType testType, boolean mandatory,
                              String expectedValue, QualityToleranceRange toleranceRange,
                              String unit) {
        this.stepNumber = stepNumber;
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = description;
        this.testType = Objects.requireNonNull(testType, "Test type cannot be null");
        this.mandatory = mandatory;
        this.expectedValue = expectedValue;
        this.toleranceRange = toleranceRange;
        this.unit = unit;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityStepTemplate that = (QualityStepTemplate) o;
        return stepNumber == that.stepNumber && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepNumber, name);
    }
}