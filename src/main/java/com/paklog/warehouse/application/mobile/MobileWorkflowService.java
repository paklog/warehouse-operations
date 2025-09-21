package com.paklog.warehouse.application.mobile;

import com.paklog.warehouse.application.mobile.dto.*;
import com.paklog.warehouse.application.mobile.barcode.BarcodeScanProcessor;
import com.paklog.warehouse.domain.work.*;

import java.util.UUID;
import com.paklog.warehouse.domain.location.LocationDirectiveService;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class MobileWorkflowService {
    private static final Logger logger = LoggerFactory.getLogger(MobileWorkflowService.class);
    
    private final WorkRepository workRepository;
    private final BarcodeScanProcessor barcodeScanProcessor;
    private final LocationDirectiveService locationDirectiveService;
    private final MobileNotificationService notificationService;

    public MobileWorkflowService(WorkRepository workRepository,
                               BarcodeScanProcessor barcodeScanProcessor,
                               LocationDirectiveService locationDirectiveService,
                               MobileNotificationService notificationService) {
        this.workRepository = Objects.requireNonNull(workRepository, "WorkRepository cannot be null");
        this.barcodeScanProcessor = Objects.requireNonNull(barcodeScanProcessor, "BarcodeScanProcessor cannot be null");
        this.locationDirectiveService = Objects.requireNonNull(locationDirectiveService, "LocationDirectiveService cannot be null");
        this.notificationService = Objects.requireNonNull(notificationService, "MobileNotificationService cannot be null");
    }

    public List<MobileWorkSummaryDto> getAssignedWork(String workerId) {
        logger.info("Getting assigned work for worker: {}", workerId);
        
        List<Work> assignedWork = new ArrayList<>();
        assignedWork.addAll(workRepository.findByAssignedToAndStatus(workerId, WorkStatus.ASSIGNED));
        assignedWork.addAll(workRepository.findByAssignedToAndStatus(workerId, WorkStatus.IN_PROGRESS));
        
        return assignedWork.stream()
            .map(this::mapToMobileWorkSummary)
            .sorted(this::compareWorkPriority)
            .collect(Collectors.toList());
    }

    public List<MobileWorkSummaryDto> getAvailableWork(String workerId, WorkType workType, int limit) {
        logger.info("Getting available work for worker: {}, type: {}, limit: {}", workerId, workType, limit);
        
        List<Work> availableWork = workRepository.findAvailable(WorkStatus.RELEASED);
        if (workType != null) {
            // Filter by work type - since the repo doesn't have findByWorkTypeAndStatus
            // we filter in memory (could be optimized with additional repo method)
            availableWork = availableWork.stream()
                .filter(work -> determineWorkType(work) == workType)
                .collect(Collectors.toList());
        }
        
        return availableWork.stream()
            .filter(work -> work.getAssignedTo() == null)
            .map(this::mapToMobileWorkSummary)
            .sorted(this::compareWorkPriority)
            .limit(limit)
            .collect(Collectors.toList());
    }

    public MobileWorkDetailDto startWork(UUID workId, String workerId) {
        logger.info("Starting work: {} for worker: {}", workId, workerId);
        
        Optional<Work> workOpt = workRepository.findById(workId);
        if (workOpt.isEmpty()) {
            throw new IllegalArgumentException("Work not found: " + workId);
        }
        
        Work work = workOpt.get();
        
        if (work.getStatus() == WorkStatus.ASSIGNED && !workerId.equals(work.getAssignedTo())) {
            throw new IllegalStateException("Work is assigned to different worker");
        }
        
        if (work.getStatus() == WorkStatus.RELEASED) {
            work.assignTo(workerId);
        }
        
        work.start();
        workRepository.save(work);
        
        notificationService.notifyWorkStarted(workId, workerId);
        
        return mapToMobileWorkDetail(work);
    }

    public MobileWorkDetailDto getWorkDetails(UUID workId, String workerId) {
        logger.debug("Getting work details for: {} by worker: {}", workId, workerId);
        
        Optional<Work> workOpt = workRepository.findById(workId);
        if (workOpt.isEmpty()) {
            throw new IllegalArgumentException("Work not found: " + workId);
        }
        
        Work work = workOpt.get();
        
        if (!workerId.equals(work.getAssignedTo())) {
            throw new IllegalStateException("Work not assigned to worker: " + workerId);
        }
        
        return mapToMobileWorkDetail(work);
    }

    public MobileStepCompletionDto completeStep(UUID workId, int stepNumber, 
                                              MobileStepCompletionRequest request) {
        logger.info("Completing step {} for work: {} by worker: {}", 
                   stepNumber, workId, request.getWorkerId());
        
        Optional<Work> workOpt = workRepository.findById(workId);
        if (workOpt.isEmpty()) {
            throw new IllegalArgumentException("Work not found: " + workId);
        }
        
        Work work = workOpt.get();
        
        if (!request.getWorkerId().equals(work.getAssignedTo())) {
            throw new IllegalStateException("Work not assigned to worker: " + request.getWorkerId());
        }
        
        // Validate scanned codes if required
        if (request.getScannedCodes() != null && !request.getScannedCodes().isEmpty()) {
            for (String scannedCode : request.getScannedCodes()) {
                MobileScanRequest scanRequest = new MobileScanRequest(
                    request.getWorkerId(), workId.toString(), stepNumber, 
                    scannedCode, "BARCODE", null, null);
                MobileScanResultDto scanResult = processScan(scanRequest);
                
                if (!scanResult.isValid() && !request.isForceComplete()) {
                    return MobileStepCompletionDto.failure("Invalid scan: " + scannedCode, 
                        List.of(scanResult.getMessage()));
                }
            }
        }
        
        try {
            boolean isWorkCompleted = work.completeCurrentStep();
            workRepository.save(work);
            
            int nextStep = isWorkCompleted ? -1 : work.getCurrentStepIndex() + 1;
            
            notificationService.notifyStepCompleted(workId, stepNumber, request.getWorkerId());
            
            return MobileStepCompletionDto.success(stepNumber, 
                isWorkCompleted ? null : nextStep, 
                isWorkCompleted, 
                isWorkCompleted ? "Work completed successfully" : "Step completed, proceed to next step");
                
        } catch (Exception e) {
            logger.error("Failed to complete step {} for work {}: {}", stepNumber, workId, e.getMessage());
            return MobileStepCompletionDto.failure("Failed to complete step: " + e.getMessage(), 
                List.of(e.getMessage()));
        }
    }

    public MobileWorkCompletionDto completeWork(UUID workId, String workerId) {
        logger.info("Completing work: {} by worker: {}", workId, workerId);
        
        Optional<Work> workOpt = workRepository.findById(workId);
        if (workOpt.isEmpty()) {
            throw new IllegalArgumentException("Work not found: " + workId);
        }
        
        Work work = workOpt.get();
        
        if (!workerId.equals(work.getAssignedTo())) {
            throw new IllegalStateException("Work not assigned to worker: " + workerId);
        }
        
        if (work.getStatus() != WorkStatus.IN_PROGRESS) {
            throw new IllegalStateException("Work is not in progress");
        }
        
        try {
            work.complete();
            workRepository.save(work);
            
            Instant completedAt = Instant.now();
            Duration duration = work.getStartedAt() != null ? 
                Duration.between(work.getStartedAt(), completedAt) : Duration.ZERO;
            
            double performanceScore = calculatePerformanceScore(work, duration);
            
            notificationService.notifyWorkCompleted(workId, workerId);
            
            return MobileWorkCompletionDto.success(workId.toString(), completedAt, 
                duration, work.getSteps().size(), performanceScore);
                
        } catch (Exception e) {
            logger.error("Failed to complete work {}: {}", workId, e.getMessage());
            return MobileWorkCompletionDto.failure(workId.toString(), 
                "Failed to complete work: " + e.getMessage());
        }
    }

    public void suspendWork(UUID workId, String workerId, String reason) {
        logger.info("Suspending work: {} by worker: {} for reason: {}", workId, workerId, reason);
        
        Optional<Work> workOpt = workRepository.findById(workId);
        if (workOpt.isEmpty()) {
            throw new IllegalArgumentException("Work not found: " + workId);
        }
        
        Work work = workOpt.get();
        
        if (!workerId.equals(work.getAssignedTo())) {
            throw new IllegalStateException("Work not assigned to worker: " + workerId);
        }
        
        // Since suspend doesn't exist, we'll cancel the work with reason
        work.cancel("SUSPENDED: " + reason);
        workRepository.save(work);
        
        notificationService.notifyWorkSuspended(workId, workerId, reason);
    }

    public MobileWorkDetailDto resumeWork(UUID workId, String workerId) {
        logger.info("Resuming work: {} by worker: {}", workId, workerId);
        
        Optional<Work> workOpt = workRepository.findById(workId);
        if (workOpt.isEmpty()) {
            throw new IllegalArgumentException("Work not found: " + workId);
        }
        
        Work work = workOpt.get();
        
        if (!workerId.equals(work.getAssignedTo())) {
            throw new IllegalStateException("Work not assigned to worker: " + workerId);
        }
        
        // Since resume doesn't exist, just return the work detail as-is
        // In a real implementation, this might recreate the work if cancelled
        notificationService.notifyWorkResumed(workId, workerId);
        
        return mapToMobileWorkDetail(work);
    }

    public MobileScanResultDto processScan(MobileScanRequest request) {
        logger.debug("Processing scan: {} for worker: {}", request.getScannedCode(), request.getWorkerId());
        
        return barcodeScanProcessor.processScan(request);
    }

    public MobileLocationValidationDto validateLocation(MobileLocationValidationRequest request) {
        logger.debug("Validating location: {} for work: {}", request.getLocation(), request.getWorkId());
        
        try {
            // Parse location format A01-1 to aisle=A, bay=01, level=1
            String[] parts = request.getLocation().split("-");
            String aisle = parts[0].substring(0, 1);
            String bay = parts[0].substring(1);
            String level = parts[1];
            BinLocation location = new BinLocation(aisle, bay, level);
            WorkType workType = WorkType.valueOf(request.getWorkType());
            SkuCode item = new SkuCode(request.getItem());
            Quantity quantity = new Quantity(request.getQuantity());
            
            LocationDirectiveService.LocationEvaluationResult evaluation = 
                locationDirectiveService.evaluateLocation(
                    new com.paklog.warehouse.domain.location.LocationQuery(workType, item, quantity), 
                    location);
            
            if (evaluation.isSuitable()) {
                return MobileLocationValidationDto.valid(request.getLocation(), 
                    evaluation.getScore(), "Location is suitable for this work");
            } else {
                return MobileLocationValidationDto.invalid(request.getLocation(), 
                    "Location not suitable", evaluation.getViolations());
            }
            
        } catch (Exception e) {
            logger.error("Error validating location {}: {}", request.getLocation(), e.getMessage());
            return MobileLocationValidationDto.invalid(request.getLocation(), 
                "Validation error: " + e.getMessage(), List.of(e.getMessage()));
        }
    }

    public MobileWorkerMetricsDto getWorkerMetrics(String workerId, String period) {
        logger.info("Getting metrics for worker: {} for period: {}", workerId, period);
        
        // This would be implemented to calculate actual metrics from work history
        // For now, return placeholder metrics
        return new MobileWorkerMetricsDto(
            workerId, period, 25, 30, 83.3, 15.5, 85.0,
            150, 75, 20, 97.5, Instant.now(),
            Map.of("PICK", 20, "PUT", 8, "COUNT", 2),
            Map.of("PICK", 88.5, "PUT", 82.0, "COUNT", 90.0)
        );
    }

    public MobileBatchPickDto startBatchPick(String workerId, int maxItems) {
        logger.info("Starting batch pick for worker: {} with max items: {}", workerId, maxItems);
        
        List<Work> availablePickWork = workRepository.findByStatus(WorkStatus.RELEASED).stream()
            .filter(work -> determineWorkType(work) == WorkType.PICK)
            .limit(maxItems)
            .collect(Collectors.toList());
        
        String batchId = UUID.randomUUID().toString();
        
        // Assign work to worker
        for (Work work : availablePickWork) {
            work.assignTo(workerId);
            workRepository.save(work);
        }
        
        List<MobileWorkSummaryDto> workSummaries = availablePickWork.stream()
            .map(this::mapToMobileWorkSummary)
            .collect(Collectors.toList());
        
        return new MobileBatchPickDto(
            batchId, workerId, workSummaries, "OPTIMIZED_ROUTE", 
            extractLocations(availablePickWork), 
            availablePickWork.stream().mapToInt(w -> w.getQuantity().getValue()).sum(),
            estimateBatchDuration(availablePickWork),
            Map.of("created_at", Instant.now()),
            List.of("Follow the optimized route", "Scan each item and location"),
            "ACTIVE"
        );
    }

    public void emergencyStop(String workerId, String reason) {
        logger.warn("Emergency stop for worker: {} - reason: {}", workerId, reason);
        
        List<Work> activeWork = workRepository.findByAssignedToAndStatus(workerId, 
            WorkStatus.IN_PROGRESS);
        
        for (Work work : activeWork) {
            work.cancel("EMERGENCY_STOP: " + reason);
            workRepository.save(work);
        }
        
        notificationService.notifyEmergencyStop(workerId, reason);
    }

    // Private helper methods
    private MobileWorkSummaryDto mapToMobileWorkSummary(Work work) {
        return new MobileWorkSummaryDto(
            work.getWorkId().toString(),
            determineWorkType(work),
            work.getStatus(),
            Priority.fromString(work.getPriority()),
            work.getLocation().toString(),
            work.getItem().getValue(),
            work.getQuantity().getValue(),
            work.getAssignedTo(),
            work.getAssignedAt(),
            null, // No due date in current domain
            work.getSteps().size(),
            work.getCurrentStepIndex(),
            isUrgentPriority(work.getPriority()),
            "Work description" // Not available in current domain
        );
    }

    private MobileWorkDetailDto mapToMobileWorkDetail(Work work) {
        List<MobileStepDto> steps = work.getSteps().stream()
            .map(this::mapToMobileStep)
            .collect(Collectors.toList());
        
        return new MobileWorkDetailDto(
            work.getWorkId().toString(),
            determineWorkType(work),
            work.getStatus(),
            Priority.fromString(work.getPriority()),
            work.getLocation().toString(),
            work.getItem().getValue(),
            "Item description", // Not available in current domain
            work.getQuantity().getValue(),
            work.getAssignedTo(),
            work.getAssignedAt(),
            work.getStartedAt(),
            null, // No due date in current domain
            steps,
            work.getCurrentStepIndex() + 1, // Convert to 1-based
            isUrgentPriority(work.getPriority()),
            "Work description", // Not available in current domain
            Map.of(), // No attributes in current domain
            List.of("Follow work steps"), // No instructions in current domain
            determineNextAction(work)
        );
    }

    private MobileStepDto mapToMobileStep(WorkStep step) {
        return new MobileStepDto(
            step.getSequence(),
            "Step " + step.getSequence(), // No name in current domain
            step.getDescription(),
            null, // No location per step in current domain
            step.getAction().toString(),
            step.getParameters(),
            false, // Completion tracking not in current domain
            !step.isMandatory(),
            step.requiresValidation(),
            List.of(), // No expected scans in current domain
            step.getValidation().toString(),
            null, // No completion tracking
            null, // No completion tracking
            null, // No notes
            5 // Default estimated duration
        );
    }

    private int compareWorkPriority(MobileWorkSummaryDto a, MobileWorkSummaryDto b) {
        // First by urgency
        if (a.isUrgent() != b.isUrgent()) {
            return a.isUrgent() ? -1 : 1;
        }
        
        // Then by priority
        int priorityCompare = a.getPriority().compareTo(b.getPriority());
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        
        // Finally by due date
        if (a.getDueDate() != null && b.getDueDate() != null) {
            return a.getDueDate().compareTo(b.getDueDate());
        }
        
        return 0;
    }

    private String determineNextAction(Work work) {
        if (work.getStatus() == WorkStatus.COMPLETED) {
            return "WORK_COMPLETED";
        } else if (work.getStatus() == WorkStatus.IN_PROGRESS) {
            return "CONTINUE_STEP";
        } else if (work.getStatus() == WorkStatus.ASSIGNED) {
            return "START_WORK";
        } else {
            return "UNKNOWN";
        }
    }

    private double calculatePerformanceScore(Work work, Duration actualDuration) {
        // Simple performance calculation based on time vs estimated time
        // Since we don't have estimated duration per step, use a default calculation
        int totalSteps = work.getSteps().size();
        long estimatedMinutes = totalSteps * 5; // 5 minutes per step default
        
        if (estimatedMinutes == 0) {
            return 100.0; // No estimate available
        }
        
        double actualMinutes = actualDuration.toMinutes();
        if (actualMinutes == 0) {
            return 100.0; // Completed instantly
        }
        
        double efficiency = (estimatedMinutes / actualMinutes) * 100.0;
        
        // Cap at 100% and floor at 0%
        return Math.min(100.0, Math.max(0.0, efficiency));
    }

    private List<String> extractLocations(List<Work> workList) {
        return workList.stream()
            .map(work -> work.getLocation().toString())
            .distinct()
            .collect(Collectors.toList());
    }

    private int estimateBatchDuration(List<Work> workList) {
        return workList.stream()
            .mapToInt(work -> work.getSteps().size() * 5) // 5 minutes per step default
            .sum();
    }
    
    private WorkType determineWorkType(Work work) {
        // Since Work doesn't have WorkType in current domain, infer from template or default
        return work.getWorkType();
    }
    
    private boolean isUrgentPriority(String priority) {
        return "URGENT".equalsIgnoreCase(priority) || "CRITICAL".equalsIgnoreCase(priority);
    }
}