package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WorkCreationService {
    private static final Logger logger = LoggerFactory.getLogger(WorkCreationService.class);
    
    private final WorkTemplateRepository workTemplateRepository;
    private final WorkRepository workRepository;

    public WorkCreationService(WorkTemplateRepository workTemplateRepository, 
                              WorkRepository workRepository) {
        this.workTemplateRepository = Objects.requireNonNull(workTemplateRepository, 
            "WorkTemplateRepository cannot be null");
        this.workRepository = Objects.requireNonNull(workRepository, 
            "WorkRepository cannot be null");
    }

    public Work createWork(WorkType workType, BinLocation location, SkuCode item, 
                          Quantity quantity, String assignedTo) {
        logger.info("Creating work for type: {}, location: {}, item: {}", 
                   workType, location, item);

        WorkTemplate template = selectBestTemplate(workType);
        if (template == null) {
            throw new IllegalStateException("No active template found for work type: " + workType);
        }

        WorkRequest request = new WorkRequest(location, item, quantity, assignedTo);
        Work work = template.generateWork(request);
        
        Work savedWork = workRepository.save(work);
        logger.info("Work created successfully with ID: {}", savedWork.getWorkId());
        
        return savedWork;
    }

    public Work createWorkFromTemplate(WorkTemplateId templateId, BinLocation location, 
                                      SkuCode item, Quantity quantity, String assignedTo) {
        logger.info("Creating work from template: {}", templateId);

        Optional<WorkTemplate> templateOpt = workTemplateRepository.findById(templateId);
        if (templateOpt.isEmpty()) {
            throw new IllegalArgumentException("Template not found: " + templateId);
        }

        WorkTemplate template = templateOpt.get();
        if (!template.isActive()) {
            throw new IllegalStateException("Cannot create work from inactive template: " + templateId);
        }

        WorkRequest request = new WorkRequest(location, item, quantity, assignedTo);
        Work work = template.generateWork(request);
        
        Work savedWork = workRepository.save(work);
        logger.info("Work created from template {} with ID: {}", templateId, savedWork.getWorkId());
        
        return savedWork;
    }

    public List<Work> createBatchWork(WorkType workType, List<WorkRequest> requests) {
        logger.info("Creating batch work for {} requests of type: {}", requests.size(), workType);

        WorkTemplate template = selectBestTemplate(workType);
        if (template == null) {
            throw new IllegalStateException("No active template found for work type: " + workType);
        }

        List<Work> workList = requests.stream()
            .map(template::generateWork)
            .map(workRepository::save)
            .toList();

        logger.info("Created {} work items successfully", workList.size());
        return workList;
    }

    public boolean canCreateWork(WorkType workType) {
        List<WorkTemplate> templates = workTemplateRepository.findByWorkTypeAndActive(workType, true);
        return !templates.isEmpty();
    }

    public List<WorkTemplate> getAvailableTemplates(WorkType workType) {
        return workTemplateRepository.findByWorkTypeAndActive(workType, true);
    }

    public WorkCreationResult validateWorkCreation(WorkType workType, BinLocation location, 
                                                  SkuCode item, Quantity quantity) {
        // Check if templates are available
        if (!canCreateWork(workType)) {
            return WorkCreationResult.failure("No active templates available for work type: " + workType);
        }

        // Validate location
        if (location == null || location.getLocation() == null || location.getLocation().trim().isEmpty()) {
            return WorkCreationResult.failure("Invalid location specified");
        }

        // Validate item
        if (item == null || item.getValue() == null || item.getValue().trim().isEmpty()) {
            return WorkCreationResult.failure("Invalid item specified");
        }

        // Validate quantity
        if (quantity == null || quantity.getValue() <= 0) {
            return WorkCreationResult.failure("Invalid quantity specified");
        }

        // Check for duplicate work
        if (hasDuplicateWork(workType, location, item)) {
            return WorkCreationResult.failure("Similar work already exists for this location and item");
        }

        return WorkCreationResult.success();
    }

    private WorkTemplate selectBestTemplate(WorkType workType) {
        List<WorkTemplate> templates = workTemplateRepository.findByWorkTypeAndActive(workType, true);
        
        if (templates.isEmpty()) {
            logger.warn("No active templates found for work type: {}", workType);
            return null;
        }

        // For now, return the first template. In the future, this could be more sophisticated
        // based on priority, complexity, or other business rules
        WorkTemplate selected = templates.get(0);
        logger.debug("Selected template: {} for work type: {}", selected.getName(), workType);
        
        return selected;
    }

    private boolean hasDuplicateWork(WorkType workType, BinLocation location, SkuCode item) {
        // Check for existing active work at the same location for the same item
        List<Work> activeWork = workRepository.findActiveWork();
        
        return activeWork.stream()
            .anyMatch(work -> work.getLocation().equals(location) && 
                            work.getItem().equals(item) &&
                            (work.isInProgress() || work.isAssigned()));
    }

    public static class WorkCreationResult {
        private final boolean success;
        private final String errorMessage;

        private WorkCreationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static WorkCreationResult success() {
            return new WorkCreationResult(true, null);
        }

        public static WorkCreationResult failure(String errorMessage) {
            return new WorkCreationResult(false, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}