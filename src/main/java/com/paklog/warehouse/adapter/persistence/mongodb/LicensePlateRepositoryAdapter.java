package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.licenseplate.*;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LicensePlateRepositoryAdapter implements LicensePlateRepository {
    private static final Logger logger = LoggerFactory.getLogger(LicensePlateRepositoryAdapter.class);
    
    private final SpringLicensePlateRepository springRepository;

    public LicensePlateRepositoryAdapter(SpringLicensePlateRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public LicensePlate save(LicensePlate licensePlate) {
        logger.debug("Saving license plate: {}", licensePlate.getLicensePlateId());
        
        LicensePlateDocument document = new LicensePlateDocument(licensePlate);
        LicensePlateDocument savedDocument = springRepository.save(document);
        
        logger.info("Successfully saved license plate: {}", licensePlate.getLicensePlateId());
        return savedDocument.toDomain();
    }

    @Override
    public Optional<LicensePlate> findById(LicensePlateId licensePlateId) {
        logger.debug("Finding license plate by ID: {}", licensePlateId);
        
        Optional<LicensePlateDocument> document = springRepository.findByLicensePlateId(
                licensePlateId.getValue().toString());
        
        Optional<LicensePlate> result = document.map(LicensePlateDocument::toDomain);
        
        if (result.isPresent()) {
            logger.debug("Found license plate: {}", licensePlateId);
        } else {
            logger.debug("License plate not found: {}", licensePlateId);
        }
        
        return result;
    }

    @Override
    public boolean existsById(LicensePlateId licensePlateId) {
        logger.debug("Checking if license plate exists: {}", licensePlateId);
        
        boolean exists = springRepository.existsById(licensePlateId.getValue().toString());
        
        logger.debug("License plate {} exists: {}", licensePlateId, exists);
        return exists;
    }

    @Override
    public List<LicensePlate> findByStatus(LicensePlateStatus status) {
        logger.debug("Finding license plates by status: {}", status);
        
        List<LicensePlateDocument> documents = springRepository.findByStatus(status.name());
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} license plates with status: {}", licensePlates.size(), status);
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findByType(LicensePlateType type) {
        logger.debug("Finding license plates by type: {}", type);
        
        List<LicensePlateDocument> documents = springRepository.findByType(type.name());
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} license plates of type: {}", licensePlates.size(), type);
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findByLocation(BinLocation location) {
        logger.debug("Finding license plates by location: {}", location);
        
        List<LicensePlateDocument> documents = springRepository.findByLocation(
                location.getAisle(), location.getRack(), location.getLevel());
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} license plates at location: {}", licensePlates.size(), location);
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findByItem(SkuCode item) {
        logger.debug("Finding license plates by item: {}", item);
        
        List<LicensePlateDocument> documents = springRepository.findByItem(item.getValue());
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} license plates containing item: {}", licensePlates.size(), item);
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findByParentLicensePlateId(LicensePlateId parentId) {
        logger.debug("Finding license plates by parent ID: {}", parentId);
        
        List<LicensePlateDocument> documents = springRepository.findByParentLicensePlateId(
                parentId.getValue().toString());
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} child license plates for parent: {}", licensePlates.size(), parentId);
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findAvailableForPicking() {
        logger.debug("Finding license plates available for picking");
        
        List<LicensePlateDocument> documents = springRepository.findAvailableForPicking();
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} license plates available for picking", licensePlates.size());
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findAvailableForShipping() {
        logger.debug("Finding license plates available for shipping");
        
        List<LicensePlateDocument> documents = springRepository.findAvailableForShipping();
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} license plates available for shipping", licensePlates.size());
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findEmptyLicensePlates() {
        logger.debug("Finding empty license plates");
        
        List<LicensePlateDocument> documents = springRepository.findEmptyLicensePlates();
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} empty license plates", licensePlates.size());
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findRootLicensePlates() {
        logger.debug("Finding root license plates");
        
        List<LicensePlateDocument> documents = springRepository.findRootLicensePlates();
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} root license plates", licensePlates.size());
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findLicensePlatesWithChildren() {
        logger.debug("Finding license plates with children");
        
        List<LicensePlateDocument> documents = springRepository.findLicensePlatesWithChildren();
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} license plates with children", licensePlates.size());
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findByReceivingReference(String receivingReference) {
        logger.debug("Finding license plates by receiving reference: {}", receivingReference);
        
        List<LicensePlateDocument> documents = springRepository.findByReceivingReference(receivingReference);
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} license plates with receiving reference: {}", 
                licensePlates.size(), receivingReference);
        return licensePlates;
    }

    @Override
    public List<LicensePlate> findByShipmentReference(String shipmentReference) {
        logger.debug("Finding license plates by shipment reference: {}", shipmentReference);
        
        List<LicensePlateDocument> documents = springRepository.findByShipmentReference(shipmentReference);
        
        List<LicensePlate> licensePlates = documents.stream()
                .map(LicensePlateDocument::toDomain)
                .toList();
        
        logger.debug("Found {} license plates with shipment reference: {}", 
                licensePlates.size(), shipmentReference);
        return licensePlates;
    }

    @Override
    public long countByStatus(LicensePlateStatus status) {
        logger.debug("Counting license plates by status: {}", status);
        
        long count = springRepository.countByStatus(status.name());
        
        logger.debug("Found {} license plates with status: {}", count, status);
        return count;
    }

    @Override
    public long countByType(LicensePlateType type) {
        logger.debug("Counting license plates by type: {}", type);
        
        long count = springRepository.countByType(type.name());
        
        logger.debug("Found {} license plates of type: {}", count, type);
        return count;
    }

    @Override
    public long countEmptyLicensePlates() {
        logger.debug("Counting empty license plates");
        
        long count = springRepository.findEmptyLicensePlates().size();
        
        logger.debug("Found {} empty license plates", count);
        return count;
    }

    @Override
    public long countRootLicensePlates() {
        logger.debug("Counting root license plates");
        
        long count = springRepository.countRootLicensePlates();
        
        logger.debug("Found {} root license plates", count);
        return count;
    }

    @Override
    public void delete(LicensePlate licensePlate) {
        logger.debug("Deleting license plate: {}", licensePlate.getLicensePlateId());
        
        springRepository.deleteById(licensePlate.getLicensePlateId().getValue().toString());
        
        logger.info("Successfully deleted license plate: {}", licensePlate.getLicensePlateId());
    }
}