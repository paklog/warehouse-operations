package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class QualityInspectionService {
    private static final Logger logger = LoggerFactory.getLogger(QualityInspectionService.class);
    
    private final QualityInspectionRepository inspectionRepository;

    public QualityInspectionService(QualityInspectionRepository inspectionRepository) {
        this.inspectionRepository = Objects.requireNonNull(inspectionRepository, 
                "Inspection repository cannot be null");
    }

    public QualityInspection scheduleInspection(SkuCode item, QualityInspectionType inspectionType, 
                                              BinLocation location, String inspectorId) {
        logger.info("Scheduling {} inspection for item: {} at location: {}", 
                inspectionType, item, location);
        
        QualityInspectionId inspectionId = QualityInspectionId.generate();
        
        // Create a default inspection plan - in a real system this would come from a repository
        QualityInspectionPlan defaultPlan = createDefaultInspectionPlan(inspectionType);
        
        QualityInspection inspection = new QualityInspection(
                inspectionId, inspectionType, item, 
                com.paklog.warehouse.domain.shared.Quantity.of(1), 
                location, defaultPlan, inspectorId
        );
        
        inspectionRepository.save(inspection);
        
        logger.info("Scheduled inspection: {} for item: {}", inspectionId, item);
        return inspection;
    }

    public void assignInspector(QualityInspectionId inspectionId, String inspectorId) {
        logger.info("Assigning inspector: {} to inspection: {}", inspectorId, inspectionId);
        
        QualityInspection inspection = inspectionRepository.findById(inspectionId);
        // Inspector assignment logic would be implemented here
        
        inspectionRepository.save(inspection);
    }

    public boolean validateInspectionCompletion(QualityInspection inspection) {
        logger.debug("Validating completion of inspection: {}", inspection.getInspectionId());
        
        // Check if all mandatory steps are completed
        boolean allMandatoryStepsCompleted = inspection.getMandatorySteps().stream()
                .allMatch(step -> step.isCompleted());
        
        if (!allMandatoryStepsCompleted) {
            logger.warn("Inspection {} has incomplete mandatory steps", inspection.getInspectionId());
            return false;
        }
        
        // Additional validation logic can be added here
        return true;
    }

    public QualityInspectionPlan determineInspectionPlan(SkuCode item, QualityInspectionType inspectionType) {
        logger.debug("Determining inspection plan for item: {} and type: {}", item, inspectionType);
        
        // In a real system, this would query a repository of inspection plans
        return createDefaultInspectionPlan(inspectionType);
    }

    public List<QualityInspection> findOverdueInspections() {
        logger.debug("Finding overdue inspections");
        return inspectionRepository.findOverdue();
    }

    public List<QualityInspection> findInspectionsByItem(SkuCode item) {
        logger.debug("Finding inspections for item: {}", item);
        return inspectionRepository.findByItem(item);
    }

    private QualityInspectionPlan createDefaultInspectionPlan(QualityInspectionType inspectionType) {
        // Create a simple default plan - in reality this would be more sophisticated
        List<QualityStepTemplate> stepTemplates = List.of(
                new QualityStepTemplate(
                        1, "Visual Inspection", "Check for visual defects",
                        QualityTestType.VISUAL, true, "PASS",
                        new QualityToleranceRange(0, 100, "%"), "score"
                )
        );
        
        return new QualityInspectionPlan(
                "DEFAULT-" + inspectionType.name(),
                "Default " + inspectionType.name() + " Plan",
                "Default inspection plan for " + inspectionType.name(),
                stepTemplates,
                true
        );
    }
}