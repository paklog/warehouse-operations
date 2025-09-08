package com.paklog.warehouse.domain.packaging;

import com.paklog.warehouse.domain.shared.OrderId;

import java.util.UUID;

public interface PackageRepository {
    void save(Package pkg);
    Package findById(UUID packageId);
    Package findByOrderId(OrderId orderId);
}