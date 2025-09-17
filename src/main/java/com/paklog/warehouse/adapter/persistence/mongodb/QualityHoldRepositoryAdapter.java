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
public class QualityHoldRepositoryAdapter implements QualityHoldRepository {
    private static final Logger logger = LoggerFactory.getLogger(QualityHoldRepositoryAdapter.class);
    
    private final SpringQualityHoldRepository springRepository;

    public QualityHoldRepositoryAdapter(SpringQualityHoldRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public void save(QualityHold hold) {
        logger.debug("Saving quality hold: {}", hold.getHoldId());
        
        QualityHoldDocument document = new QualityHoldDocument(hold);
        springRepository.save(document);
        
        logger.info("Successfully saved quality hold: {}", hold.getHoldId());
    }

    @Override
    public QualityHold findById(QualityHoldId holdId) {
        logger.debug("Finding quality hold by ID: {}", holdId);
        
        Optional<QualityHoldDocument> document = springRepository.findByHoldId(holdId.getValue().toString());
        
        if (document.isEmpty()) {
            logger.warn("Quality hold not found: {}", holdId);
            throw new IllegalArgumentException("Quality hold not found: " + holdId);
        }
        
        QualityHold hold = document.get().toDomain();
        logger.debug("Found quality hold: {}", hold.getHoldId());
        return hold;
    }

    @Override
    public Optional<QualityHold> findByIdOptional(QualityHoldId holdId) {
        logger.debug("Finding quality hold by ID (optional): {}", holdId);
        
        return springRepository.findByHoldId(holdId.getValue().toString())
                .map(QualityHoldDocument::toDomain);
    }

    @Override
    public List<QualityHold> findByItem(SkuCode item) {
        logger.debug("Finding quality holds by item: {}", item);
        
        List<QualityHoldDocument> documents = springRepository.findByItemSkuCode(item.getValue());
        
        List<QualityHold> holds = documents.stream()
                .map(QualityHoldDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality holds for item: {}", holds.size(), item);
        return holds;
    }

    @Override
    public List<QualityHold> findActiveHolds() {
        logger.debug("Finding active quality holds");
        
        List<QualityHoldDocument> documents = springRepository.findActiveHolds();
        
        List<QualityHold> holds = documents.stream()
                .map(QualityHoldDocument::toDomain)
                .toList();
        
        logger.debug("Found {} active quality holds", holds.size());
        return holds;
    }

    @Override
    public List<QualityHold> findByReason(QualityHoldReason reason) {
        logger.debug("Finding quality holds by reason: {}", reason);
        
        List<QualityHoldDocument> documents = springRepository.findByReason(reason.name());
        
        List<QualityHold> holds = documents.stream()
                .map(QualityHoldDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality holds with reason: {}", holds.size(), reason);
        return holds;
    }

    @Override
    public List<QualityHold> findByStatus(QualityHoldStatus status) {
        logger.debug("Finding quality holds by status: {}", status);
        
        List<QualityHoldDocument> documents = springRepository.findByStatus(status.name());
        
        List<QualityHold> holds = documents.stream()
                .map(QualityHoldDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality holds with status: {}", holds.size(), status);
        return holds;
    }

    @Override
    public List<QualityHold> findByHeldBy(String heldBy) {
        logger.debug("Finding quality holds by held by: {}", heldBy);
        
        List<QualityHoldDocument> documents = springRepository.findByHeldBy(heldBy);
        
        List<QualityHold> holds = documents.stream()
                .map(QualityHoldDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality holds held by: {}", holds.size(), heldBy);
        return holds;
    }

    @Override
    public List<QualityHold> findByBatchNumber(String batchNumber) {
        logger.debug("Finding quality holds by batch number: {}", batchNumber);
        
        List<QualityHoldDocument> documents = springRepository.findByBatchNumber(batchNumber);
        
        List<QualityHold> holds = documents.stream()
                .map(QualityHoldDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality holds for batch: {}", holds.size(), batchNumber);
        return holds;
    }

    @Override
    public List<QualityHold> findByPriority(QualityHoldPriority priority) {
        logger.debug("Finding quality holds by priority: {}", priority);
        
        List<QualityHoldDocument> documents = springRepository.findByPriority(priority.name());
        
        List<QualityHold> holds = documents.stream()
                .map(QualityHoldDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality holds with priority: {}", holds.size(), priority);
        return holds;
    }

    @Override
    public List<QualityHold> findByDateRange(Instant startDate, Instant endDate) {
        logger.debug("Finding quality holds between {} and {}", startDate, endDate);
        
        List<QualityHoldDocument> documents = springRepository.findByDateRange(startDate, endDate);
        
        List<QualityHold> holds = documents.stream()
                .map(QualityHoldDocument::toDomain)
                .toList();
        
        logger.debug("Found {} quality holds in date range", holds.size());
        return holds;
    }

    @Override
    public List<QualityHold> findExpiredHolds(Instant currentTime) {
        logger.debug("Finding expired quality holds as of: {}", currentTime);
        
        List<QualityHoldDocument> documents = springRepository.findExpiredHolds(currentTime);
        
        List<QualityHold> holds = documents.stream()
                .map(QualityHoldDocument::toDomain)
                .toList();
        
        logger.debug("Found {} expired quality holds", holds.size());
        return holds;
    }

    @Override
    public long countByStatus(QualityHoldStatus status) {
        logger.debug("Counting quality holds by status: {}", status);
        
        long count = springRepository.countByStatus(status.name());
        
        logger.debug("Found {} quality holds with status: {}", count, status);
        return count;
    }

    @Override
    public long countByReason(QualityHoldReason reason) {
        logger.debug("Counting quality holds by reason: {}", reason);
        
        long count = springRepository.countByReason(reason.name());
        
        logger.debug("Found {} quality holds with reason: {}", count, reason);
        return count;
    }

    @Override
    public void delete(QualityHold hold) {
        logger.debug("Deleting quality hold: {}", hold.getHoldId());
        
        springRepository.deleteById(hold.getHoldId().getValue().toString());
        
        logger.info("Successfully deleted quality hold: {}", hold.getHoldId());
    }

    @Override
    public boolean existsById(QualityHoldId holdId) {
        logger.debug("Checking if quality hold exists: {}", holdId);
        
        boolean exists = springRepository.existsById(holdId.getValue().toString());
        
        logger.debug("Quality hold {} exists: {}", holdId, exists);
        return exists;
    }
}