package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QualityInspectionTest {

    private QualityInspection inspection;
    private QualityInspectionId inspectionId;
    private SkuCode item;

    @BeforeEach
    void setUp() {
        inspectionId = QualityInspectionId.generate();
        item = new SkuCode("TEST-ITEM-001");
        
        inspection = new QualityInspection(
            inspectionId,
            QualityInspectionType.RECEIVING_INSPECTION,
            item,
            "BATCH-001",
            100,
            "inspector-001"
        );
    }

    @Test
    void shouldCreateInspectionWithCorrectInitialState() {
        assertEquals(inspectionId, inspection.getInspectionId());
        assertEquals(QualityInspectionType.RECEIVING_INSPECTION, inspection.getInspectionType());
        assertEquals(item, inspection.getItem());
        assertEquals("BATCH-001", inspection.getBatchNumber());
        assertEquals(100, inspection.getQuantity());
        assertEquals("inspector-001", inspection.getInspectorId());
        assertEquals(QualityInspectionStatus.SCHEDULED, inspection.getStatus());
        assertNotNull(inspection.getScheduledAt());
        assertTrue(inspection.getSteps().isEmpty());
        assertNull(inspection.getFinalDecision());
    }

    @Test
    void shouldStartInspectionSuccessfully() {
        inspection.start("inspector-001");
        
        assertEquals(QualityInspectionStatus.IN_PROGRESS, inspection.getStatus());
        assertNotNull(inspection.getStartedAt());
        assertEquals("inspector-001", inspection.getInspectorId());
    }

    @Test
    void shouldNotStartInspectionIfNotScheduled() {
        inspection.start("inspector-001");
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            inspection.start("inspector-002");
        });
        
        assertTrue(exception.getMessage().contains("Cannot start inspection"));
    }

    @Test
    void shouldAddStepToInspection() {
        inspection.start("inspector-001");
        
        QualityStepTemplate stepTemplate = new QualityStepTemplate(
            1, "Visual Inspection", "Check for visual defects",
            QualityTestType.VISUAL_INSPECTION, true,
            "PASS", null, null
        );
        
        inspection.addStep(stepTemplate);
        
        assertEquals(1, inspection.getSteps().size());
        QualityInspectionStep step = inspection.getSteps().get(0);
        assertEquals(1, step.getSequence());
        assertEquals("Visual Inspection", step.getName());
        assertFalse(step.isCompleted());
    }

    @Test
    void shouldCompleteStepWithTestResult() {
        inspection.start("inspector-001");
        
        QualityStepTemplate stepTemplate = new QualityStepTemplate(
            1, "Weight Check", "Check weight measurement",
            QualityTestType.WEIGHT, true,
            "100", new QualityToleranceRange("95", "105", 5.0), "g"
        );
        
        inspection.addStep(stepTemplate);
        
        QualityTestResult testResult = QualityTestResult.measurement(
            "102", "g", true, QualityTestType.WEIGHT, "inspector-001"
        );
        
        inspection.completeStep(1, testResult, "Within tolerance");
        
        QualityInspectionStep step = inspection.getSteps().get(0);
        assertTrue(step.isCompleted());
        assertEquals(testResult, step.getTestResult());
        assertEquals("Within tolerance", step.getNotes());
    }

    @Test
    void shouldCompleteInspectionWhenAllStepsComplete() {
        inspection.start("inspector-001");
        
        // Add and complete a step
        QualityStepTemplate stepTemplate = new QualityStepTemplate(
            1, "Visual Check", "Visual inspection",
            QualityTestType.VISUAL_INSPECTION, true,
            "PASS", null, null
        );
        
        inspection.addStep(stepTemplate);
        
        QualityTestResult testResult = QualityTestResult.pass(
            "PASS", QualityTestType.VISUAL_INSPECTION, "inspector-001"
        );
        
        inspection.completeStep(1, testResult, "All good");
        
        // Complete the inspection
        inspection.complete(QualityDecision.APPROVED, "supervisor-001", false);
        
        assertEquals(QualityInspectionStatus.COMPLETED, inspection.getStatus());
        assertEquals(QualityDecision.APPROVED, inspection.getFinalDecision());
        assertEquals("supervisor-001", inspection.getSupervisorId());
        assertNotNull(inspection.getCompletedAt());
        assertFalse(inspection.hasNonConformances());
    }

    @Test
    void shouldPutInspectionOnHold() {
        inspection.start("inspector-001");
        
        inspection.hold(QualityHoldReason.EQUIPMENT_MALFUNCTION, 
                       "Scale not working", "inspector-001");
        
        assertEquals(QualityInspectionStatus.ON_HOLD, inspection.getStatus());
    }

    @Test
    void shouldReleaseInspectionFromHold() {
        inspection.start("inspector-001");
        inspection.hold(QualityHoldReason.EQUIPMENT_MALFUNCTION, 
                       "Scale not working", "inspector-001");
        
        inspection.release("Scale fixed", "inspector-001");
        
        assertEquals(QualityInspectionStatus.IN_PROGRESS, inspection.getStatus());
    }

    @Test
    void shouldCancelInspection() {
        inspection.cancel("Item no longer needed", "supervisor-001");
        
        assertEquals(QualityInspectionStatus.CANCELLED, inspection.getStatus());
    }

    @Test
    void shouldAddNonConformance() {
        inspection.start("inspector-001");
        
        inspection.addNonConformance(QualityNonConformanceType.APPEARANCE_DEFECT,
                                   "Surface scratches found",
                                   QualitySeverity.MEDIUM, 
                                   "inspector-001");
        
        assertTrue(inspection.hasNonConformances());
        assertEquals(1, inspection.getNonConformanceIds().size());
    }

    @Test
    void shouldAssignSupervisor() {
        inspection.assignSupervisor("supervisor-001");
        
        assertEquals("supervisor-001", inspection.getSupervisorId());
    }

    @Test
    void shouldRescheduleInspection() {
        Instant newDate = Instant.now().plusSeconds(3600);
        inspection.reschedule(newDate);
        
        assertEquals(newDate, inspection.getScheduledDate());
    }
}