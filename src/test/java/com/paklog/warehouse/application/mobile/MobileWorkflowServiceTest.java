package com.paklog.warehouse.application.mobile;

import com.paklog.warehouse.application.mobile.barcode.BarcodeScanProcessor;
import com.paklog.warehouse.application.mobile.dto.*;
import com.paklog.warehouse.domain.location.LocationDirectiveService;
import com.paklog.warehouse.domain.location.LocationQuery;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.Priority;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.work.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobileWorkflowServiceTest {

    @Mock
    private WorkRepository workRepository;
    
    @Mock
    private BarcodeScanProcessor barcodeScanProcessor;
    
    @Mock
    private LocationDirectiveService locationDirectiveService;
    
    @Mock
    private MobileNotificationService notificationService;

    private MobileWorkflowService service;
    private Work testWork;
    private WorkId testWorkId;

    @BeforeEach
    void setUp() {
        service = new MobileWorkflowService(workRepository, barcodeScanProcessor, 
                                          locationDirectiveService, notificationService);
        
        testWorkId = new WorkId("WORK-001");
        testWork = createTestWork(testWorkId, WorkStatus.ASSIGNED);
    }

    @Test
    void shouldGetAssignedWorkForWorker() {
        // Arrange
        String workerId = "WORKER-001";
        List<Work> assignedWork = Arrays.asList(testWork);
        
        when(workRepository.findByAssignedToAndStatusIn(eq(workerId), 
            eq(Arrays.asList(WorkStatus.ASSIGNED, WorkStatus.IN_PROGRESS))))
            .thenReturn(assignedWork);

        // Act
        List<MobileWorkSummaryDto> result = service.getAssignedWork(workerId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testWorkId.getValue(), result.get(0).getWorkId());
        assertEquals(workerId, result.get(0).getAssignedTo());
        verify(workRepository).findByAssignedToAndStatusIn(workerId, 
            Arrays.asList(WorkStatus.ASSIGNED, WorkStatus.IN_PROGRESS));
    }

    @Test
    void shouldGetAvailableWorkByType() {
        // Arrange
        String workerId = "WORKER-001";
        WorkType workType = WorkType.PICK;
        int limit = 5;
        
        Work availableWork = createTestWork(new WorkId("WORK-002"), WorkStatus.RELEASED);
        availableWork.assignTo(null); // Unassigned
        
        when(workRepository.findByWorkTypeAndStatus(workType, WorkStatus.RELEASED))
            .thenReturn(Arrays.asList(availableWork));

        // Act
        List<MobileWorkSummaryDto> result = service.getAvailableWork(workerId, workType, limit);

        // Assert
        assertEquals(1, result.size());
        assertNull(result.get(0).getAssignedTo());
        verify(workRepository).findByWorkTypeAndStatus(workType, WorkStatus.RELEASED);
    }

    @Test
    void shouldStartWorkSuccessfully() {
        // Arrange
        String workerId = "WORKER-001";
        testWork.assignTo(workerId);
        
        when(workRepository.findById(testWorkId.getValue())).thenReturn(Optional.of(testWork));
        when(workRepository.save(any(Work.class))).thenReturn(testWork);

        // Act
        MobileWorkDetailDto result = service.startWork(testWorkId.getValue(), workerId);

        // Assert
        assertNotNull(result);
        assertEquals(testWorkId.getValue(), result.getWorkId());
        assertEquals(WorkStatus.IN_PROGRESS, result.getStatus());
        verify(workRepository).save(testWork);
        verify(notificationService).notifyWorkStarted(testWorkId.getValue(), workerId);
    }

    @Test
    void shouldThrowExceptionWhenStartingNonExistentWork() {
        // Arrange
        String workerId = "WORKER-001";
        when(workRepository.findById(testWorkId.getValue())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> service.startWork(testWorkId.getValue(), workerId));
    }

    @Test
    void shouldThrowExceptionWhenStartingWorkAssignedToAnotherWorker() {
        // Arrange
        String workerId = "WORKER-001";
        String otherWorkerId = "WORKER-002";
        testWork.assignTo(otherWorkerId);
        
        when(workRepository.findById(testWorkId.getValue())).thenReturn(Optional.of(testWork));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
            () -> service.startWork(testWorkId.getValue(), workerId));
    }

    @Test
    void shouldCompleteStepSuccessfully() {
        // Arrange
        String workerId = "WORKER-001";
        int stepNumber = 1;
        testWork.assignTo(workerId);
        testWork.start();
        
        MobileStepCompletionRequest request = new MobileStepCompletionRequest(
            workerId, List.of("SCAN001"), Map.of(), "Test completion", false, 5, "A01-1");
        
        MobileScanResultDto scanResult = MobileScanResultDto.valid("SCAN001", "SCAN001", "ITEM", "Valid scan");
        
        when(workRepository.findById(testWorkId.getValue())).thenReturn(Optional.of(testWork));
        when(barcodeScanProcessor.processScan(any())).thenReturn(scanResult);
        when(workRepository.save(any(Work.class))).thenReturn(testWork);

        // Act
        MobileStepCompletionDto result = service.completeStep(testWorkId.getValue(), stepNumber, request);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(stepNumber, result.getCompletedStepNumber());
        assertNotNull(result.getNextStepNumber());
        verify(workRepository).save(testWork);
        verify(notificationService).notifyStepCompleted(testWorkId.getValue(), stepNumber, workerId);
    }

    @Test
    void shouldFailStepCompletionWithInvalidScan() {
        // Arrange
        String workerId = "WORKER-001";
        int stepNumber = 1;
        testWork.assignTo(workerId);
        testWork.start();
        
        MobileStepCompletionRequest request = new MobileStepCompletionRequest(
            workerId, List.of("INVALID"), Map.of(), "Test completion", false, 5, "A01-1");
        
        MobileScanResultDto scanResult = MobileScanResultDto.invalid("INVALID", "Invalid barcode", null);
        
        when(workRepository.findById(testWorkId.getValue())).thenReturn(Optional.of(testWork));
        when(barcodeScanProcessor.processScan(any())).thenReturn(scanResult);

        // Act
        MobileStepCompletionDto result = service.completeStep(testWorkId.getValue(), stepNumber, request);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid scan"));
        verify(workRepository, never()).save(any());
        verify(notificationService, never()).notifyStepCompleted(any(), anyInt(), any());
    }

    @Test
    void shouldCompleteWorkSuccessfully() {
        // Arrange
        String workerId = "WORKER-001";
        testWork.assignTo(workerId);
        testWork.start();
        
        when(workRepository.findById(testWorkId.getValue())).thenReturn(Optional.of(testWork));
        when(workRepository.save(any(Work.class))).thenReturn(testWork);

        // Act
        MobileWorkCompletionDto result = service.completeWork(testWorkId.getValue(), workerId);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(testWorkId.getValue(), result.getWorkId());
        assertNotNull(result.getCompletedAt());
        verify(workRepository).save(testWork);
        verify(notificationService).notifyWorkCompleted(testWorkId.getValue(), workerId);
    }

    @Test
    void shouldSuspendWorkSuccessfully() {
        // Arrange
        String workerId = "WORKER-001";
        String reason = "Equipment failure";
        testWork.assignTo(workerId);
        testWork.start();
        
        when(workRepository.findById(testWorkId.getValue())).thenReturn(Optional.of(testWork));
        when(workRepository.save(any(Work.class))).thenReturn(testWork);

        // Act
        service.suspendWork(testWorkId.getValue(), workerId, reason);

        // Assert
        verify(workRepository).save(testWork);
        verify(notificationService).notifyWorkSuspended(testWorkId.getValue(), workerId, reason);
    }

    @Test
    void shouldValidateLocationSuccessfully() {
        // Arrange
        MobileLocationValidationRequest request = new MobileLocationValidationRequest(
            "WORKER-001", testWorkId.getValue().toString(), 1, "A01-1", "PICK", "SKU001", 5);
        
        LocationDirectiveService.LocationEvaluationResult evaluation = 
            LocationDirectiveService.LocationEvaluationResult.suitable(85.0, 1);
        
        when(locationDirectiveService.evaluateLocation(any(LocationQuery.class), any(BinLocation.class)))
            .thenReturn(evaluation);

        // Act
        MobileLocationValidationDto result = service.validateLocation(request);

        // Assert
        assertTrue(result.isValid());
        assertEquals("A01-1", result.getLocation());
        assertEquals(85.0, result.getSuitabilityScore());
    }

    @Test
    void shouldProcessScanSuccessfully() {
        // Arrange
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", testWorkId.getValue().toString(), 1, "SCAN001", "BARCODE", "ITEM", null);
        
        MobileScanResultDto expectedResult = MobileScanResultDto.valid(
            "SCAN001", "SCAN001", "ITEM", "Valid scan");
        
        when(barcodeScanProcessor.processScan(request)).thenReturn(expectedResult);

        // Act
        MobileScanResultDto result = service.processScan(request);

        // Assert
        assertEquals(expectedResult, result);
        assertTrue(result.isValid());
        verify(barcodeScanProcessor).processScan(request);
    }

    @Test
    void shouldStartBatchPickSuccessfully() {
        // Arrange
        String workerId = "WORKER-001";
        int maxItems = 5;
        
        List<Work> availableWork = Arrays.asList(
            createTestWork(new WorkId("WORK-001"), WorkStatus.RELEASED),
            createTestWork(new WorkId("WORK-002"), WorkStatus.RELEASED)
        );
        
        when(workRepository.findByWorkTypeAndStatus(WorkType.PICK, WorkStatus.RELEASED))
            .thenReturn(availableWork);
        when(workRepository.save(any(Work.class))).thenReturn(availableWork.get(0));

        // Act
        MobileBatchPickDto result = service.startBatchPick(workerId, maxItems);

        // Assert
        assertNotNull(result);
        assertEquals(workerId, result.getWorkerId());
        assertEquals(2, result.getAssignedWork().size());
        assertNotNull(result.getBatchId());
        assertEquals("ACTIVE", result.getStatus());
        verify(workRepository, times(2)).save(any(Work.class));
    }

    @Test
    void shouldHandleEmergencyStop() {
        // Arrange
        String workerId = "WORKER-001";
        String reason = "Safety hazard";
        
        List<Work> activeWork = Arrays.asList(testWork);
        testWork.assignTo(workerId);
        testWork.start();
        
        when(workRepository.findByAssignedToAndStatusIn(workerId, List.of(WorkStatus.IN_PROGRESS)))
            .thenReturn(activeWork);
        when(workRepository.save(any(Work.class))).thenReturn(testWork);

        // Act
        service.emergencyStop(workerId, reason);

        // Assert
        verify(workRepository).save(testWork);
        verify(notificationService).notifyEmergencyStop(workerId, reason);
    }

    private Work createTestWork(WorkId workId, WorkStatus status) {
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(10);
        
        // Create work steps
        List<WorkStep> steps = Arrays.asList(
            new WorkStep(1, WorkAction.NAVIGATE_TO_LOCATION, ValidationType.LOCATION_SCAN, 
                        "Go to A01-1", Map.of()),
            new WorkStep(2, WorkAction.PICK_ITEM, ValidationType.QUANTITY_RANGE, 
                        "Pick 10 units of SKU001", Map.of("quantity", 10))
        );
        
        Work work = new Work(WorkTemplateId.generate(), location, item, quantity, steps);
        
        // Set status appropriately
        if (status == WorkStatus.ASSIGNED) {
            work.assignTo("WORKER-001");
        } else if (status == WorkStatus.IN_PROGRESS) {
            work.assignTo("WORKER-001");
            work.start();
        }
        
        return work;
    }
}