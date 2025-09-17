package com.paklog.warehouse.domain.work;

import java.util.List;
import java.util.Optional;

public interface WorkTemplateRepository {
    WorkTemplate save(WorkTemplate workTemplate);
    
    Optional<WorkTemplate> findById(WorkTemplateId id);
    
    List<WorkTemplate> findByWorkType(WorkType workType);
    
    List<WorkTemplate> findActiveTemplates();
    
    List<WorkTemplate> findByWorkTypeAndActive(WorkType workType, boolean active);
    
    Optional<WorkTemplate> findByName(String name);
    
    boolean existsByName(String name);
    
    void delete(WorkTemplate workTemplate);
    
    List<WorkTemplate> findAll();
    
    long countByWorkType(WorkType workType);
    
    long countActiveTemplates();
}