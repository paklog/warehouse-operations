package com.paklog.warehouse.domain.work;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkRepository {
    Work save(Work work);
    
    Optional<Work> findById(UUID workId);
    
    List<Work> findByStatus(WorkStatus status);
    
    List<Work> findByAssignedTo(String assignedTo);
    
    List<Work> findByAssignedToAndStatus(String assignedTo, WorkStatus status);
    
    List<Work> findByAssignedToAndStatusIn(String assignedTo, List<WorkStatus> statuses);
    
    List<Work> findByTemplateId(WorkTemplateId templateId);
    
    List<Work> findByWorkType(WorkType workType);
    
    List<Work> findByWorkTypeAndStatus(WorkType workType, WorkStatus status);
    
    List<Work> findActiveWork();
    
    List<Work> findAvailableWork();
    
    void delete(Work work);
    
    List<Work> findAll();
    
    long countByStatus(WorkStatus status);
    
    long countByAssignedTo(String assignedTo);
}