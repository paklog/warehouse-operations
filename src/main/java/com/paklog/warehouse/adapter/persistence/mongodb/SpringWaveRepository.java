package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.wave.WaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringWaveRepository extends MongoRepository<WaveDocument, String> {

    // Basic queries using compound indexes
    List<WaveDocument> findByStatus(WaveStatus status);
    List<WaveDocument> findByStatusOrderByCreatedAtDesc(WaveStatus status);
    Page<WaveDocument> findByStatus(WaveStatus status, Pageable pageable);
    
    // Compound index queries for better performance
    List<WaveDocument> findByStatusAndCarrier(WaveStatus status, String carrier);
    List<WaveDocument> findByStatusAndPlannedDateBetween(WaveStatus status, Date start, Date end);
    List<WaveDocument> findByCarrierAndStatus(String carrier, WaveStatus status);
    List<WaveDocument> findByCarrier(String carrier);
    
    // Date range queries
    List<WaveDocument> findByPlannedDateBetween(Date start, Date end);
    List<WaveDocument> findByCutoffTimeBefore(Date cutoffTime);
    List<WaveDocument> findByReleaseDateBetween(Date start, Date end);
    
    // Optimized array query with multikey index
    @Query("{ 'orderIds': { $in: [?0] } }")
    Optional<WaveDocument> findByOrderId(String orderId);
    
    // Complex queries for operational efficiency
    @Query("{ 'cutoffTime': { $lt: ?0 }, 'status': ?1 }")
    List<WaveDocument> findByCutoffTimeBeforeAndStatus(Date cutoffTime, WaveStatus status);
    
    @Query("{ 'releaseDate': { $gte: ?0, $lte: ?1 }, 'status': { $in: ?2 } }")
    List<WaveDocument> findByReleaseDateBetweenAndStatusIn(Date start, Date end, List<WaveStatus> statuses);
    
    // Count queries for analytics - using compound indexes
    long countByStatus(WaveStatus status);
    long countByStatusAndCarrier(WaveStatus status, String carrier);
    long countByStatusAndPlannedDateBetween(WaveStatus status, Date start, Date end);
    
    // Projection queries for performance (only fetch needed fields)
    @Query(value = "{ 'status': ?0 }", fields = "{ 'id': 1, 'status': 1, 'orderIds': 1, 'totalOrders': 1 }")
    List<WaveDocument> findBasicInfoByStatus(WaveStatus status);
    
    @Query(value = "{ 'carrier': ?0, 'status': ?1 }", fields = "{ 'id': 1, 'carrier': 1, 'totalOrders': 1, 'createdAt': 1 }")
    List<WaveDocument> findCarrierWavesSummary(String carrier, WaveStatus status);
    
    // Text search (requires text index on relevant fields)
    @Query("{ '$text': { '$search': ?0 } }")
    List<WaveDocument> findByTextSearch(String searchText);
    
    // Aggregation queries for complex analytics
    @Aggregation(pipeline = {
        "{ $match: { 'status': ?0 } }",
        "{ $group: { _id: '$carrier', totalWaves: { $sum: 1 }, totalOrders: { $sum: '$totalOrders' } } }",
        "{ $sort: { totalWaves: -1 } }"
    })
    List<CarrierWaveSummary> getWavesSummaryByCarrier(WaveStatus status);
    
    // Bulk operations support
    @Query("{ 'status': ?0, 'cutoffTime': { $lt: ?1 } }")
    List<WaveDocument> findWavesReadyForRelease(WaveStatus status, Date cutoffTime);
    
    // Interface for aggregation results
    interface CarrierWaveSummary {
        String getId(); // carrier name
        int getTotalWaves();
        int getTotalOrders();
    }
}