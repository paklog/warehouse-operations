package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QualitySampleTest {

    private QualitySample sample;
    private QualitySampleId sampleId;
    private QualitySamplingPlanId planId;
    private SkuCode item;
    private List<QualityTestType> requiredTests;

    @BeforeEach
    void setUp() {
        sampleId = QualitySampleId.generate();
        planId = QualitySamplingPlanId.generate();
        item = new SkuCode("TEST-ITEM-001");
        requiredTests = Arrays.asList(
            QualityTestType.VISUAL_INSPECTION,
            QualityTestType.WEIGHT
        );
        
        sample = new QualitySample(
            sampleId,
            planId,
            "BATCH-001",
            item,
            5,
            requiredTests
        );
    }

    @Test
    void shouldCreateSampleWithCorrectInitialState() {
        assertEquals(sampleId, sample.getSampleId());
        assertEquals(planId, sample.getSamplingPlanId());
        assertEquals("BATCH-001", sample.getBatchNumber());
        assertEquals(item, sample.getItem());
        assertEquals(5, sample.getSampleSize());
        assertEquals(requiredTests, sample.getRequiredTests());
        assertEquals(QualitySampleStatus.COLLECTED, sample.getStatus());
        assertNotNull(sample.getCollectedAt());
        assertTrue(sample.getTestResults().isEmpty());
        assertNull(sample.getVerdict());
        assertFalse(sample.isComplete());
    }

    @Test
    void shouldStartTestingSuccessfully() {
        sample.startTesting();
        
        assertEquals(QualitySampleStatus.TESTING_IN_PROGRESS, sample.getStatus());
    }

    @Test
    void shouldNotStartTestingIfNotCollected() {
        sample.startTesting();
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            sample.startTesting();
        });
        
        assertTrue(exception.getMessage().contains("Cannot start testing"));
    }

    @Test
    void shouldAddTestResultSuccessfully() {
        sample.startTesting();
        
        QualityTestResult testResult = QualityTestResult.pass(
            "PASS", QualityTestType.VISUAL_INSPECTION, "tester-001"
        );
        
        sample.addTestResult(testResult);
        
        assertEquals(1, sample.getTestResults().size());
        assertEquals(testResult, sample.getTestResults().get(0));
    }

    @Test
    void shouldNotAddTestResultForNonRequiredTest() {
        sample.startTesting();
        
        QualityTestResult testResult = QualityTestResult.pass(
            "PASS", QualityTestType.TEMPERATURE, "tester-001"
        );
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sample.addTestResult(testResult);
        });
        
        assertTrue(exception.getMessage().contains("Test type"));
        assertTrue(exception.getMessage().contains("is not required"));
    }

    @Test
    void shouldNotAddTestResultIfNotInProgress() {
        QualityTestResult testResult = QualityTestResult.pass(
            "PASS", QualityTestType.VISUAL_INSPECTION, "tester-001"
        );
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            sample.addTestResult(testResult);
        });
        
        assertTrue(exception.getMessage().contains("Cannot add test result"));
    }

    @Test
    void shouldCompleteTestingWhenAllTestsAdded() {
        sample.startTesting();
        
        // Add first test result
        QualityTestResult visualResult = QualityTestResult.pass(
            "PASS", QualityTestType.VISUAL_INSPECTION, "tester-001"
        );
        sample.addTestResult(visualResult);
        
        // Status should still be in progress
        assertEquals(QualitySampleStatus.TESTING_IN_PROGRESS, sample.getStatus());
        
        // Add second test result - should automatically complete
        QualityTestResult weightResult = QualityTestResult.measurement(
            "102", "g", true, QualityTestType.WEIGHT, "tester-001"
        );
        sample.addTestResult(weightResult);
        
        assertEquals(QualitySampleStatus.TESTING_COMPLETED, sample.getStatus());
        assertEquals(QualitySampleVerdict.ACCEPTED, sample.getVerdict());
        assertTrue(sample.isComplete());
    }

    @Test
    void shouldRejectSampleWhenTestsFail() {
        sample.startTesting();
        
        // Add passing visual test
        QualityTestResult visualResult = QualityTestResult.pass(
            "PASS", QualityTestType.VISUAL_INSPECTION, "tester-001"
        );
        sample.addTestResult(visualResult);
        
        // Add failing weight test
        QualityTestResult weightResult = QualityTestResult.measurement(
            "80", "g", false, QualityTestType.WEIGHT, "tester-001"
        );
        sample.addTestResult(weightResult);
        
        assertEquals(QualitySampleStatus.TESTING_COMPLETED, sample.getStatus());
        assertEquals(QualitySampleVerdict.REJECTED, sample.getVerdict());
        assertTrue(sample.isComplete());
    }

    @Test
    void shouldRejectSampleManually() {
        sample.reject("Sample contaminated during collection");
        
        assertEquals(QualitySampleStatus.REJECTED, sample.getStatus());
        assertEquals(QualitySampleVerdict.REJECTED, sample.getVerdict());
        assertEquals("Sample contaminated during collection", sample.getNotes());
        assertTrue(sample.isComplete());
    }

    @Test
    void shouldNotCompleteTestingIfNotAllTestsComplete() {
        sample.startTesting();
        
        // Add only one test result
        QualityTestResult visualResult = QualityTestResult.pass(
            "PASS", QualityTestType.VISUAL_INSPECTION, "tester-001"
        );
        sample.addTestResult(visualResult);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            sample.completeTesting();
        });
        
        assertTrue(exception.getMessage().contains("not all required tests are complete"));
    }

    @Test
    void shouldNotCompleteTestingIfNotInProgress() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            sample.completeTesting();
        });
        
        assertTrue(exception.getMessage().contains("Cannot complete testing"));
    }
}