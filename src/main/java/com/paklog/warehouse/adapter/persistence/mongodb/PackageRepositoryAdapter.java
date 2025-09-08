package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.packaging.Package;
import com.paklog.warehouse.domain.packaging.PackageRepository;
import com.paklog.warehouse.domain.shared.OrderId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PackageRepositoryAdapter implements PackageRepository {

    private final SpringPackageRepository springPackageRepository;

    @Autowired
    public PackageRepositoryAdapter(SpringPackageRepository springPackageRepository) {
        this.springPackageRepository = springPackageRepository;
    }

    @Override
    public void save(Package pkg) {
        PackageDocument document = new PackageDocument(pkg);
        springPackageRepository.save(document);
    }

    @Override
    public Package findById(UUID packageId) {
        Optional<PackageDocument> document = springPackageRepository.findById(packageId.toString());
        return document.map(PackageDocument::toDomain).orElse(null);
    }

    @Override
    public Package findByOrderId(OrderId orderId) {
        Optional<PackageDocument> document = springPackageRepository.findByOrderId(orderId.getValue().toString());
        return document.map(PackageDocument::toDomain).orElse(null);
    }
}