package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class QualitySamplingPlan {
    private final QualitySamplingPlanId planId;
    private final String name;
    private final String description;
    private final SkuCode applicableItem;
    private final QualitySamplingStrategy strategy;
    private final int sampleSize;
    private final double acceptanceQualityLevel; // AQL
    private final List<QualityTestType> requiredTests;
    private final boolean active;
    private final Instant createdAt;
    private final String createdBy;

    public QualitySamplingPlan(QualitySamplingPlanId planId, String name, String description,
                             SkuCode applicableItem, QualitySamplingStrategy strategy,
                             int sampleSize, double acceptanceQualityLevel,
                             List<QualityTestType> requiredTests, String createdBy) {
        this.planId = Objects.requireNonNull(planId, "Plan ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = description;
        this.applicableItem = applicableItem; // null means applies to all items
        this.strategy = Objects.requireNonNull(strategy, "Strategy cannot be null");
        this.sampleSize = validateSampleSize(strategy, sampleSize);
        this.acceptanceQualityLevel = validateAQL(acceptanceQualityLevel);
        this.requiredTests = Objects.requireNonNull(requiredTests, "Required tests cannot be null");
        this.active = true;
        this.createdAt = Instant.now();
        this.createdBy = Objects.requireNonNull(createdBy, "Created by cannot be null");
    }

    public QualitySample createSample(String batchNumber, int totalQuantity) {
        int calculatedSampleSize = calculateSampleSize(totalQuantity);
        return new QualitySample(
            QualitySampleId.generate(),
            this.planId,
            batchNumber,
            this.applicableItem,
            calculatedSampleSize,
            this.requiredTests
        );
    }

    public boolean isApplicableTo(SkuCode item) {
        return applicableItem == null || applicableItem.equals(item);
    }

    private int calculateSampleSize(int totalQuantity) {
        return switch (strategy) {
            case FIXED_SIZE -> sampleSize;
            case PERCENTAGE -> Math.max(1, (int) Math.ceil(totalQuantity * (sampleSize / 100.0)));
            case SQUARE_ROOT -> Math.max(1, (int) Math.ceil(Math.sqrt(totalQuantity)));
            case MIL_STD_105E -> calculateMilStdSampleSize(totalQuantity);
        };
    }

    private int calculateMilStdSampleSize(int lotSize) {
        // Simplified MIL-STD-105E sample size calculation
        if (lotSize <= 8) return 2;
        if (lotSize <= 25) return 3;
        if (lotSize <= 50) return 5;
        if (lotSize <= 90) return 8;
        if (lotSize <= 150) return 13;
        if (lotSize <= 280) return 20;
        if (lotSize <= 500) return 32;
        if (lotSize <= 1200) return 50;
        if (lotSize <= 3200) return 80;
        if (lotSize <= 10000) return 125;
        return 200;
    }

    private int validateSampleSize(QualitySamplingStrategy strategy, int size) {
        if (strategy == QualitySamplingStrategy.FIXED_SIZE || strategy == QualitySamplingStrategy.PERCENTAGE) {
            if (size <= 0) {
                throw new IllegalArgumentException("Sample size must be positive");
            }
            return size;
        }

        if (size < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }

        return size;
    }

    private double validateAQL(double aql) {
        if (aql < 0 || aql > 100) {
            throw new IllegalArgumentException("AQL must be between 0 and 100");
        }
        return aql;
    }

    // Getters
    public QualitySamplingPlanId getPlanId() { return planId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkuCode getApplicableItem() { return applicableItem; }
    public QualitySamplingStrategy getStrategy() { return strategy; }
    public int getSampleSize() { return sampleSize; }
    public double getAcceptanceQualityLevel() { return acceptanceQualityLevel; }
    public List<QualityTestType> getRequiredTests() { return requiredTests; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
}
