package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.location.*;
import com.paklog.warehouse.domain.work.WorkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LocationDirectiveRepositoryAdapter implements LocationDirectiveRepository {
    private static final Logger logger = LoggerFactory.getLogger(LocationDirectiveRepositoryAdapter.class);
    
    private final SpringLocationDirectiveRepository springRepository;

    public LocationDirectiveRepositoryAdapter(SpringLocationDirectiveRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public void save(LocationDirective directive) {
        logger.debug("Saving location directive: {}", directive.getId());
        
        LocationDirectiveDocument document = new LocationDirectiveDocument(directive);
        springRepository.save(document);
        
        logger.info("Successfully saved location directive: {}", directive.getId());
    }

    @Override
    public LocationDirective findById(LocationDirectiveId directiveId) {
        logger.debug("Finding location directive by ID: {}", directiveId);
        
        Optional<LocationDirectiveDocument> document = springRepository.findByDirectiveId(
                directiveId.getValue().toString());
        
        if (document.isEmpty()) {
            logger.warn("Location directive not found: {}", directiveId);
            throw new IllegalArgumentException("Location directive not found: " + directiveId);
        }
        
        LocationDirective directive = document.get().toDomain();
        logger.debug("Found location directive: {}", directive.getId());
        return directive;
    }

    @Override
    public Optional<LocationDirective> findByIdOptional(LocationDirectiveId directiveId) {
        logger.debug("Finding location directive by ID (optional): {}", directiveId);
        
        return springRepository.findByDirectiveId(directiveId.getValue().toString())
                .map(LocationDirectiveDocument::toDomain);
    }

    @Override
    public List<LocationDirective> findByWorkType(WorkType workType) {
        logger.debug("Finding location directives by work type: {}", workType);
        
        List<LocationDirectiveDocument> documents = springRepository.findByWorkType(workType.name());
        
        List<LocationDirective> directives = documents.stream()
                .map(LocationDirectiveDocument::toDomain)
                .toList();
        
        logger.debug("Found {} location directives for work type: {}", directives.size(), workType);
        return directives;
    }

    @Override
    public List<LocationDirective> findByWorkTypeAndActive(WorkType workType, boolean active) {
        logger.debug("Finding location directives by work type: {} and active: {}", workType, active);
        
        List<LocationDirectiveDocument> documents = springRepository.findByWorkTypeAndActive(
                workType.name(), active);
        
        List<LocationDirective> directives = documents.stream()
                .map(LocationDirectiveDocument::toDomain)
                .toList();
        
        logger.debug("Found {} location directives for work type: {} and active: {}", 
                directives.size(), workType, active);
        return directives;
    }

    @Override
    public List<LocationDirective> findByStrategy(LocationStrategy strategy) {
        logger.debug("Finding location directives by strategy: {}", strategy);
        
        List<LocationDirectiveDocument> documents = springRepository.findByStrategy(strategy.name());
        
        List<LocationDirective> directives = documents.stream()
                .map(LocationDirectiveDocument::toDomain)
                .toList();
        
        logger.debug("Found {} location directives with strategy: {}", directives.size(), strategy);
        return directives;
    }

    @Override
    public List<LocationDirective> findActiveDirectives() {
        logger.debug("Finding active location directives");
        
        List<LocationDirectiveDocument> documents = springRepository.findActiveDirectives();
        
        List<LocationDirective> directives = documents.stream()
                .map(LocationDirectiveDocument::toDomain)
                .toList();
        
        logger.debug("Found {} active location directives", directives.size());
        return directives;
    }

    @Override
    public List<LocationDirective> findByPriority(int priority) {
        logger.debug("Finding location directives by priority: {}", priority);
        
        List<LocationDirectiveDocument> documents = springRepository.findByPriority(priority);
        
        List<LocationDirective> directives = documents.stream()
                .map(LocationDirectiveDocument::toDomain)
                .toList();
        
        logger.debug("Found {} location directives with priority: {}", directives.size(), priority);
        return directives;
    }

    @Override
    public List<LocationDirective> findByPriorityRange(int minPriority, int maxPriority) {
        logger.debug("Finding location directives by priority range: {} - {}", minPriority, maxPriority);
        
        List<LocationDirectiveDocument> documents = springRepository.findByPriorityRange(
                minPriority, maxPriority);
        
        List<LocationDirective> directives = documents.stream()
                .map(LocationDirectiveDocument::toDomain)
                .toList();
        
        logger.debug("Found {} location directives in priority range: {} - {}", 
                directives.size(), minPriority, maxPriority);
        return directives;
    }

    @Override
    public List<LocationDirective> findActiveDirectivesByStrategy(LocationStrategy strategy) {
        logger.debug("Finding active location directives by strategy: {}", strategy);
        
        List<LocationDirectiveDocument> documents = springRepository.findActiveDirectivesByStrategy(
                strategy.name());
        
        List<LocationDirective> directives = documents.stream()
                .map(LocationDirectiveDocument::toDomain)
                .toList();
        
        logger.debug("Found {} active location directives with strategy: {}", 
                directives.size(), strategy);
        return directives;
    }

    @Override
    public List<LocationDirective> findByNameContaining(String name) {
        logger.debug("Finding location directives by name containing: {}", name);
        
        List<LocationDirectiveDocument> documents = springRepository.findByNameContainingIgnoreCase(name);
        
        List<LocationDirective> directives = documents.stream()
                .map(LocationDirectiveDocument::toDomain)
                .toList();
        
        logger.debug("Found {} location directives with name containing: {}", directives.size(), name);
        return directives;
    }

    @Override
    public List<LocationDirective> findByCreatedBy(String createdBy) {
        logger.debug("Finding location directives by created by: {}", createdBy);
        
        List<LocationDirectiveDocument> documents = springRepository.findByCreatedBy(createdBy);
        
        List<LocationDirective> directives = documents.stream()
                .map(LocationDirectiveDocument::toDomain)
                .toList();
        
        logger.debug("Found {} location directives created by: {}", directives.size(), createdBy);
        return directives;
    }

    @Override
    public long countByWorkType(WorkType workType) {
        logger.debug("Counting location directives by work type: {}", workType);
        
        long count = springRepository.countByWorkType(workType.name());
        
        logger.debug("Found {} location directives for work type: {}", count, workType);
        return count;
    }

    @Override
    public long countByStrategy(LocationStrategy strategy) {
        logger.debug("Counting location directives by strategy: {}", strategy);
        
        long count = springRepository.countByStrategy(strategy.name());
        
        logger.debug("Found {} location directives with strategy: {}", count, strategy);
        return count;
    }

    @Override
    public long countActiveDirectives() {
        logger.debug("Counting active location directives");
        
        long count = springRepository.countActiveDirectives();
        
        logger.debug("Found {} active location directives", count);
        return count;
    }

    @Override
    public long countActiveDirectivesByWorkType(WorkType workType) {
        logger.debug("Counting active location directives by work type: {}", workType);
        
        long count = springRepository.countActiveDirectivesByWorkType(workType.name());
        
        logger.debug("Found {} active location directives for work type: {}", count, workType);
        return count;
    }

    @Override
    public void delete(LocationDirective directive) {
        logger.debug("Deleting location directive: {}", directive.getId());
        
        springRepository.deleteById(directive.getId().getValue().toString());
        
        logger.info("Successfully deleted location directive: {}", directive.getId());
    }

    @Override
    public boolean existsById(LocationDirectiveId directiveId) {
        logger.debug("Checking if location directive exists: {}", directiveId);
        
        boolean exists = springRepository.existsById(directiveId.getValue().toString());
        
        logger.debug("Location directive {} exists: {}", directiveId, exists);
        return exists;
    }
}