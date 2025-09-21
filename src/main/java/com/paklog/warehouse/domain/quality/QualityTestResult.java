package com.paklog.warehouse.domain.quality;

import java.time.Instant;
import java.util.Objects;

public class QualityTestResult {
    private final String actualValue;
    private final boolean passed;
    private final String unit;
    private final QualityTestType testType;
    private final String notes;
    private final Instant measuredAt;
    private final String measuredBy;
    private final String expectedValue;

    public QualityTestResult(String actualValue, boolean passed, String unit,
                           QualityTestType testType, String notes, String measuredBy) {
        this.actualValue = actualValue;
        this.passed = passed;
        this.unit = unit;
        this.testType = Objects.requireNonNull(testType, "Test type cannot be null");
        this.notes = notes;
        this.measuredAt = Instant.now();
        this.measuredBy = measuredBy;
        this.expectedValue = null;
    }

    // Constructor for document deserialization
    public QualityTestResult(QualityTestType testType, String actualValue, String expectedValue,
                           boolean passed, String unit, String notes, Instant testedAt) {
        this.testType = Objects.requireNonNull(testType, "Test type cannot be null");
        this.actualValue = actualValue;
        this.expectedValue = expectedValue;
        this.passed = passed;
        this.unit = unit;
        this.notes = notes;
        this.measuredAt = testedAt;
        this.measuredBy = null;
    }

    public static QualityTestResult pass(String actualValue, QualityTestType testType, String measuredBy) {
        return new QualityTestResult(actualValue, true, null, testType, null, measuredBy);
    }

    public static QualityTestResult fail(String actualValue, QualityTestType testType, 
                                       String notes, String measuredBy) {
        return new QualityTestResult(actualValue, false, null, testType, notes, measuredBy);
    }

    public static QualityTestResult measurement(String actualValue, String unit, boolean passed, 
                                              QualityTestType testType, String measuredBy) {
        return new QualityTestResult(actualValue, passed, unit, testType, null, measuredBy);
    }

    // Getters
    public String getActualValue() { return actualValue; }
    public boolean isPassed() { return passed; }
    public String getUnit() { return unit; }
    public QualityTestType getTestType() { return testType; }
    public String getNotes() { return notes; }
    public Instant getMeasuredAt() { return measuredAt; }
    public Instant getTestedAt() { return measuredAt; }  // Alias for getMeasuredAt
    public String getMeasuredBy() { return measuredBy; }
    
    // Additional method that may be needed
    public String getExpectedValue() { return expectedValue; }

    @Override
    public String toString() {
        return "QualityTestResult{" +
                "actualValue='" + actualValue + '\'' +
                ", passed=" + passed +
                ", testType=" + testType +
                ", measuredBy='" + measuredBy + '\'' +
                '}';
    }
}