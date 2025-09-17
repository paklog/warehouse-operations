package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QualityHoldRepository {
    
    void save(QualityHold hold);
    
    QualityHold findById(QualityHoldId holdId);
    
    Optional<QualityHold> findByIdOptional(QualityHoldId holdId);
    
    List<QualityHold> findByItem(SkuCode item);
    
    List<QualityHold> findActiveHolds();
    
    List<QualityHold> findByReason(QualityHoldReason reason);
    
    List<QualityHold> findByStatus(QualityHoldStatus status);
    
    List<QualityHold> findByHeldBy(String heldBy);
    
    List<QualityHold> findByBatchNumber(String batchNumber);
    
    List<QualityHold> findByPriority(QualityHoldPriority priority);
    
    List<QualityHold> findByDateRange(Instant startDate, Instant endDate);
    
    List<QualityHold> findExpiredHolds(Instant currentTime);
    
    long countByStatus(QualityHoldStatus status);
    
    long countByReason(QualityHoldReason reason);
    
    void delete(QualityHold hold);
    
    boolean existsById(QualityHoldId holdId);
}