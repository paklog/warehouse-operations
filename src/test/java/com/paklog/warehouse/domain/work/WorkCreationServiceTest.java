package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkCreationServiceTest {

    @Mock
    private WorkTemplateRepository workTemplateRepository;

    @Mock
    private WorkRepository workRepository;

    private WorkCreationService workCreationService;
    private WorkTemplate mockTemplate;
    private BinLocation location;
    private SkuCode item;
    private Quantity quantity;

    @BeforeEach
    void setUp() {
        workCreationService = new WorkCreationService(workTemplateRepository, workRepository);
        
        location = new BinLocation("A", "01", "1");
        item = new SkuCode("SKU001");
        quantity = new Quantity(5);
        
        // Create a mock template
        mockTemplate = new WorkTemplate("Pick Template", "Standard pick", WorkType.PICK);
        WorkStep step = new WorkStep(1, WorkAction.PICK_ITEM, ValidationType.QUANTITY_RANGE, 
                                    "Pick item", new HashMap<>());
        mockTemplate.addStep(step);
    }

    @Test
    void shouldCreateWorkSuccessfully() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(mockTemplate));
        when(workRepository.save(any(Work.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Work createdWork = workCreationService.createWork(WorkType.PICK, location, item, quantity, "worker1");

        // Assert
        assertNotNull(createdWork);
        assertEquals(location, createdWork.getLocation());
        assertEquals(item, createdWork.getItem());
        assertEquals(quantity, createdWork.getQuantity());
        assertEquals("worker1", createdWork.getAssignedTo());
        assertEquals(WorkStatus.ASSIGNED, createdWork.getStatus());
        
        verify(workTemplateRepository).findByWorkTypeAndActive(WorkType.PICK, true);
        verify(workRepository).save(any(Work.class));
    }

    @Test
    void shouldThrowExceptionWhenNoTemplateFound() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            workCreationService.createWork(WorkType.PICK, location, item, quantity, "worker1"));
        
        verify(workTemplateRepository).findByWorkTypeAndActive(WorkType.PICK, true);
        verify(workRepository, never()).save(any(Work.class));
    }

    @Test
    void shouldCreateWorkFromSpecificTemplate() {
        // Arrange
        WorkTemplateId templateId = mockTemplate.getId();
        when(workTemplateRepository.findById(templateId))
            .thenReturn(Optional.of(mockTemplate));
        when(workRepository.save(any(Work.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Work createdWork = workCreationService.createWorkFromTemplate(
            templateId, location, item, quantity, "worker1");

        // Assert
        assertNotNull(createdWork);
        assertEquals(templateId, createdWork.getTemplateId());
        assertEquals("worker1", createdWork.getAssignedTo());
        
        verify(workTemplateRepository).findById(templateId);
        verify(workRepository).save(any(Work.class));
    }

    @Test
    void shouldThrowExceptionWhenTemplateNotFoundById() {
        // Arrange
        WorkTemplateId templateId = WorkTemplateId.generate();
        when(workTemplateRepository.findById(templateId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            workCreationService.createWorkFromTemplate(templateId, location, item, quantity, "worker1"));
        
        verify(workTemplateRepository).findById(templateId);
        verify(workRepository, never()).save(any(Work.class));
    }

    @Test
    void shouldThrowExceptionWhenTemplateIsInactive() {
        // Arrange
        mockTemplate.deactivate();
        WorkTemplateId templateId = mockTemplate.getId();
        when(workTemplateRepository.findById(templateId))
            .thenReturn(Optional.of(mockTemplate));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            workCreationService.createWorkFromTemplate(templateId, location, item, quantity, "worker1"));
        
        verify(workTemplateRepository).findById(templateId);
        verify(workRepository, never()).save(any(Work.class));
    }

    @Test
    void shouldCreateBatchWorkSuccessfully() {
        // Arrange
        WorkRequest request1 = new WorkRequest(location, item, quantity, "worker1");
        WorkRequest request2 = new WorkRequest(
            new BinLocation("B", "02", "1"), 
            new SkuCode("SKU002"), 
            new Quantity(3), 
            "worker2"
        );
        List<WorkRequest> requests = Arrays.asList(request1, request2);
        
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(mockTemplate));
        when(workRepository.save(any(Work.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<Work> createdWork = workCreationService.createBatchWork(WorkType.PICK, requests);

        // Assert
        assertEquals(2, createdWork.size());
        assertEquals("worker1", createdWork.get(0).getAssignedTo());
        assertEquals("worker2", createdWork.get(1).getAssignedTo());
        
        verify(workTemplateRepository).findByWorkTypeAndActive(WorkType.PICK, true);
        verify(workRepository, times(2)).save(any(Work.class));
    }

    @Test
    void shouldReturnTrueWhenCanCreateWork() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(mockTemplate));

        // Act
        boolean canCreate = workCreationService.canCreateWork(WorkType.PICK);

        // Assert
        assertTrue(canCreate);
        verify(workTemplateRepository).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldReturnFalseWhenCannotCreateWork() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList());

        // Act
        boolean canCreate = workCreationService.canCreateWork(WorkType.PICK);

        // Assert
        assertFalse(canCreate);
        verify(workTemplateRepository).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldGetAvailableTemplates() {
        // Arrange
        List<WorkTemplate> expectedTemplates = Arrays.asList(mockTemplate);
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(expectedTemplates);

        // Act
        List<WorkTemplate> availableTemplates = workCreationService.getAvailableTemplates(WorkType.PICK);

        // Assert
        assertEquals(expectedTemplates, availableTemplates);
        verify(workTemplateRepository).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldValidateWorkCreationSuccessfully() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(mockTemplate));
        when(workRepository.findActiveWork())
            .thenReturn(Arrays.asList());

        // Act
        WorkCreationService.WorkCreationResult result = workCreationService.validateWorkCreation(
            WorkType.PICK, location, item, quantity);

        // Assert
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
    }

    @Test
    void shouldFailValidationWhenNoTemplatesAvailable() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList());

        // Act
        WorkCreationService.WorkCreationResult result = workCreationService.validateWorkCreation(
            WorkType.PICK, location, item, quantity);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("No active templates available"));
    }

    @Test
    void shouldFailValidationWithInvalidLocation() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(mockTemplate));

        // Act
        WorkCreationService.WorkCreationResult result = workCreationService.validateWorkCreation(
            WorkType.PICK, null, item, quantity);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Invalid location"));
    }

    @Test
    void shouldFailValidationWithInvalidItem() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(mockTemplate));

        // Act
        WorkCreationService.WorkCreationResult result = workCreationService.validateWorkCreation(
            WorkType.PICK, location, null, quantity);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Invalid item"));
    }

    @Test
    void shouldFailValidationWithInvalidQuantity() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(mockTemplate));

        // Act
        WorkCreationService.WorkCreationResult result = workCreationService.validateWorkCreation(
            WorkType.PICK, location, item, new Quantity(0));

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Invalid quantity"));
    }

    @Test
    void shouldFailValidationWhenDuplicateWorkExists() {
        // Arrange
        when(workTemplateRepository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(mockTemplate));
        
        Work existingWork = mock(Work.class);
        when(existingWork.getLocation()).thenReturn(location);
        when(existingWork.getItem()).thenReturn(item);
        when(existingWork.isInProgress()).thenReturn(true);
        lenient().when(existingWork.isAssigned()).thenReturn(false);
        
        when(workRepository.findActiveWork())
            .thenReturn(Arrays.asList(existingWork));

        // Act
        WorkCreationService.WorkCreationResult result = workCreationService.validateWorkCreation(
            WorkType.PICK, location, item, quantity);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Similar work already exists"));
    }

    @Test
    void shouldThrowExceptionWhenWorkTemplateRepositoryIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new WorkCreationService(null, workRepository));
    }

    @Test
    void shouldThrowExceptionWhenWorkRepositoryIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new WorkCreationService(workTemplateRepository, null));
    }
}