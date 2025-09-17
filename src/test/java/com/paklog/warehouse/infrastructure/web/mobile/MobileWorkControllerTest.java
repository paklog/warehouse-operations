package com.paklog.warehouse.infrastructure.web.mobile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.warehouse.application.mobile.MobileWorkflowService;
import com.paklog.warehouse.application.mobile.dto.*;
import com.paklog.warehouse.domain.shared.Priority;
import com.paklog.warehouse.domain.work.WorkId;
import com.paklog.warehouse.domain.work.WorkStatus;

import java.util.UUID;
import com.paklog.warehouse.domain.work.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MobileWorkController.class)
class MobileWorkControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MobileWorkflowService mobileWorkflowService;

    private MobileWorkSummaryDto testWorkSummary;
    private MobileWorkDetailDto testWorkDetail;

    @BeforeEach
    void setUp() {
        testWorkSummary = new MobileWorkSummaryDto(
            "WORK-001", WorkType.PICK, WorkStatus.ASSIGNED, Priority.NORMAL,
            "A01-1", "SKU001", 10, "WORKER-001", Instant.now(),
            Instant.now().plusSeconds(3600), 2, 0, false, "Test work"
        );

        testWorkDetail = new MobileWorkDetailDto(
            "WORK-001", WorkType.PICK, WorkStatus.IN_PROGRESS, Priority.NORMAL,
            "A01-1", "SKU001", "Test Item", 10, "WORKER-001", Instant.now(),
            Instant.now(), Instant.now().plusSeconds(3600), 
            Arrays.asList(createTestMobileStep()), 1, false, "Test work",
            Map.of(), List.of("Pick carefully"), "CONTINUE_STEP"
        );
    }

    @Test
    void shouldGetAssignedWorkSuccessfully() throws Exception {
        // Arrange
        String workerId = "WORKER-001";
        List<MobileWorkSummaryDto> assignedWork = Arrays.asList(testWorkSummary);
        
        when(mobileWorkflowService.getAssignedWork(workerId)).thenReturn(assignedWork);

        // Act & Assert
        mockMvc.perform(get("/api/mobile/work/assigned/{workerId}", workerId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].workId").value("WORK-001"))
            .andExpect(jsonPath("$[0].assignedTo").value(workerId));
    }

    @Test
    void shouldGetAvailableWorkSuccessfully() throws Exception {
        // Arrange
        String workerId = "WORKER-001";
        List<MobileWorkSummaryDto> availableWork = Arrays.asList(testWorkSummary);
        
        when(mobileWorkflowService.getAvailableWork(eq(workerId), eq(WorkType.PICK), eq(10)))
            .thenReturn(availableWork);

        // Act & Assert
        mockMvc.perform(get("/api/mobile/work/available/{workerId}", workerId)
                .param("workType", "PICK")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].workId").value("WORK-001"));
    }

    @Test
    void shouldStartWorkSuccessfully() throws Exception {
        // Arrange
        UUID workId = UUID.randomUUID();
        String workerId = "WORKER-001";
        
        when(mobileWorkflowService.startWork(eq(workId), eq(workerId)))
            .thenReturn(testWorkDetail);

        // Act & Assert
        mockMvc.perform(post("/api/mobile/work/{workId}/start", workId)
                .param("workerId", workerId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.workId").value(workId))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldGetWorkDetailsSuccessfully() throws Exception {
        // Arrange
        UUID workId = UUID.randomUUID();
        String workerId = "WORKER-001";
        
        when(mobileWorkflowService.getWorkDetails(eq(workId), eq(workerId)))
            .thenReturn(testWorkDetail);

        // Act & Assert
        mockMvc.perform(get("/api/mobile/work/{workId}/details", workId)
                .param("workerId", workerId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.workId").value(workId))
            .andExpect(jsonPath("$.assignedTo").value(workerId));
    }

    @Test
    void shouldCompleteStepSuccessfully() throws Exception {
        // Arrange
        UUID workId = UUID.randomUUID();
        int stepNumber = 1;
        
        MobileStepCompletionRequest request = new MobileStepCompletionRequest(
            "WORKER-001", List.of("SCAN001"), Map.of(), "Step completed", false, 5, "A01-1");
        
        MobileStepCompletionDto completionResult = MobileStepCompletionDto.success(
            stepNumber, 2, false, "Step completed successfully");
        
        when(mobileWorkflowService.completeStep(eq(workId), eq(stepNumber), any()))
            .thenReturn(completionResult);

        // Act & Assert
        mockMvc.perform(post("/api/mobile/work/{workId}/steps/{stepNumber}/complete", workId, stepNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.completedStepNumber").value(stepNumber));
    }

    @Test
    void shouldCompleteWorkSuccessfully() throws Exception {
        // Arrange
        UUID workId = UUID.randomUUID();
        String workerId = "WORKER-001";
        
        MobileWorkCompletionDto completionResult = MobileWorkCompletionDto.success(
            workId.toString(), Instant.now(), java.time.Duration.ofMinutes(15), 2, 88.5);
        
        when(mobileWorkflowService.completeWork(eq(workId), eq(workerId)))
            .thenReturn(completionResult);

        // Act & Assert
        mockMvc.perform(post("/api/mobile/work/{workId}/complete", workId)
                .param("workerId", workerId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.workId").value(workId));
    }

    @Test
    void shouldSuspendWorkSuccessfully() throws Exception {
        // Arrange
        UUID workId = UUID.randomUUID();
        String workerId = "WORKER-001";
        String reason = "Equipment failure";

        // Act & Assert
        mockMvc.perform(post("/api/mobile/work/{workId}/suspend", workId)
                .param("workerId", workerId)
                .param("reason", reason))
            .andExpect(status().isOk());
    }

    @Test
    void shouldProcessScanSuccessfully() throws Exception {
        // Arrange
        MobileScanRequest scanRequest = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, "SCAN001", "BARCODE", "ITEM", null);
        
        MobileScanResultDto scanResult = MobileScanResultDto.valid(
            "SCAN001", "SCAN001", "ITEM", "Valid scan");
        
        when(mobileWorkflowService.processScan(any())).thenReturn(scanResult);

        // Act & Assert
        mockMvc.perform(post("/api/mobile/work/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scanRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.scannedValue").value("SCAN001"));
    }

    @Test
    void shouldValidateLocationSuccessfully() throws Exception {
        // Arrange
        MobileLocationValidationRequest validationRequest = new MobileLocationValidationRequest(
            "WORKER-001", "WORK-001", 1, "A01-1", "PICK", "SKU001", 5);
        
        MobileLocationValidationDto validationResult = MobileLocationValidationDto.valid(
            "A01-1", 85.0, "Location is suitable");
        
        when(mobileWorkflowService.validateLocation(any())).thenReturn(validationResult);

        // Act & Assert
        mockMvc.perform(post("/api/mobile/work/validate-location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validationRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.location").value("A01-1"));
    }

    @Test
    void shouldGetWorkerMetricsSuccessfully() throws Exception {
        // Arrange
        String workerId = "WORKER-001";
        String period = "TODAY";
        
        MobileWorkerMetricsDto metrics = new MobileWorkerMetricsDto(
            workerId, period, 25, 30, 83.3, 15.5, 85.0,
            150, 75, 20, 97.5, Instant.now(),
            Map.of("PICK", 20, "PUT", 8), Map.of("PICK", 88.5, "PUT", 82.0));
        
        when(mobileWorkflowService.getWorkerMetrics(workerId, period)).thenReturn(metrics);

        // Act & Assert
        mockMvc.perform(get("/api/mobile/work/metrics/{workerId}", workerId)
                .param("period", period))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.workerId").value(workerId))
            .andExpect(jsonPath("$.completedTasks").value(25))
            .andExpect(jsonPath("$.accuracyRate").value(97.5));
    }

    @Test
    void shouldStartBatchPickSuccessfully() throws Exception {
        // Arrange
        String workerId = "WORKER-001";
        int maxItems = 5;
        
        MobileBatchPickDto batchPick = new MobileBatchPickDto(
            "BATCH-001", workerId, Arrays.asList(testWorkSummary),
            "OPTIMIZED_ROUTE", List.of("A01-1"), 10, 25, 
            Map.of(), List.of(), "ACTIVE");
        
        when(mobileWorkflowService.startBatchPick(workerId, maxItems)).thenReturn(batchPick);

        // Act & Assert
        mockMvc.perform(post("/api/mobile/work/batch-pick")
                .param("workerId", workerId)
                .param("maxItems", String.valueOf(maxItems)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.batchId").value("BATCH-001"))
            .andExpect(jsonPath("$.workerId").value(workerId))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldHandleEmergencyStopSuccessfully() throws Exception {
        // Arrange
        String workerId = "WORKER-001";
        String reason = "Safety hazard";

        // Act & Assert
        mockMvc.perform(post("/api/mobile/work/emergency-stop")
                .param("workerId", workerId)
                .param("reason", reason))
            .andExpect(status().isOk());
    }

    private MobileStepDto createTestMobileStep() {
        return new MobileStepDto(
            1, "Navigate to location", "Go to A01-1", "A01-1", "NAVIGATE",
            Map.of(), false, false, false, List.of(), null,
            null, null, null, 2
        );
    }
}