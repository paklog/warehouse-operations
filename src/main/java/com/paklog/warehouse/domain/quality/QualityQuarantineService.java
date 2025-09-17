package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Location;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QualityQuarantineService {
    
    QualityQuarantine createQuarantine(SkuCode item, String batchNumber, int quantity, 
                                     QualityQuarantineReason reason, String quarantinedBy);
    
    void moveToQuarantineLocation(QualityQuarantineId quarantineId, Location quarantineLocation);
    
    void releaseFromQuarantine(QualityQuarantineId quarantineId, String releasedBy, String notes);
    
    void disposeQuarantinedItem(QualityQuarantineId quarantineId, String disposedBy, String notes);
    
    Optional<QualityQuarantine> findById(QualityQuarantineId quarantineId);
    
    List<QualityQuarantine> findActiveQuarantines();
    
    List<QualityQuarantine> findByItem(SkuCode item);
    
    List<QualityQuarantine> findByBatch(String batchNumber);
    
    List<QualityQuarantine> findExpiredQuarantines(Instant cutoffDate);
    
    boolean isItemQuarantined(SkuCode item, String batchNumber);
    
    void extendQuarantinePeriod(QualityQuarantineId quarantineId, Instant newExpiryDate, 
                               String extendedBy, String reason);
}