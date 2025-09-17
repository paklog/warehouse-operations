package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkTemplateTest {

    private WorkTemplate workTemplate;
    private WorkStep pickStep;
    private WorkStep scanStep;

    @BeforeEach
    void setUp() {
        workTemplate = new WorkTemplate("Pick Template", "Standard picking template", WorkType.PICK);
        
        Map<String, Object> pickParams = new HashMap<>();
        pickParams.put("timeout", 300);
        pickStep = new WorkStep(1, WorkAction.PICK_ITEM, ValidationType.QUANTITY_RANGE, 
                               "Pick specified quantity", pickParams);
        
        Map<String, Object> scanParams = new HashMap<>();
        scanParams.put("required", true);
        scanStep = new WorkStep(2, WorkAction.SCAN_ITEM, ValidationType.BARCODE_SCAN, 
                               "Scan item barcode", scanParams);
    }

    @Test
    void shouldCreateWorkTemplateWithValidData() {
        assertNotNull(workTemplate.getId());
        assertEquals("Pick Template", workTemplate.getName());
        assertEquals("Standard picking template", workTemplate.getDescription());
        assertEquals(WorkType.PICK, workTemplate.getWorkType());
        assertTrue(workTemplate.isActive());
        assertNotNull(workTemplate.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new WorkTemplate(null, "Description", WorkType.PICK));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> 
            new WorkTemplate("", "Description", WorkType.PICK));
    }

    @Test
    void shouldThrowExceptionWhenWorkTypeIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new WorkTemplate("Name", "Description", null));
    }

    @Test
    void shouldAddStepSuccessfully() {
        workTemplate.addStep(pickStep);
        
        assertEquals(1, workTemplate.getStepCount());
        assertEquals(pickStep, workTemplate.getSteps().get(0));
    }

    @Test
    void shouldAddMultipleStepsInOrder() {
        workTemplate.addStep(scanStep);
        workTemplate.addStep(pickStep);
        
        assertEquals(2, workTemplate.getStepCount());
        assertEquals(pickStep, workTemplate.getSteps().get(0)); // Sequence 1
        assertEquals(scanStep, workTemplate.getSteps().get(1)); // Sequence 2
    }

    @Test
    void shouldThrowExceptionWhenAddingStepWithDuplicateSequence() {
        workTemplate.addStep(pickStep);
        
        WorkStep duplicateStep = new WorkStep(1, WorkAction.SCAN_LOCATION, 
                                             ValidationType.LOCATION_SCAN, 
                                             "Another step", new HashMap<>());
        
        assertThrows(IllegalArgumentException.class, () -> 
            workTemplate.addStep(duplicateStep));
    }

    @Test
    void shouldRemoveStepSuccessfully() {
        workTemplate.addStep(pickStep);
        workTemplate.addStep(scanStep);
        
        workTemplate.removeStep(1);
        
        assertEquals(1, workTemplate.getStepCount());
        assertEquals(scanStep, workTemplate.getSteps().get(0));
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentStep() {
        assertThrows(IllegalArgumentException.class, () -> 
            workTemplate.removeStep(99));
    }

    @Test
    void shouldUpdateStepSuccessfully() {
        workTemplate.addStep(pickStep);
        
        Map<String, Object> newParams = new HashMap<>();
        newParams.put("timeout", 600);
        WorkStep updatedStep = new WorkStep(1, WorkAction.PICK_ITEM, ValidationType.NUMERIC_INPUT, 
                                           "Updated pick step", newParams);
        
        workTemplate.updateStep(updatedStep);
        
        assertEquals(1, workTemplate.getStepCount());
        WorkStep retrievedStep = workTemplate.getSteps().get(0);
        assertEquals("Updated pick step", retrievedStep.getDescription());
        assertEquals(ValidationType.NUMERIC_INPUT, retrievedStep.getValidation());
    }

    @Test
    void shouldActivateAndDeactivateTemplate() {
        assertTrue(workTemplate.isActive());
        
        workTemplate.deactivate();
        assertFalse(workTemplate.isActive());
        
        workTemplate.activate();
        assertTrue(workTemplate.isActive());
    }

    @Test
    void shouldUpdateNameSuccessfully() {
        workTemplate.updateName("New Template Name");
        assertEquals("New Template Name", workTemplate.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToNull() {
        assertThrows(NullPointerException.class, () -> 
            workTemplate.updateName(null));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToEmpty() {
        assertThrows(IllegalArgumentException.class, () -> 
            workTemplate.updateName(""));
    }

    @Test
    void shouldUpdateDescriptionSuccessfully() {
        workTemplate.updateDescription("New description");
        assertEquals("New description", workTemplate.getDescription());
    }

    @Test
    void shouldGenerateWorkSuccessfully() {
        workTemplate.addStep(pickStep);
        workTemplate.addStep(scanStep);
        
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        WorkRequest request = new WorkRequest(location, item, quantity, "worker1");
        
        Work work = workTemplate.generateWork(request);
        
        assertNotNull(work);
        assertEquals(workTemplate.getId(), work.getTemplateId());
        assertEquals(location, work.getLocation());
        assertEquals(item, work.getItem());
        assertEquals(quantity, work.getQuantity());
        assertEquals(2, work.getSteps().size());
    }

    @Test
    void shouldThrowExceptionWhenGeneratingWorkFromInactiveTemplate() {
        workTemplate.addStep(pickStep);
        workTemplate.deactivate();
        
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        WorkRequest request = new WorkRequest(location, item, quantity, "worker1");
        
        assertThrows(IllegalStateException.class, () -> 
            workTemplate.generateWork(request));
    }

    @Test
    void shouldThrowExceptionWhenGeneratingWorkWithNoSteps() {
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        WorkRequest request = new WorkRequest(location, item, quantity, "worker1");
        
        assertThrows(IllegalStateException.class, () -> 
            workTemplate.generateWork(request));
    }

    @Test
    void shouldValidateTemplateWithValidSteps() {
        workTemplate.addStep(pickStep);
        workTemplate.addStep(scanStep);
        
        assertTrue(workTemplate.isValid());
    }

    @Test
    void shouldInvalidateTemplateWithNoSteps() {
        assertFalse(workTemplate.isValid());
    }

    @Test
    void shouldInvalidateTemplateWithSequenceGaps() {
        WorkStep step1 = new WorkStep(1, WorkAction.PICK_ITEM, ValidationType.NONE, 
                                     "Step 1", new HashMap<>());
        WorkStep step3 = new WorkStep(3, WorkAction.SCAN_ITEM, ValidationType.BARCODE_SCAN, 
                                     "Step 3", new HashMap<>());
        
        workTemplate.addStep(step1);
        workTemplate.addStep(step3);
        
        assertFalse(workTemplate.isValid());
    }

    @Test
    void shouldValidatePickTemplateSteps() {
        WorkStep validPickStep = new WorkStep(1, WorkAction.NAVIGATE_TO_LOCATION, 
                                             ValidationType.LOCATION_SCAN, 
                                             "Navigate to location", new HashMap<>());
        WorkStep invalidPickStep = new WorkStep(2, WorkAction.PUT_ITEM, 
                                               ValidationType.NONE, 
                                               "Put item (invalid for pick)", new HashMap<>());
        
        workTemplate.addStep(validPickStep);
        assertTrue(workTemplate.isValid());
        
        workTemplate.addStep(invalidPickStep);
        assertFalse(workTemplate.isValid());
    }

    @Test
    void shouldEqualTemplatesWithSameId() {
        WorkTemplate template1 = new WorkTemplate("Template 1", "Description", WorkType.PICK);
        WorkTemplate template2 = new WorkTemplate(template1.getId(), "Template 2", "Different", 
                                                 WorkType.PUT, new java.util.ArrayList<>(), 
                                                 true, false, null, java.time.Instant.now(), 
                                                 java.time.Instant.now(), "user", 1);
        
        assertEquals(template1, template2);
        assertEquals(template1.hashCode(), template2.hashCode());
    }

    @Test
    void shouldNotEqualTemplatesWithDifferentIds() {
        WorkTemplate template1 = new WorkTemplate("Template 1", "Description", WorkType.PICK);
        WorkTemplate template2 = new WorkTemplate("Template 2", "Description", WorkType.PICK);
        
        assertNotEquals(template1, template2);
    }
}