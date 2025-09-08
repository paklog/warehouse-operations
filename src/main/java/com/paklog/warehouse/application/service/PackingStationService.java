package com.paklog.warehouse.application.service;

import com.paklog.warehouse.domain.packaging.Package;
import com.paklog.warehouse.domain.packaging.PackageRepository;
import com.paklog.warehouse.domain.picklist.PickListRepository;
import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.shared.OrderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackingStationService {
    private static final Logger logger = LoggerFactory.getLogger(PackingStationService.class);
    
    private final PackageRepository packageRepository;
    private final PickListRepository pickListRepository;

    public PackingStationService(
        PackageRepository packageRepository, 
        PickListRepository pickListRepository
    ) {
        this.packageRepository = packageRepository;
        this.pickListRepository = pickListRepository;
    }

    public Package createPackage(FulfillmentOrder order) {
        logger.info("Creating package for order: {}", order.getOrderId());
        
        // Fetch the associated PickList for the order
        PickList pickList = pickListRepository.findByOrderId(order.getOrderId());
        
        if (pickList == null) {
            logger.error("No pick list found for order: {}", order.getOrderId());
            throw new IllegalStateException("No pick list found for order: " + order.getOrderId());
        }

        // Create a new Package
        Package pkg = new Package(order, pickList);
        
        // Save the package
        packageRepository.save(pkg);
        
        logger.info("Package created successfully for order: {} with package ID: {}", 
                   order.getOrderId(), pkg.getPackageId());
        
        return pkg;
    }

    public void confirmPackage(Package pkg) {
        logger.info("Confirming package: {}", pkg.getPackageId());
        
        try {
            pkg.confirmPacking();
            packageRepository.save(pkg);
            logger.info("Package confirmed successfully: {}", pkg.getPackageId());
        } catch (Exception e) {
            logger.error("Failed to confirm package: {}", pkg.getPackageId(), e);
            throw e;
        }
    }

    public Package findPackageByOrderId(OrderId orderId) {
        logger.debug("Finding package by order ID: {}", orderId);
        
        Package pkg = packageRepository.findByOrderId(orderId);
        if (pkg != null) {
            logger.debug("Package found for order: {}, package ID: {}", orderId, pkg.getPackageId());
        } else {
            logger.warn("No package found for order: {}", orderId);
        }
        
        return pkg;
    }
}