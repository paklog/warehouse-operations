package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QualitySamplingPlanTest {

    private QualitySamplingPlan samplingPlan;
    private QualitySamplingPlanId planId;
    private SkuCode applicableItem;
    private List<QualityTestType> requiredTests;

    @BeforeEach
    void setUp() {
        planId = QualitySamplingPlanId.generate();
        applicableItem = new SkuCode("TEST-ITEM-001");
        requiredTests = Arrays.asList(
            QualityTestType.VISUAL_INSPECTION,
            QualityTestType.WEIGHT,
            QualityTestType.DIMENSION
        );
        
        samplingPlan = new QualitySamplingPlan(
            planId,
            "Standard Incoming Inspection",
            "Standard sampling plan for incoming materials",
            applicableItem,
            QualitySamplingStrategy.FIXED_SIZE,
            5, // Fixed sample size of 5
            2.5, // 2.5% AQL
            requiredTests,
            "qa-manager-001"
        );
    }

    @Test
    void shouldCreateSamplingPlanWithCorrectInitialState() {
        assertEquals(planId, samplingPlan.getPlanId());
        assertEquals("Standard Incoming Inspection", samplingPlan.getName());
        assertEquals("Standard sampling plan for incoming materials", samplingPlan.getDescription());
        assertEquals(applicableItem, samplingPlan.getApplicableItem());
        assertEquals(QualitySamplingStrategy.FIXED_SIZE, samplingPlan.getStrategy());
        assertEquals(5, samplingPlan.getSampleSize());
        assertEquals(2.5, samplingPlan.getAcceptanceQualityLevel());
        assertEquals(requiredTests, samplingPlan.getRequiredTests());
        assertTrue(samplingPlan.isActive());
        assertEquals("qa-manager-001", samplingPlan.getCreatedBy());
        assertNotNull(samplingPlan.getCreatedAt());
    }

    @Test
    void shouldCreateSampleWithFixedSize() {
        QualitySample sample = samplingPlan.createSample("BATCH-001", 100);
        
        assertNotNull(sample);
        assertEquals(planId, sample.getSamplingPlanId());
        assertEquals("BATCH-001", sample.getBatchNumber());
        assertEquals(applicableItem, sample.getItem());
        assertEquals(5, sample.getSampleSize()); // Fixed size regardless of lot size
        assertEquals(requiredTests, sample.getRequiredTests());
    }

    @Test
    void shouldCreateSampleWithPercentageStrategy() {
        QualitySamplingPlan percentagePlan = new QualitySamplingPlan(
            QualitySamplingPlanId.generate(),
            "Percentage Plan",
            "10% sampling",
            null, // Applies to all items
            QualitySamplingStrategy.PERCENTAGE,
            10, // 10% of lot size
            1.0, // 1.0% AQL
            requiredTests,
            "qa-manager-001"
        );
        
        QualitySample sample = percentagePlan.createSample("BATCH-002", 50);
        
        assertEquals(5, sample.getSampleSize()); // 10% of 50 = 5
    }

    @Test
    void shouldCreateSampleWithSquareRootStrategy() {
        QualitySamplingPlan sqrtPlan = new QualitySamplingPlan(
            QualitySamplingPlanId.generate(),
            "Square Root Plan",
            "Square root sampling",
            null,
            QualitySamplingStrategy.SQUARE_ROOT,
            0, // Not used for square root
            1.5, // 1.5% AQL
            requiredTests,
            "qa-manager-001"
        );
        
        QualitySample sample = sqrtPlan.createSample("BATCH-003", 100);
        
        assertEquals(10, sample.getSampleSize()); // sqrt(100) = 10
    }

    @Test
    void shouldCreateSampleWithMilStdStrategy() {
        QualitySamplingPlan milStdPlan = new QualitySamplingPlan(
            QualitySamplingPlanId.generate(),
            "MIL-STD Plan",
            "MIL-STD-105E sampling",
            null,
            QualitySamplingStrategy.MIL_STD_105E,
            0, // Not used for MIL-STD
            2.0, // 2.0% AQL
            requiredTests,
            "qa-manager-001"
        );
        
        QualitySample smallLotSample = milStdPlan.createSample("BATCH-004", 25);
        assertEquals(3, smallLotSample.getSampleSize()); // MIL-STD for lot size 25
        
        QualitySample largeLotSample = milStdPlan.createSample("BATCH-005", 500);
        assertEquals(32, largeLotSample.getSampleSize()); // MIL-STD for lot size 500
    }

    @Test
    void shouldApplyToSpecificItem() {
        assertTrue(samplingPlan.isApplicableTo(applicableItem));
        assertFalse(samplingPlan.isApplicableTo(new SkuCode("OTHER-ITEM")));
    }

    @Test
    void shouldApplyToAllItemsWhenItemIsNull() {
        QualitySamplingPlan universalPlan = new QualitySamplingPlan(
            QualitySamplingPlanId.generate(),
            "Universal Plan",
            "Applies to all items",
            null, // Applies to all items
            QualitySamplingStrategy.FIXED_SIZE,
            3,
            1.0,
            requiredTests,
            "qa-manager-001"
        );
        
        assertTrue(universalPlan.isApplicableTo(applicableItem));
        assertTrue(universalPlan.isApplicableTo(new SkuCode("ANY-ITEM")));
    }

    @Test
    void shouldThrowExceptionForInvalidSampleSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            new QualitySamplingPlan(
                QualitySamplingPlanId.generate(),
                "Invalid Plan",
                "Invalid sample size",
                applicableItem,
                QualitySamplingStrategy.FIXED_SIZE,
                0, // Invalid sample size
                1.0,
                requiredTests,
                "qa-manager-001"
            );
        });
    }

    @Test
    void shouldThrowExceptionForInvalidAQL() {
        assertThrows(IllegalArgumentException.class, () -> {
            new QualitySamplingPlan(
                QualitySamplingPlanId.generate(),
                "Invalid AQL Plan",
                "Invalid AQL",
                applicableItem,
                QualitySamplingStrategy.FIXED_SIZE,
                5,
                -1.0, // Invalid AQL
                requiredTests,
                "qa-manager-001"
            );
        });
    }

    @Test
    void shouldEnsureMinimumSampleSizeOfOne() {
        QualitySamplingPlan percentagePlan = new QualitySamplingPlan(
            QualitySamplingPlanId.generate(),
            "Small Percentage Plan",
            "Very small percentage",
            null,
            QualitySamplingStrategy.PERCENTAGE,
            1, // 1% of lot size
            1.0,
            requiredTests,
            "qa-manager-001"
        );
        
        QualitySample sample = percentagePlan.createSample("BATCH-006", 10);
        
        assertEquals(1, sample.getSampleSize()); // 1% of 10 = 0.1, but minimum is 1
    }
}