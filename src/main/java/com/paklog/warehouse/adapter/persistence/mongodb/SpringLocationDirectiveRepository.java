package com.paklog.warehouse.adapter.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringLocationDirectiveRepository extends MongoRepository<LocationDirectiveDocument, String> {
    
    Optional<LocationDirectiveDocument> findByDirectiveId(String directiveId);
    
    List<LocationDirectiveDocument> findByWorkType(String workType);
    
    List<LocationDirectiveDocument> findByStrategy(String strategy);
    
    @Query("{ 'workType': ?0, 'active': ?1 }")
    List<LocationDirectiveDocument> findByWorkTypeAndActive(String workType, boolean active);
    
    @Query("{ 'active': true }")
    List<LocationDirectiveDocument> findActiveDirectives();
    
    List<LocationDirectiveDocument> findByPriority(int priority);
    
    @Query("{ 'priority': { $gte: ?0, $lte: ?1 } }")
    List<LocationDirectiveDocument> findByPriorityRange(int minPriority, int maxPriority);
    
    @Query("{ 'strategy': ?0, 'active': true }")
    List<LocationDirectiveDocument> findActiveDirectivesByStrategy(String strategy);
    
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<LocationDirectiveDocument> findByNameContainingIgnoreCase(String name);
    
    @Query("{ 'createdBy': ?0 }")
    List<LocationDirectiveDocument> findByCreatedBy(String createdBy);
    
    long countByWorkType(String workType);
    
    long countByStrategy(String strategy);
    
    @Query("{ 'active': true }")
    long countActiveDirectives();
    
    @Query("{ 'workType': ?0, 'active': true }")
    long countActiveDirectivesByWorkType(String workType);
}