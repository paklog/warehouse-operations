package com.paklog.warehouse.adapter.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringQualityHoldRepository extends MongoRepository<QualityHoldDocument, String> {
    
    Optional<QualityHoldDocument> findByHoldId(String holdId);
    
    List<QualityHoldDocument> findByItemSkuCode(String itemSkuCode);
    
    List<QualityHoldDocument> findByStatus(String status);
    
    List<QualityHoldDocument> findByReason(String reason);
    
    List<QualityHoldDocument> findByHeldBy(String heldBy);
    
    List<QualityHoldDocument> findByBatchNumber(String batchNumber);
    
    List<QualityHoldDocument> findByPriority(String priority);
    
    @Query("{ 'status': 'ACTIVE' }")
    List<QualityHoldDocument> findActiveHolds();
    
    @Query("{ 'heldAt': { $gte: ?0, $lte: ?1 } }")
    List<QualityHoldDocument> findByDateRange(Instant startDate, Instant endDate);
    
    @Query("{ 'status': 'ACTIVE', 'heldAt': { $lt: ?0 } }")
    List<QualityHoldDocument> findExpiredHolds(Instant currentTime);
    
    @Query("{ 'status': ?0, 'priority': ?1 }")
    List<QualityHoldDocument> findByStatusAndPriority(String status, String priority);
    
    long countByStatus(String status);
    
    long countByReason(String reason);
    
    @Query("{ 'itemSkuCode': ?0, 'status': 'ACTIVE' }")
    List<QualityHoldDocument> findActiveHoldsByItem(String itemSkuCode);
    
    @Query("{ 'reason': ?0, 'status': 'ACTIVE' }")
    List<QualityHoldDocument> findActiveHoldsByReason(String reason);
}