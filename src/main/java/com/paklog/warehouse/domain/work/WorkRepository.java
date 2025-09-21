package com.paklog.warehouse.domain.work;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkRepository extends MongoRepository<Work, UUID> {
    
    List<Work> findByStatus(WorkStatus status);
    
    List<Work> findByAssignedTo(String assignedTo);
    
    List<Work> findByAssignedToAndStatus(String assignedTo, WorkStatus status);
    
    List<Work> findByAssignedToAndStatusIn(String assignedTo, List<WorkStatus> statuses);
    
    List<Work> findByTemplateId(WorkTemplateId templateId);
    
    List<Work> findByWorkType(WorkType workType);
    
    List<Work> findByWorkTypeAndStatus(WorkType workType, WorkStatus status);
    
    @Query("{ 'status' : { '$in' : [ ?0, ?1 ] } }")
    List<Work> findActive(WorkStatus status1, WorkStatus status2);

    @Query("{ 'status' : { '$in' : [ 'IN_PROGRESS', 'ASSIGNED' ] } }")
    List<Work> findActiveWork();
    
    @Query("{ 'status' : ?0, 'assignedTo' : null }")
    List<Work> findAvailable(WorkStatus status);
    
    long countByStatus(WorkStatus status);
    
    long countByAssignedTo(String assignedTo);
}
