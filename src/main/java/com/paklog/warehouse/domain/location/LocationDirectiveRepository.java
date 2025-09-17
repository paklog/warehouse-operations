package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.work.WorkType;

import java.util.List;
import java.util.Optional;

public interface LocationDirectiveRepository {
    void save(LocationDirective directive);
    
    LocationDirective findById(LocationDirectiveId id);
    
    Optional<LocationDirective> findByIdOptional(LocationDirectiveId id);
    
    List<LocationDirective> findByWorkType(WorkType workType);
    
    List<LocationDirective> findByWorkTypeAndActive(WorkType workType, boolean active);
    
    List<LocationDirective> findByStrategy(LocationStrategy strategy);
    
    List<LocationDirective> findActiveDirectives();
    
    List<LocationDirective> findByPriority(int priority);
    
    List<LocationDirective> findByPriorityRange(int minPriority, int maxPriority);
    
    List<LocationDirective> findActiveDirectivesByStrategy(LocationStrategy strategy);
    
    List<LocationDirective> findByNameContaining(String name);
    
    List<LocationDirective> findByCreatedBy(String createdBy);
    
    long countByWorkType(WorkType workType);
    
    long countByStrategy(LocationStrategy strategy);
    
    long countActiveDirectives();
    
    long countActiveDirectivesByWorkType(WorkType workType);
    
    void delete(LocationDirective directive);
    
    boolean existsById(LocationDirectiveId directiveId);
}