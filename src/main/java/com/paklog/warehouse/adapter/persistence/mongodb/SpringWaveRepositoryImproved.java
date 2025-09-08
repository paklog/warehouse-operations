package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.wave.WaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringWaveRepositoryImproved extends MongoRepository<WaveDocument, String> {

    // Single field indexes
    List<WaveDocument> findByStatus(WaveStatus status);
    
    // Compound queries for better performance
    List<WaveDocument> findByStatusAndCarrier(WaveStatus status, String carrier);
    
    List<WaveDocument> findByStatusAndPlannedDateBetween(WaveStatus status, Instant start, Instant end);
    
    // Paginated queries for large result sets
    Page<WaveDocument> findByStatus(WaveStatus status, Pageable pageable);
    
    // Optimized array query with multikey index
    @Query("{ 'orderIds': { $in: [?0] } }")
    Optional<WaveDocument> findByOrderId(String orderId);
    
    // Range queries with proper indexing
    @Query("{ 'cutoffTime': { $lt: ?0 }, 'status': ?1 }")
    List<WaveDocument> findByCutoffTimeBeforeAndStatus(Instant cutoffTime, WaveStatus status);
    
    // Aggregation-friendly queries
    @Query("{ 'releaseDate': { $gte: ?0, $lte: ?1 }, 'status': { $in: [?2] } }")
    List<WaveDocument> findByReleaseDateBetweenAndStatusIn(Instant start, Instant end, List<WaveStatus> statuses);
    
    // Count queries for analytics
    long countByStatusAndCarrier(WaveStatus status, String carrier);
    
    // Text search if needed
    @Query("{ '$text': { '$search': ?0 } }")
    List<WaveDocument> findByTextSearch(String searchText);
    
    // Projection queries for performance
    @Query(value = "{ 'status': ?0 }", fields = "{ 'id': 1, 'status': 1, 'orderIds': 1 }")
    List<WaveDocument> findStatusAndOrderIdsByStatus(WaveStatus status);
}