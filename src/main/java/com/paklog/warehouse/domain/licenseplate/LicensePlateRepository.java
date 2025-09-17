package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.List;
import java.util.Optional;

public interface LicensePlateRepository {
    LicensePlate save(LicensePlate licensePlate);
    
    Optional<LicensePlate> findById(LicensePlateId licensePlateId);
    
    boolean existsById(LicensePlateId licensePlateId);
    
    List<LicensePlate> findByStatus(LicensePlateStatus status);
    
    List<LicensePlate> findByType(LicensePlateType type);
    
    List<LicensePlate> findByLocation(BinLocation location);
    
    List<LicensePlate> findByItem(SkuCode item);
    
    List<LicensePlate> findByParentLicensePlateId(LicensePlateId parentId);
    
    List<LicensePlate> findAvailableForPicking();
    
    List<LicensePlate> findAvailableForShipping();
    
    List<LicensePlate> findEmptyLicensePlates();
    
    List<LicensePlate> findRootLicensePlates();
    
    List<LicensePlate> findLicensePlatesWithChildren();
    
    List<LicensePlate> findByReceivingReference(String receivingReference);
    
    List<LicensePlate> findByShipmentReference(String shipmentReference);
    
    long countByStatus(LicensePlateStatus status);
    
    long countByType(LicensePlateType type);
    
    long countEmptyLicensePlates();
    
    long countRootLicensePlates();
    
    void delete(LicensePlate licensePlate);
}