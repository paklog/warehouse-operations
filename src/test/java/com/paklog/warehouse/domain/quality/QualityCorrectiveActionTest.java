package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QualityCorrectiveActionTest {

    private QualityCorrectiveAction correctiveAction;
    private QualityCorrectiveActionId actionId;
    private QualityNonConformanceId nonConformanceId;
    private SkuCode affectedItem;

    @BeforeEach
    void setUp() {
        actionId = QualityCorrectiveActionId.generate();
        nonConformanceId = QualityNonConformanceId.generate();
        affectedItem = new SkuCode("TEST-ITEM-001");
        
        correctiveAction = new QualityCorrectiveAction(
            actionId,
            nonConformanceId,
            affectedItem,
            "Implement improved inspection process",
            QualityCorrectiveActionType.CORRECTIVE_ACTION,
            QualityCorrectiveActionPriority.HIGH,
            "qa-manager-001",
            30 // 30 days due date
        );
    }

    @Test
    void shouldCreateCorrectiveActionWithCorrectInitialState() {
        assertEquals(actionId, correctiveAction.getActionId());
        assertEquals(nonConformanceId, correctiveAction.getNonConformanceId());
        assertEquals(affectedItem, correctiveAction.getAffectedItem());
        assertEquals("Implement improved inspection process", correctiveAction.getDescription());
        assertEquals(QualityCorrectiveActionType.CORRECTIVE_ACTION, correctiveAction.getActionType());
        assertEquals(QualityCorrectiveActionPriority.HIGH, correctiveAction.getPriority());
        assertEquals("qa-manager-001", correctiveAction.getAssignedTo());
        assertEquals(QualityCorrectiveActionStatus.ASSIGNED, correctiveAction.getStatus());
        assertNotNull(correctiveAction.getAssignedAt());
        assertNotNull(correctiveAction.getDueDate());
        assertTrue(correctiveAction.getSteps().isEmpty());
        assertNull(correctiveAction.getCompletedBy());
        assertNull(correctiveAction.getEffectiveness());
    }

    @Test
    void shouldStartImplementationSuccessfully() {
        correctiveAction.startImplementation();
        
        assertEquals(QualityCorrectiveActionStatus.IN_PROGRESS, correctiveAction.getStatus());
    }

    @Test
    void shouldNotStartImplementationIfNotAssigned() {
        correctiveAction.startImplementation();
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            correctiveAction.startImplementation();
        });
        
        assertTrue(exception.getMessage().contains("Cannot start implementation"));
    }

    @Test
    void shouldAddStepSuccessfully() {
        correctiveAction.addStep("Review current inspection procedures", "qa-analyst-001", 7);
        
        assertEquals(1, correctiveAction.getSteps().size());
        QualityCorrectiveAction.QualityCorrectiveActionStep step = correctiveAction.getSteps().get(0);
        assertEquals(1, step.getStepNumber());
        assertEquals("Review current inspection procedures", step.getDescription());
        assertEquals("qa-analyst-001", step.getAssignedTo());
        assertFalse(step.isCompleted());
        assertNotNull(step.getDueDate());
    }

    @Test
    void shouldNotAddStepWhenCompleted() {
        correctiveAction.startImplementation();
        correctiveAction.completeAction("qa-manager-001", "All tasks completed");
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            correctiveAction.addStep("New step", "qa-analyst-001", 7);
        });
        
        assertTrue(exception.getMessage().contains("Cannot add step"));
    }

    @Test
    void shouldCompleteStepSuccessfully() {
        correctiveAction.addStep("Review procedures", "qa-analyst-001", 7);
        
        correctiveAction.completeStep(1, "qa-analyst-001", "Review completed successfully");
        
        QualityCorrectiveAction.QualityCorrectiveActionStep step = correctiveAction.getSteps().get(0);
        assertTrue(step.isCompleted());
        assertEquals("qa-analyst-001", step.getCompletedBy());
        assertEquals("Review completed successfully", step.getNotes());
        assertNotNull(step.getCompletedAt());
    }

    @Test
    void shouldNotCompleteAlreadyCompletedStep() {
        correctiveAction.addStep("Review procedures", "qa-analyst-001", 7);
        correctiveAction.completeStep(1, "qa-analyst-001", "First completion");
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            correctiveAction.completeStep(1, "qa-analyst-001", "Second completion");
        });
        
        assertTrue(exception.getMessage().contains("Step 1 is already completed"));
    }

    @Test
    void shouldCompleteActionWhenAllStepsComplete() {
        correctiveAction.startImplementation();
        correctiveAction.addStep("Step 1", "qa-analyst-001", 7);
        correctiveAction.addStep("Step 2", "qa-analyst-002", 7);
        
        correctiveAction.completeStep(1, "qa-analyst-001", "Step 1 done");
        correctiveAction.completeStep(2, "qa-analyst-002", "Step 2 done");
        
        assertEquals(QualityCorrectiveActionStatus.COMPLETED, correctiveAction.getStatus());
        assertEquals("qa-analyst-002", correctiveAction.getCompletedBy());
        assertNotNull(correctiveAction.getCompletedAt());
    }

    @Test
    void shouldCompleteActionDirectly() {
        correctiveAction.startImplementation();
        
        correctiveAction.completeAction("qa-manager-001", "Simple action completed directly");
        
        assertEquals(QualityCorrectiveActionStatus.COMPLETED, correctiveAction.getStatus());
        assertEquals("qa-manager-001", correctiveAction.getCompletedBy());
        assertEquals("Simple action completed directly", correctiveAction.getCompletionNotes());
        assertNotNull(correctiveAction.getCompletedAt());
    }

    @Test
    void shouldNotCompleteActionIfNotInProgress() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            correctiveAction.completeAction("qa-manager-001", "Trying to complete");
        });
        
        assertTrue(exception.getMessage().contains("Cannot complete action"));
    }

    @Test
    void shouldVerifyEffectivenessSuccessfully() {
        correctiveAction.startImplementation();
        correctiveAction.completeAction("qa-manager-001", "Action completed");
        
        correctiveAction.verifyEffectiveness(
            QualityCorrectiveActionEffectiveness.EFFECTIVE, 
            "qa-director-001", 
            "No recurrence observed after 3 months"
        );
        
        assertEquals(QualityCorrectiveActionEffectiveness.EFFECTIVE, correctiveAction.getEffectiveness());
    }

    @Test
    void shouldNotVerifyEffectivenessIfNotCompleted() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            correctiveAction.verifyEffectiveness(
                QualityCorrectiveActionEffectiveness.EFFECTIVE, 
                "qa-director-001", 
                "Verification notes"
            );
        });
        
        assertTrue(exception.getMessage().contains("Cannot verify effectiveness"));
    }

    @Test
    void shouldCancelActionSuccessfully() {
        correctiveAction.cancel("qa-director-001", "Higher priority actions take precedence");
        
        assertEquals(QualityCorrectiveActionStatus.CANCELLED, correctiveAction.getStatus());
    }

    @Test
    void shouldNotCancelCompletedAction() {
        correctiveAction.startImplementation();
        correctiveAction.completeAction("qa-manager-001", "Completed");
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            correctiveAction.cancel("qa-director-001", "Trying to cancel");
        });
        
        assertTrue(exception.getMessage().contains("Cannot cancel completed action"));
    }

    @Test
    void shouldDetectOverdueAction() {
        // Create action with very short due date
        QualityCorrectiveAction shortAction = new QualityCorrectiveAction(
            QualityCorrectiveActionId.generate(),
            nonConformanceId,
            affectedItem,
            "Urgent action",
            QualityCorrectiveActionType.IMMEDIATE_CONTAINMENT,
            QualityCorrectiveActionPriority.CRITICAL,
            "qa-manager-001",
            -1 // Already overdue
        );
        
        assertTrue(shortAction.isOverdue());
    }

    @Test
    void shouldNotBeOverdueWhenCompleted() {
        QualityCorrectiveAction shortAction = new QualityCorrectiveAction(
            QualityCorrectiveActionId.generate(),
            nonConformanceId,
            affectedItem,
            "Urgent action",
            QualityCorrectiveActionType.IMMEDIATE_CONTAINMENT,
            QualityCorrectiveActionPriority.CRITICAL,
            "qa-manager-001",
            -1 // Already overdue
        );
        
        shortAction.startImplementation();
        shortAction.completeAction("qa-manager-001", "Completed on time");
        
        assertFalse(shortAction.isOverdue());
    }
}