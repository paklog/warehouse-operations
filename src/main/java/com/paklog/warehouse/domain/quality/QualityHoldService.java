package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QualityHoldService {
    
    QualityHold createHold(SkuCode item, String batchNumber, int quantity, 
                          QualityHoldReason reason, String heldBy, String notes);
    
    void releaseHold(QualityHoldId holdId, String releasedBy, String notes);
    
    void escalateHold(QualityHoldId holdId, QualityHoldReason newReason, String escalatedBy);
    
    Optional<QualityHold> findById(QualityHoldId holdId);
    
    List<QualityHold> findActiveHolds();
    
    List<QualityHold> findByItem(SkuCode item);
    
    List<QualityHold> findByBatch(String batchNumber);
    
    List<QualityHold> findByReason(QualityHoldReason reason);
    
    List<QualityHold> findOverdueHolds(Instant cutoffDate);
    
    boolean isItemOnHold(SkuCode item, String batchNumber);
    
    void addHoldNote(QualityHoldId holdId, String note, String addedBy);
}