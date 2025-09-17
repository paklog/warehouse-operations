package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QualityInspectionRepository {
    
    void save(QualityInspection inspection);
    
    QualityInspection findById(QualityInspectionId inspectionId);
    
    Optional<QualityInspection> findByIdOptional(QualityInspectionId inspectionId);
    
    List<QualityInspection> findByItem(SkuCode item);
    
    List<QualityInspection> findByStatus(QualityInspectionStatus status);
    
    List<QualityInspection> findByInspector(String inspectorId);
    
    List<QualityInspection> findOverdue();
    
    List<QualityInspection> findByType(QualityInspectionType type);
    
    List<QualityInspection> findActiveInspections();
    
    List<QualityInspection> findInspectionsWithNonConformances();
    
    List<QualityInspection> findByInspectorAndStatus(String inspectorId, QualityInspectionStatus status);
    
    List<QualityInspection> findByDateRange(Instant startDate, Instant endDate);
    
    long countByStatus(QualityInspectionStatus status);
    
    long countByInspector(String inspectorId);
    
    void delete(QualityInspection inspection);
    
    boolean existsById(QualityInspectionId inspectionId);
}