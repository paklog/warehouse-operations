package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.picklist.PickListStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringPickListRepository extends MongoRepository<PickListDocument, String> {

    // Basic queries using compound indexes
    Optional<PickListDocument> findByOrderId(String orderId);
    List<PickListDocument> findByStatus(PickListStatus status);
    Page<PickListDocument> findByStatus(PickListStatus status, Pageable pageable);
    
    // Picker-focused queries using compound indexes
    List<PickListDocument> findByAssignedPickerId(String pickerId);
    List<PickListDocument> findByStatusAndAssignedPickerId(PickListStatus status, String pickerId);
    List<PickListDocument> findByAssignedPickerIdOrderByPriorityDesc(String pickerId);
    
    // Priority-based queries for pick optimization
    List<PickListDocument> findByStatusOrderByPriorityDescCreatedAtAsc(PickListStatus status);
    List<PickListDocument> findByStatusAndPriorityGreaterThanOrderByPriorityDesc(PickListStatus status, Integer priority);
    
    // Zone-based queries for warehouse efficiency
    List<PickListDocument> findByWarehouseZoneAndStatus(String warehouseZone, PickListStatus status);
    List<PickListDocument> findByWarehouseZoneAndStatusOrderByPriorityDesc(String warehouseZone, PickListStatus status);
    
    // Time-based operational queries
    List<PickListDocument> findByStatusAndCreatedAtBetween(PickListStatus status, Date start, Date end);
    List<PickListDocument> findByCreatedAtBetweenOrderByPriorityDesc(Date start, Date end);
    
    // Complex operational queries
    @Query("{ 'status': ?0, 'priority': { $gte: ?1 }, 'warehouseZone': ?2 }")
    List<PickListDocument> findHighPriorityPickListsInZone(PickListStatus status, Integer minPriority, String zone);
    
    @Query("{ 'status': { $in: ?0 }, 'assignedPickerId': ?1 }")
    List<PickListDocument> findPickListsInStatusesForPicker(List<PickListStatus> statuses, String pickerId);
    
    // Performance analytics queries
    @Query("{ 'completedAt': { $gte: ?0, $lte: ?1 }, 'allInstructionsCompleted': true }")
    List<PickListDocument> findCompletedPickListsInPeriod(Date start, Date end);
    
    // Count queries for metrics
    long countByStatus(PickListStatus status);
    long countByStatusAndAssignedPickerId(PickListStatus status, String pickerId);
    long countByWarehouseZoneAndStatus(String warehouseZone, PickListStatus status);
    long countByStatusAndPriorityGreaterThan(PickListStatus status, Integer priority);
    
    // Projection queries for dashboard summaries
    @Query(value = "{ 'assignedPickerId': ?0, 'status': { $in: ?1 } }", 
           fields = "{ 'id': 1, 'orderId': 1, 'status': 1, 'priority': 1, 'totalInstructions': 1, 'completedInstructions': 1 }")
    List<PickListDocument> findPickerWorkSummary(String pickerId, List<PickListStatus> statuses);
    
    @Query(value = "{ 'warehouseZone': ?0, 'status': ?1 }", 
           fields = "{ 'id': 1, 'assignedPickerId': 1, 'priority': 1, 'estimatedPickTimeMinutes': 1 }")
    List<PickListDocument> findZoneWorkloadSummary(String warehouseZone, PickListStatus status);
    
    // Aggregation queries for advanced analytics
    @Aggregation(pipeline = {
        "{ $match: { 'status': 'COMPLETED', 'completedAt': { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { _id: '$assignedPickerId', totalCompleted: { $sum: 1 }, avgPickTime: { $avg: '$estimatedPickTimeMinutes' } } }",
        "{ $sort: { totalCompleted: -1 } }"
    })
    List<PickerPerformanceSummary> getPickerPerformanceInPeriod(Date start, Date end);
    
    @Aggregation(pipeline = {
        "{ $match: { 'status': ?0 } }",
        "{ $group: { _id: '$warehouseZone', totalPickLists: { $sum: 1 }, avgPriority: { $avg: '$priority' } } }",
        "{ $sort: { totalPickLists: -1 } }"
    })
    List<ZoneWorkloadSummary> getZoneWorkloadSummary(PickListStatus status);
    
    // Bulk operations support
    @Query("{ 'status': 'PENDING', 'priority': { $gte: ?0 }, 'warehouseZone': ?1 }")
    List<PickListDocument> findPickListsForAutoAssignment(Integer minPriority, String zone);
    
    // Interfaces for aggregation results
    interface PickerPerformanceSummary {
        String getId(); // picker ID
        int getTotalCompleted();
        Double getAvgPickTime();
    }
    
    interface ZoneWorkloadSummary {
        String getId(); // zone name
        int getTotalPickLists();
        Double getAvgPriority();
    }
}