package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QualitySample extends AggregateRoot {
    private final QualitySampleId sampleId;
    private final QualitySamplingPlanId samplingPlanId;
    private final String batchNumber;
    private final SkuCode item;
    private final int sampleSize;
    private final List<QualityTestType> requiredTests;
    private final Instant collectedAt;
    private final String collectedBy;
    private QualitySampleStatus status;
    private final List<QualityTestResult> testResults;
    private QualitySampleVerdict verdict;
    private String notes;

    public QualitySample(QualitySampleId sampleId, QualitySamplingPlanId samplingPlanId,
                        String batchNumber, SkuCode item, int sampleSize,
                        List<QualityTestType> requiredTests) {
        this.sampleId = Objects.requireNonNull(sampleId, "Sample ID cannot be null");
        this.samplingPlanId = Objects.requireNonNull(samplingPlanId, "Sampling plan ID cannot be null");
        this.batchNumber = batchNumber;
        this.item = item;
        this.sampleSize = sampleSize;
        this.requiredTests = new ArrayList<>(Objects.requireNonNull(requiredTests, "Required tests cannot be null"));
        this.collectedAt = Instant.now();
        this.collectedBy = "SYSTEM"; // Could be parameterized
        this.status = QualitySampleStatus.COLLECTED;
        this.testResults = new ArrayList<>();

        addDomainEvent(new QualitySampleCollectedEvent(sampleId, samplingPlanId, batchNumber, 
                      item, sampleSize, collectedAt));
    }

    public void startTesting() {
        if (this.status != QualitySampleStatus.COLLECTED) {
            throw new IllegalStateException("Cannot start testing - sample status is " + status);
        }

        this.status = QualitySampleStatus.TESTING_IN_PROGRESS;
        addDomainEvent(new QualitySampleTestingStartedEvent(sampleId, Instant.now()));
    }

    public void addTestResult(QualityTestResult testResult) {
        if (this.status != QualitySampleStatus.TESTING_IN_PROGRESS) {
            throw new IllegalStateException("Cannot add test result - sample status is " + status);
        }

        if (!requiredTests.contains(testResult.getTestType())) {
            throw new IllegalArgumentException("Test type " + testResult.getTestType() + 
                                             " is not required for this sample");
        }

        this.testResults.add(Objects.requireNonNull(testResult, "Test result cannot be null"));
        
        addDomainEvent(new QualitySampleTestCompletedEvent(sampleId, testResult, Instant.now()));

        // Check if all required tests are complete
        if (areAllTestsComplete()) {
            completeTesting();
        }
    }

    public void completeTesting() {
        if (this.status != QualitySampleStatus.TESTING_IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete testing - sample status is " + status);
        }

        if (!areAllTestsComplete()) {
            throw new IllegalStateException("Cannot complete testing - not all required tests are complete");
        }

        this.status = QualitySampleStatus.TESTING_COMPLETED;
        this.verdict = determineVerdict();

        addDomainEvent(new QualitySampleTestingCompletedEvent(sampleId, verdict, Instant.now()));
    }

    public void reject(String reason) {
        this.status = QualitySampleStatus.REJECTED;
        this.verdict = QualitySampleVerdict.REJECTED;
        this.notes = reason;

        addDomainEvent(new QualitySampleRejectedEvent(sampleId, reason, Instant.now()));
    }

    private boolean areAllTestsComplete() {
        return requiredTests.stream()
                .allMatch(requiredTest -> 
                    testResults.stream()
                        .anyMatch(result -> result.getTestType().equals(requiredTest)));
    }

    private QualitySampleVerdict determineVerdict() {
        boolean allPassed = testResults.stream().allMatch(QualityTestResult::isPassed);
        return allPassed ? QualitySampleVerdict.ACCEPTED : QualitySampleVerdict.REJECTED;
    }

    public boolean isComplete() {
        return status == QualitySampleStatus.TESTING_COMPLETED || status == QualitySampleStatus.REJECTED;
    }

    // Getters
    public QualitySampleId getSampleId() { return sampleId; }
    public QualitySamplingPlanId getSamplingPlanId() { return samplingPlanId; }
    public String getBatchNumber() { return batchNumber; }
    public SkuCode getItem() { return item; }
    public int getSampleSize() { return sampleSize; }
    public List<QualityTestType> getRequiredTests() { return new ArrayList<>(requiredTests); }
    public Instant getCollectedAt() { return collectedAt; }
    public String getCollectedBy() { return collectedBy; }
    public QualitySampleStatus getStatus() { return status; }
    public List<QualityTestResult> getTestResults() { return new ArrayList<>(testResults); }
    public QualitySampleVerdict getVerdict() { return verdict; }
    public String getNotes() { return notes; }
}