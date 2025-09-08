package com.paklog.warehouse.domain.wave;

import com.paklog.warehouse.domain.shared.OrderId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WaveRepository {
    
    /**
     * Save a wave to the repository
     */
    void save(Wave wave);
    
    /**
     * Find a wave by its ID
     */
    Optional<Wave> findById(WaveId waveId);
    
    /**
     * Find all waves with the specified status
     */
    List<Wave> findByStatus(WaveStatus status);
    
    /**
     * Find all waves planned for a specific date
     */
    List<Wave> findByPlannedDate(Instant plannedDate);
    
    /**
     * Find all waves with cutoff time before the specified time
     */
    List<Wave> findByCutoffTimeBefore(Instant cutoffTime);
    
    /**
     * Find all waves containing the specified order
     */
    Optional<Wave> findByOrderId(OrderId orderId);
    
    /**
     * Find all waves for a specific carrier
     */
    List<Wave> findByCarrier(String carrier);
    
    /**
     * Find all waves released between two dates
     */
    List<Wave> findByReleaseDateBetween(Instant start, Instant end);
    
    /**
     * Delete a wave by ID
     */
    void deleteById(WaveId waveId);
    
    /**
     * Check if a wave with the given ID exists
     */
    boolean existsById(WaveId waveId);
    
    /**
     * Count total number of waves
     */
    long count();
    
    /**
     * Count waves by status
     */
    long countByStatus(WaveStatus status);
}