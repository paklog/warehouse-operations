package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.work.Work;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.List;
import java.util.Optional;

public interface QualityWorkIntegrationService {
    
    QualityInspection createInspectionForWork(Work work, String inspectorId);
    
    void assignInspectionToWork(Work work, QualityInspectionId inspectionId);
    
    void approveWorkQuality(Work work, QualityInspectionId inspectionId);
    
    void rejectWorkQuality(Work work, QualityInspectionId inspectionId, 
                          List<QualityNonConformance> nonConformances);
    
    Optional<QualityInspection> findInspectionByWork(Work work);
    
    List<Work> findWorkAwaitingQualityInspection();
    
    List<Work> findWorkByInspectionStatus(QualityInspectionStatus status);
    
    boolean canWorkBeCompleted(Work work);
    
    void createQuarantineForRejectedWork(Work work, QualityQuarantineReason reason);
    
    void createHoldForWork(Work work, QualityHoldReason reason, String notes);
    
    void releaseWorkFromHold(Work work, String releasedBy, String notes);
    
    boolean isItemUnderQualityRestriction(SkuCode item, String batchNumber);
    
    void scheduleInspectionForCompletedWork(Work work);
}