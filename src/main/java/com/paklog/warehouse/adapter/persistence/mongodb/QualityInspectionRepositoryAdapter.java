package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.quality.*;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class QualityInspectionRepositoryAdapter implements QualityInspectionRepository {
    private static final Logger logger = LoggerFactory.getLogger(QualityInspectionRepositoryAdapter.class);
    
    private final SpringQualityInspectionRepository springRepository;

    public QualityInspectionRepositoryAdapter(SpringQualityInspectionRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public void save(QualityInspection inspection) {
        logger.debug("Saving quality inspection: {}", inspection.getInspectionId());
        
        QualityInspectionDocument document = new QualityInspectionDocument(inspection);
        springRepository.save(document);
        
        logger.info("Successfully saved quality inspection: {}", inspection.getInspectionId());
    }

    @Override
    public QualityInspection findById(QualityInspectionId id) {
        logger.debug("Finding quality inspection by ID: {}", id);
        
        Optional<QualityInspectionDocument> document = springRepository.findByInspectionId(id.getValue().toString());
        
        if (document.isEmpty()) {
            logger.warn("Quality inspection not found: {}", id);
            throw new IllegalArgumentException("Quality inspection not found: " + id);
        }
        
        QualityInspection inspection = document.get().toDomain();
        logger.debug("Found quality inspection: {}", inspection.getInspectionId());
        return inspection;
    }

    @Override
    public Optional<QualityInspection> findByIdOptional(QualityInspectionId id) {
        logger.debug("Finding quality inspection by ID (optional): {}", id);
        
        return springRepository.findByInspectionId(id.getValue().toString())
                .map(QualityInspectionDocument::toDomain);
    }

    @Override
    public List<QualityInspection> findByItem(SkuCode item) {
        logger.debug("Finding quality inspections by item: {}", item);
        
        List<QualityInspectionDocument> documents = springRepository.findByItemSkuCode(item.getValue());
        
        List<QualityInspection> inspections = documents.stream()
                .map(QualityInspectionDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality inspections for item: {}", inspections.size(), item);
        return inspections;
    }

    @Override
    public List<QualityInspection> findByStatus(QualityInspectionStatus status) {
        logger.debug("Finding quality inspections by status: {}", status);
        
        List<QualityInspectionDocument> documents = springRepository.findByStatus(status.name());
        
        List<QualityInspection> inspections = documents.stream()
                .map(QualityInspectionDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality inspections with status: {}", inspections.size(), status);
        return inspections;
    }

    @Override
    public List<QualityInspection> findByInspector(String inspectorId) {
        logger.debug("Finding quality inspections by inspector: {}", inspectorId);
        
        List<QualityInspectionDocument> documents = springRepository.findByInspectorId(inspectorId);
        
        List<QualityInspection> inspections = documents.stream()
                .map(QualityInspectionDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality inspections for inspector: {}", inspections.size(), inspectorId);
        return inspections;
    }

    @Override
    public List<QualityInspection> findOverdue() {
        logger.debug("Finding overdue quality inspections");
        
        Instant currentTime = Instant.now();
        List<QualityInspectionDocument> documents = springRepository.findOverdueInspections(currentTime);
        
        List<QualityInspection> inspections = documents.stream()
                .map(QualityInspectionDocument::toDomain)
                .toList();
        
        logger.debug("Found {} overdue quality inspections", inspections.size());
        return inspections;
    }

    @Override
    public List<QualityInspection> findByType(QualityInspectionType type) {
        logger.debug("Finding quality inspections by type: {}", type);
        
        List<QualityInspectionDocument> documents = springRepository.findByInspectionType(type.name());
        
        List<QualityInspection> inspections = documents.stream()
                .map(QualityInspectionDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality inspections of type: {}", inspections.size(), type);
        return inspections;
    }

    @Override
    public List<QualityInspection> findActiveInspections() {
        logger.debug("Finding active quality inspections");
        
        List<QualityInspectionDocument> documents = springRepository.findActiveInspections();
        
        List<QualityInspection> inspections = documents.stream()
                .map(QualityInspectionDocument::toDomain)
                .toList();
        
        logger.debug("Found {} active quality inspections", inspections.size());
        return inspections;
    }

    @Override
    public List<QualityInspection> findInspectionsWithNonConformances() {
        logger.debug("Finding quality inspections with non-conformances");
        
        List<QualityInspectionDocument> documents = springRepository.findInspectionsWithNonConformances();
        
        List<QualityInspection> inspections = documents.stream()
                .map(QualityInspectionDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality inspections with non-conformances", inspections.size());
        return inspections;
    }

    @Override
    public List<QualityInspection> findByInspectorAndStatus(String inspectorId, QualityInspectionStatus status) {
        logger.debug("Finding quality inspections by inspector: {} and status: {}", inspectorId, status);
        
        List<QualityInspectionDocument> documents = springRepository.findByStatusAndInspectorId(
                status.name(), inspectorId);
        
        List<QualityInspection> inspections = documents.stream()
                .map(QualityInspectionDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality inspections for inspector: {} with status: {}", 
                inspections.size(), inspectorId, status);
        return inspections;
    }

    @Override
    public List<QualityInspection> findByDateRange(Instant startDate, Instant endDate) {
        logger.debug("Finding quality inspections between {} and {}", startDate, endDate);
        
        List<QualityInspectionDocument> documents = springRepository.findByDateRange(startDate, endDate);
        
        List<QualityInspection> inspections = documents.stream()
                .map(QualityInspectionDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality inspections in date range", inspections.size());
        return inspections;
    }

    @Override
    public long countByStatus(QualityInspectionStatus status) {
        logger.debug("Counting quality inspections by status: {}", status);
        
        long count = springRepository.countByStatus(status.name());
        
        logger.debug("Found {} quality inspections with status: {}", count, status);
        return count;
    }

    @Override
    public long countByInspector(String inspectorId) {
        logger.debug("Counting quality inspections by inspector: {}", inspectorId);
        
        long count = springRepository.countByInspectorId(inspectorId);
        
        logger.debug("Found {} quality inspections for inspector: {}", count, inspectorId);
        return count;
    }

    @Override
    public void delete(QualityInspection inspection) {
        logger.debug("Deleting quality inspection: {}", inspection.getInspectionId());
        
        springRepository.deleteById(inspection.getInspectionId().getValue().toString());
        
        logger.info("Successfully deleted quality inspection: {}", inspection.getInspectionId());
    }

    @Override
    public boolean existsById(QualityInspectionId id) {
        logger.debug("Checking if quality inspection exists: {}", id);
        
        boolean exists = springRepository.existsById(id.getValue().toString());
        
        logger.debug("Quality inspection {} exists: {}", id, exists);
        return exists;
    }
}