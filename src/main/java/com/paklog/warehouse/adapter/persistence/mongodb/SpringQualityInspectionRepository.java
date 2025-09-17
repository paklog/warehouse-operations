package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.quality.QualityInspectionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringQualityInspectionRepository extends MongoRepository<QualityInspectionDocument, String> {
    
    List<QualityInspectionDocument> findByStatus(String status);
    
    List<QualityInspectionDocument> findByInspectorId(String inspectorId);
    
    List<QualityInspectionDocument> findByItemSkuCode(String itemSkuCode);
    
    List<QualityInspectionDocument> findByInspectionType(String inspectionType);
    
    @Query("{ 'status': ?0, 'inspectorId': ?1 }")
    List<QualityInspectionDocument> findByStatusAndInspectorId(String status, String inspectorId);
    
    @Query("{ 'scheduledDate': { $lt: ?0 }, 'status': 'SCHEDULED' }")
    List<QualityInspectionDocument> findOverdueInspections(Instant currentTime);
    
    @Query("{ 'status': { $in: ['SCHEDULED', 'IN_PROGRESS', 'ON_HOLD'] } }")
    List<QualityInspectionDocument> findActiveInspections();
    
    @Query("{ 'nonConformances': { $exists: true, $not: { $size: 0 } } }")
    List<QualityInspectionDocument> findInspectionsWithNonConformances();
    
    @Query("{ 'location.aisle': ?0 }")
    List<QualityInspectionDocument> findByLocationAisle(String aisle);
    
    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
    List<QualityInspectionDocument> findByDateRange(Instant startDate, Instant endDate);
    
    long countByStatus(String status);
    
    long countByInspectorId(String inspectorId);
    
    @Query("{ 'finalDecision': ?0 }")
    List<QualityInspectionDocument> findByFinalDecision(String decision);
    
    Optional<QualityInspectionDocument> findByInspectionId(String inspectionId);
}