package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkTest {

    private Work work;
    private WorkTemplateId templateId;
    private BinLocation location;
    private SkuCode item;
    private Quantity quantity;
    private List<WorkStep> steps;

    @BeforeEach
    void setUp() {
        templateId = WorkTemplateId.generate();
        location = new BinLocation("A", "01", "1");
        item = new SkuCode("SKU001");
        quantity = new Quantity(5);
        
        WorkStep step1 = new WorkStep(1, WorkAction.NAVIGATE_TO_LOCATION, 
                                     ValidationType.LOCATION_SCAN, 
                                     "Navigate to location", new HashMap<>());
        WorkStep step2 = new WorkStep(2, WorkAction.PICK_ITEM, 
                                     ValidationType.QUANTITY_RANGE, 
                                     "Pick item", new HashMap<>());
        
        steps = Arrays.asList(step1, step2);
        work = new Work(templateId, WorkType.PICK, location, item, quantity, steps);
    }

    @Test
    void shouldCreateWorkWithValidData() {
        assertNotNull(work.getWorkId());
        assertEquals(templateId, work.getTemplateId());
        assertEquals(location, work.getLocation());
        assertEquals(item, work.getItem());
        assertEquals(quantity, work.getQuantity());
        assertEquals(WorkStatus.CREATED, work.getStatus());
        assertEquals(0, work.getCurrentStepIndex());
        assertNotNull(work.getCreatedAt());
        assertEquals(2, work.getSteps().size());
    }

    @Test
    void shouldThrowExceptionWhenTemplateIdIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new Work(null, WorkType.PICK, location, item, quantity, steps));
    }

    @Test
    void shouldThrowExceptionWhenLocationIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new Work(templateId, WorkType.PICK, null, item, quantity, steps));
    }

    @Test
    void shouldThrowExceptionWhenItemIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new Work(templateId, WorkType.PICK, location, null, quantity, steps));
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new Work(templateId, WorkType.PICK, location, item, null, steps));
    }

    @Test
    void shouldThrowExceptionWhenStepsIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new Work(templateId, WorkType.PICK, location, item, quantity, null));
    }

    @Test
    void shouldThrowExceptionWhenStepsIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Work(templateId, WorkType.PICK, location, item, quantity, Arrays.asList()));
    }

    @Test
    void shouldAssignToWorkerSuccessfully() {
        work.assignTo("worker1");
        
        assertEquals(WorkStatus.ASSIGNED, work.getStatus());
        assertEquals("worker1", work.getAssignedTo());
        assertNotNull(work.getAssignedAt());
        assertTrue(work.isAssigned());
    }

    @Test
    void shouldHandleNullWorkerAssignment() {
        // Null assignment should not throw exception, it should handle gracefully
        assertDoesNotThrow(() -> work.assignTo(null));
        assertNull(work.getAssignedTo());
    }

    @Test
    void shouldStartWorkSuccessfully() {
        work.assignTo("worker1");
        work.start();
        
        assertEquals(WorkStatus.IN_PROGRESS, work.getStatus());
        assertNotNull(work.getStartedAt());
        assertTrue(work.isInProgress());
    }

    @Test
    void shouldThrowExceptionWhenStartingUnassignedWork() {
        assertThrows(IllegalStateException.class, () -> 
            work.start());
    }

    @Test
    void shouldGetCurrentStepCorrectly() {
        WorkStep currentStep = work.getCurrentStep();
        
        assertNotNull(currentStep);
        assertEquals(1, currentStep.getSequence());
        assertEquals(WorkAction.NAVIGATE_TO_LOCATION, currentStep.getAction());
    }

    @Test
    void shouldCompleteStepsSequentially() {
        work.assignTo("worker1");
        work.start();
        
        // Complete first step
        assertFalse(work.completeCurrentStep());
        assertEquals(1, work.getCurrentStepIndex());
        assertEquals(0.5, work.getProgress());
        assertEquals(1, work.getRemainingSteps());
        
        // Complete second step
        assertTrue(work.completeCurrentStep());
        assertEquals(WorkStatus.COMPLETED, work.getStatus());
        assertEquals(1.0, work.getProgress());
        assertEquals(0, work.getRemainingSteps());
        assertTrue(work.isComplete());
    }

    @Test
    void shouldThrowExceptionWhenCompletingStepOnNonInProgressWork() {
        assertThrows(IllegalStateException.class, () -> 
            work.completeCurrentStep());
    }

    @Test
    void shouldCompleteWorkDirectly() {
        work.assignTo("worker1");
        work.start();
        work.complete();
        
        assertEquals(WorkStatus.COMPLETED, work.getStatus());
        assertNotNull(work.getCompletedAt());
        assertTrue(work.isComplete());
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonInProgressWork() {
        assertThrows(IllegalStateException.class, () -> 
            work.complete());
    }

    @Test
    void shouldCancelWorkSuccessfully() {
        work.cancel("Test cancellation");
        
        assertEquals(WorkStatus.CANCELLED, work.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenCancellingCompletedWork() {
        work.assignTo("worker1");
        work.start();
        work.complete();
        
        assertThrows(IllegalStateException.class, () -> 
            work.cancel("Cannot cancel"));
    }

    @Test
    void shouldReleaseWorkSuccessfully() {
        work.release();
        
        assertEquals(WorkStatus.RELEASED, work.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenReleasingNonCreatedWork() {
        work.assignTo("worker1");
        
        assertThrows(IllegalStateException.class, () -> 
            work.release());
    }

    @Test
    void shouldReturnNullForCurrentStepWhenAllCompleted() {
        work.assignTo("worker1");
        work.start();
        work.completeCurrentStep();
        work.completeCurrentStep();
        
        assertNull(work.getCurrentStep());
    }

    @Test
    void shouldThrowExceptionWhenCompletingMoreStepsThanAvailable() {
        work.assignTo("worker1");
        work.start();
        work.completeCurrentStep();
        work.completeCurrentStep();
        
        assertThrows(IllegalStateException.class, () -> 
            work.completeCurrentStep());
    }

    @Test
    void shouldCalculateProgressCorrectly() {
        assertEquals(0.0, work.getProgress());
        
        work.assignTo("worker1");
        work.start();
        work.completeCurrentStep();
        
        assertEquals(0.5, work.getProgress());
        
        work.completeCurrentStep();
        
        assertEquals(1.0, work.getProgress());
    }

    @Test
    void shouldSetPriorityCorrectly() {
        assertEquals("NORMAL", work.getPriority());
        
        work.setPriority("HIGH");
        assertEquals("HIGH", work.getPriority());
    }

    @Test
    void shouldEqualWorksWithSameId() {
        Work work1 = new Work(templateId, WorkType.PICK, location, item, quantity, steps);
        Work work2 = new Work(templateId, WorkType.PICK, location, item, quantity, steps);
        
        // They should not be equal as they have different IDs
        assertNotEquals(work1, work2);
        
        // But a work should equal itself
        assertEquals(work1, work1);
    }

    @Test
    void shouldReturnCorrectStringRepresentation() {
        work.assignTo("worker1");
        String workString = work.toString();
        
        assertTrue(workString.contains("Work{"));
        assertTrue(workString.contains("workId="));
        assertTrue(workString.contains("status=ASSIGNED"));
        assertTrue(workString.contains("assignedTo='worker1'"));
    }
}