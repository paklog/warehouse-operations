package com.paklog.warehouse.infrastructure.web.mobile;

import com.paklog.warehouse.application.mobile.MobileWorkflowService;
import com.paklog.warehouse.application.mobile.dto.*;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import java.util.UUID;
import com.paklog.warehouse.domain.work.WorkType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.*;

// OpenAPI Documentation imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/mobile/work")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Mobile Work Management", description = "REST API for mobile work operations including assignments, status updates, and completion")
public class MobileWorkController {

    private final MobileWorkflowService mobileWorkflowService;

    public MobileWorkController(MobileWorkflowService mobileWorkflowService) {
        this.mobileWorkflowService = Objects.requireNonNull(mobileWorkflowService,
            "MobileWorkflowService cannot be null");
    }

    @Operation(summary = "Get assigned work for worker", description = "Retrieves all work currently assigned to a specific worker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved assigned work"),
        @ApiResponse(responseCode = "404", description = "Worker not found")
    })
    @GetMapping(value = "/assigned/{workerId}", produces = "application/json")
    public ResponseEntity<List<MobileWorkSummaryDto>> getAssignedWork(
            @Parameter(description = "Worker ID", required = true) 
            @PathVariable String workerId) {
        List<MobileWorkSummaryDto> assignedWork = mobileWorkflowService.getAssignedWork(workerId);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .body(assignedWork);
    }

    @Operation(summary = "Get available work for worker", description = "Retrieves available work that can be assigned to a worker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved available work"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @GetMapping(value = "/available/{workerId}", produces = "application/json")
    public ResponseEntity<List<MobileWorkSummaryDto>> getAvailableWork(
            @Parameter(description = "Worker ID", required = true) 
            @PathVariable String workerId,
            @Parameter(description = "Filter by work type") 
            @RequestParam(required = false) WorkType workType,
            @Parameter(description = "Maximum number of results") 
            @RequestParam(defaultValue = "10") int limit) {
        List<MobileWorkSummaryDto> availableWork = mobileWorkflowService
            .getAvailableWork(workerId, workType, limit);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(java.time.Duration.ofMinutes(5)))
            .body(availableWork);
    }

    @Operation(summary = "Update work status to started", description = "Changes work status to IN_PROGRESS and assigns to worker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Work successfully started"),
        @ApiResponse(responseCode = "400", description = "Invalid work ID or worker ID"),
        @ApiResponse(responseCode = "409", description = "Work cannot be started in current state")
    })
    @PatchMapping(value = "/{workId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MobileWorkDetailDto> updateWorkStatus(
            @Parameter(description = "Work ID", required = true) 
            @PathVariable String workId,
            @RequestBody WorkStatusUpdateRequest request) {
        UUID id = UUID.fromString(workId);
        MobileWorkDetailDto workDetail;
        
        switch (request.getStatus().toUpperCase()) {
            case "IN_PROGRESS":
                workDetail = mobileWorkflowService.startWork(id, request.getWorkerId());
                break;
            case "SUSPENDED":
                mobileWorkflowService.suspendWork(id, request.getWorkerId(), request.getReason());
                workDetail = mobileWorkflowService.getWorkDetails(id, request.getWorkerId());
                break;
            case "COMPLETED":
                MobileWorkCompletionDto completionResult = mobileWorkflowService.completeWork(id, request.getWorkerId());
                workDetail = mobileWorkflowService.getWorkDetails(id, request.getWorkerId());
                break;
            default:
                throw new IllegalArgumentException("Unsupported status: " + request.getStatus());
        }
        
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .body(workDetail);
    }

    @Operation(summary = "Get work details", description = "Retrieves detailed information about a specific work item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Work details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Work not found")
    })
    @GetMapping(value = "/{workId}", produces = "application/json")
    public ResponseEntity<MobileWorkDetailDto> getWorkDetails(
            @Parameter(description = "Work ID", required = true) 
            @PathVariable String workId,
            @Parameter(description = "Worker ID") 
            @RequestParam String workerId) {
        UUID id = UUID.fromString(workId);
        MobileWorkDetailDto workDetail = mobileWorkflowService.getWorkDetails(id, workerId);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(java.time.Duration.ofMinutes(1)))
            .body(workDetail);
    }

    @Operation(summary = "Update work step status", description = "Marks a work step as completed with validation results")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Step completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid step or completion data"),
        @ApiResponse(responseCode = "409", description = "Step cannot be completed in current state")
    })
    @PatchMapping(value = "/{workId}/steps/{stepNumber}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MobileStepCompletionDto> updateStepStatus(
            @Parameter(description = "Work ID", required = true) 
            @PathVariable String workId,
            @Parameter(description = "Step number", required = true) 
            @PathVariable int stepNumber,
            @RequestBody MobileStepCompletionRequest request) {
        UUID id = UUID.fromString(workId);
        MobileStepCompletionDto result = mobileWorkflowService
            .completeStep(id, stepNumber, request);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .body(result);
    }

    // This endpoint is now handled by the PATCH /{workId} endpoint with status="COMPLETED"

    // This endpoint is now handled by the PATCH /{workId} endpoint with status="SUSPENDED"

    // This endpoint is now handled by the PATCH /{workId} endpoint with status="IN_PROGRESS"

    @Operation(summary = "Process barcode scan", description = "Creates a scan result for barcode validation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Scan processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid scan data")
    })
    @PostMapping(value = "/scans", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MobileScanResultDto> createScanResult(
            @RequestBody MobileScanRequest request) {
        MobileScanResultDto result = mobileWorkflowService.processScan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .cacheControl(CacheControl.noCache())
            .body(result);
    }

    @Operation(summary = "Validate location", description = "Creates a location validation result")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Location validation created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid location data")
    })
    @PostMapping(value = "/location-validations", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MobileLocationValidationDto> createLocationValidation(
            @RequestBody MobileLocationValidationRequest request) {
        MobileLocationValidationDto result = mobileWorkflowService
            .validateLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .cacheControl(CacheControl.noCache())
            .body(result);
    }

    @Operation(summary = "Get worker performance metrics", description = "Retrieves performance metrics for a worker over a specified period")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Worker not found")
    })
    @GetMapping(value = "/workers/{workerId}/metrics", produces = "application/json")
    public ResponseEntity<MobileWorkerMetricsDto> getWorkerMetrics(
            @Parameter(description = "Worker ID", required = true) 
            @PathVariable String workerId,
            @Parameter(description = "Time period for metrics") 
            @RequestParam(defaultValue = "TODAY") String period) {
        MobileWorkerMetricsDto metrics = mobileWorkflowService
            .getWorkerMetrics(workerId, period);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(java.time.Duration.ofMinutes(15)))
            .body(metrics);
    }

    @Operation(summary = "Create batch pick operation", description = "Initiates a batch picking operation for multiple items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Batch pick created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid batch pick parameters")
    })
    @PostMapping(value = "/batch-picks", produces = "application/json")
    public ResponseEntity<MobileBatchPickDto> createBatchPick(
            @Parameter(description = "Worker ID", required = true) 
            @RequestParam String workerId,
            @Parameter(description = "Maximum items in batch") 
            @RequestParam(defaultValue = "5") int maxItems) {
        MobileBatchPickDto batchPick = mobileWorkflowService
            .startBatchPick(workerId, maxItems);
        return ResponseEntity.status(HttpStatus.CREATED)
            .cacheControl(CacheControl.noCache())
            .body(batchPick);
    }

    @Operation(summary = "Create emergency alert", description = "Creates an emergency alert to stop all work for a worker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Emergency alert created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid emergency alert data")
    })
    @PostMapping(value = "/emergency-alerts", produces = "application/json")
    public ResponseEntity<Void> createEmergencyAlert(
            @Parameter(description = "Worker ID", required = true) 
            @RequestParam String workerId,
            @Parameter(description = "Emergency reason", required = true) 
            @RequestParam String reason) {
        mobileWorkflowService.emergencyStop(workerId, reason);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // DTO for work status updates
    public static class WorkStatusUpdateRequest {
        private String status;
        private String workerId;
        private String reason;
        
        public WorkStatusUpdateRequest() {}
        
        public WorkStatusUpdateRequest(String status, String workerId, String reason) {
            this.status = status;
            this.workerId = workerId;
            this.reason = reason;
        }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getWorkerId() { return workerId; }
        public void setWorkerId(String workerId) { this.workerId = workerId; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}