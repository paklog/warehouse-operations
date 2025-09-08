package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.packaging.PackageStatus;
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
public interface SpringPackageRepository extends MongoRepository<PackageDocument, String> {

    // Basic queries using compound indexes
    Optional<PackageDocument> findByOrderId(String orderId);
    List<PackageDocument> findByStatus(PackageStatus status);
    Page<PackageDocument> findByStatus(PackageStatus status, Pageable pageable);
    
    // Carrier-focused queries using compound indexes
    List<PackageDocument> findByCarrierAndStatus(String carrier, PackageStatus status);
    List<PackageDocument> findByCarrier(String carrier);
    List<PackageDocument> findByStatusOrderByCreatedAtDesc(PackageStatus status);
    
    // Tracking and shipping queries
    Optional<PackageDocument> findByTrackingNumber(String trackingNumber);
    List<PackageDocument> findByCarrierAndShippedAtBetween(String carrier, Date start, Date end);
    List<PackageDocument> findByStatusAndShippedAtIsNull(PackageStatus status);
    
    // Package characteristics queries
    List<PackageDocument> findByPackageTypeAndStatus(String packageType, PackageStatus status);
    List<PackageDocument> findByPackageSizeAndStatus(String packageSize, PackageStatus status);
    
    // Weight and volume range queries for logistics
    @Query("{ 'totalWeight': { $gte: ?0, $lte: ?1 }, 'status': ?2 }")
    List<PackageDocument> findByWeightRangeAndStatus(double minWeight, double maxWeight, PackageStatus status);
    
    @Query("{ 'totalVolume': { $gte: ?0, $lte: ?1 }, 'status': ?2 }")
    List<PackageDocument> findByVolumeRangeAndStatus(double minVolume, double maxVolume, PackageStatus status);
    
    // Time-based operational queries
    List<PackageDocument> findByStatusAndCreatedAtBetween(PackageStatus status, Date start, Date end);
    List<PackageDocument> findByPackedAtBetween(Date start, Date end);
    List<PackageDocument> findByDeliveredAtBetween(Date start, Date end);
    
    // Complex operational queries for shipping optimization
    @Query("{ 'status': ?0, 'carrier': ?1, 'totalWeight': { $lte: ?2 } }")
    List<PackageDocument> findPackagesReadyForShipment(PackageStatus status, String carrier, double maxWeight);
    
    @Query("{ 'status': { $in: ?0 }, 'shippedAt': { $gte: ?1 } }")
    List<PackageDocument> findRecentlyShippedPackages(List<PackageStatus> statuses, Date since);
    
    @Query("{ 'shippedAt': { $lt: ?0 }, 'deliveredAt': null, 'status': 'SHIPPED' }")
    List<PackageDocument> findOverdueDeliveries(Date overdueThreshold);
    
    // Count queries for metrics and reporting
    long countByStatus(PackageStatus status);
    long countByCarrierAndStatus(String carrier, PackageStatus status);
    long countByStatusAndCreatedAtBetween(PackageStatus status, Date start, Date end);
    long countByCarrierAndShippedAtBetween(String carrier, Date start, Date end);
    
    // Projection queries for dashboard summaries
    @Query(value = "{ 'carrier': ?0, 'status': { $in: ?1 } }", 
           fields = "{ 'id': 1, 'orderId': 1, 'status': 1, 'totalWeight': 1, 'totalVolume': 1, 'createdAt': 1 }")
    List<PackageDocument> findCarrierPackageSummary(String carrier, List<PackageStatus> statuses);
    
    @Query(value = "{ 'status': ?0, 'createdAt': { $gte: ?1 } }", 
           fields = "{ 'id': 1, 'carrier': 1, 'packageType': 1, 'totalItems': 1, 'totalWeight': 1 }")
    List<PackageDocument> findRecentPackageSummary(PackageStatus status, Date since);
    
    // Aggregation queries for advanced analytics
    @Aggregation(pipeline = {
        "{ $match: { 'status': 'DELIVERED', 'deliveredAt': { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { _id: '$carrier', totalPackages: { $sum: 1 }, totalWeight: { $sum: '$totalWeight' }, avgWeight: { $avg: '$totalWeight' } } }",
        "{ $sort: { totalPackages: -1 } }"
    })
    List<CarrierPerformanceSummary> getCarrierPerformanceInPeriod(Date start, Date end);
    
    @Aggregation(pipeline = {
        "{ $match: { 'status': ?0 } }",
        "{ $group: { _id: { type: '$packageType', size: '$packageSize' }, count: { $sum: 1 }, avgWeight: { $avg: '$totalWeight' } } }",
        "{ $sort: { count: -1 } }"
    })
    List<PackageTypeSummary> getPackageTypeSummary(PackageStatus status);
    
    @Aggregation(pipeline = {
        "{ $match: { 'shippedAt': { $gte: ?0, $lte: ?1 }, 'deliveredAt': { $ne: null } } }",
        "{ $project: { carrier: 1, deliveryTime: { $subtract: ['$deliveredAt', '$shippedAt'] } } }",
        "{ $group: { _id: '$carrier', avgDeliveryTime: { $avg: '$deliveryTime' }, totalDeliveries: { $sum: 1 } } }",
        "{ $sort: { avgDeliveryTime: 1 } }"
    })
    List<DeliveryPerformanceSummary> getDeliveryPerformanceByCarrier(Date start, Date end);
    
    // Bulk operations support
    @Query("{ 'status': 'CONFIRMED', 'carrier': ?0, 'totalWeight': { $lte: ?1 } }")
    List<PackageDocument> findPackagesForBulkShipping(String carrier, double maxWeightPerBatch);
    
    @Query("{ 'status': 'PENDING', 'totalWeight': { $gte: ?0 } }")
    List<PackageDocument> findHeavyPackagesRequiringSpecialHandling(double minWeight);
    
    // Interfaces for aggregation results
    interface CarrierPerformanceSummary {
        String getId(); // carrier name
        int getTotalPackages();
        Double getTotalWeight();
        Double getAvgWeight();
    }
    
    interface PackageTypeSummary {
        PackageTypeId getId();
        int getCount();
        Double getAvgWeight();
        
        interface PackageTypeId {
            String getType();
            String getSize();
        }
    }
    
    interface DeliveryPerformanceSummary {
        String getId(); // carrier name
        Double getAvgDeliveryTime(); // in milliseconds
        int getTotalDeliveries();
    }
}