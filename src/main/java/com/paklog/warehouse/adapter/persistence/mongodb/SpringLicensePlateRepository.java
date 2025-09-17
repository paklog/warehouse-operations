package com.paklog.warehouse.adapter.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringLicensePlateRepository extends MongoRepository<LicensePlateDocument, String> {
    
    Optional<LicensePlateDocument> findByLicensePlateId(String licensePlateId);
    
    List<LicensePlateDocument> findByStatus(String status);
    
    List<LicensePlateDocument> findByType(String type);
    
    @Query("{ 'currentLocation.aisle': ?0, 'currentLocation.rack': ?1, 'currentLocation.level': ?2 }")
    List<LicensePlateDocument> findByLocation(String aisle, String rack, String level);
    
    @Query("{ 'inventory.?0': { $exists: true, $gt: 0 } }")
    List<LicensePlateDocument> findByItem(String skuCode);
    
    List<LicensePlateDocument> findByParentLicensePlateId(String parentId);
    
    @Query("{ 'status': { $in: ['AVAILABLE', 'RECEIVED'] } }")
    List<LicensePlateDocument> findAvailableForPicking();
    
    @Query("{ 'status': { $in: ['PICKED', 'STAGED'] } }")
    List<LicensePlateDocument> findAvailableForShipping();
    
    @Query("{ 'inventory': { $eq: {} } }")
    List<LicensePlateDocument> findEmptyLicensePlates();
    
    @Query("{ 'parentLicensePlateId': null }")
    List<LicensePlateDocument> findRootLicensePlates();
    
    @Query("{ 'childLicensePlates': { $exists: true, $not: { $size: 0 } } }")
    List<LicensePlateDocument> findLicensePlatesWithChildren();
    
    @Query("{ 'currentLocation.aisle': ?0 }")
    List<LicensePlateDocument> findByLocationAisle(String aisle);
    
    @Query("{ 'receivingReference': ?0 }")
    List<LicensePlateDocument> findByReceivingReference(String receivingReference);
    
    @Query("{ 'shipmentReference': ?0 }")
    List<LicensePlateDocument> findByShipmentReference(String shipmentReference);
    
    List<LicensePlateDocument> findByCreatedBy(String createdBy);
    
    List<LicensePlateDocument> findByLastMovedBy(String lastMovedBy);
    
    long countByStatus(String status);
    
    long countByType(String type);
    
    @Query("{ 'inventory': { $ne: {} } }")
    long countNonEmptyLicensePlates();
    
    @Query("{ 'parentLicensePlateId': null }")
    long countRootLicensePlates();
}