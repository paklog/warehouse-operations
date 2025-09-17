package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class LicensePlateService {
    private static final Logger logger = LoggerFactory.getLogger(LicensePlateService.class);
    
    private final LicensePlateRepository repository;
    private final LicensePlateGenerator generator;

    public LicensePlateService(LicensePlateRepository repository, LicensePlateGenerator generator) {
        this.repository = Objects.requireNonNull(repository, "Repository cannot be null");
        this.generator = Objects.requireNonNull(generator, "Generator cannot be null");
    }

    public LicensePlate createLicensePlate(LicensePlateType type, String createdBy) {
        logger.info("Creating new license plate of type: {} by: {}", type, createdBy);
        
        LicensePlateId licensePlateId = generator.generateId();
        LicensePlate licensePlate = new LicensePlate(licensePlateId, type, createdBy);
        
        return repository.save(licensePlate);
    }

    public LicensePlate createLicensePlateWithId(LicensePlateId licensePlateId, 
                                               LicensePlateType type, String createdBy) {
        logger.info("Creating license plate with ID: {} of type: {} by: {}", 
                   licensePlateId, type, createdBy);
        
        if (repository.existsById(licensePlateId)) {
            throw new IllegalArgumentException("License plate already exists: " + licensePlateId);
        }
        
        LicensePlate licensePlate = new LicensePlate(licensePlateId, type, createdBy);
        return repository.save(licensePlate);
    }

    public LicensePlate receiveLicensePlate(LicensePlateId licensePlateId, BinLocation location,
                                          String receivedBy, String receivingReference) {
        logger.info("Receiving license plate: {} at location: {} by: {}", 
                   licensePlateId, location, receivedBy);
        
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        licensePlate.receive(location, receivedBy, receivingReference);
        
        return repository.save(licensePlate);
    }

    public LicensePlate moveLicensePlate(LicensePlateId licensePlateId, BinLocation newLocation,
                                       String movedBy) {
        logger.info("Moving license plate: {} to location: {} by: {}", 
                   licensePlateId, newLocation, movedBy);
        
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        licensePlate.moveTo(newLocation, movedBy);
        
        return repository.save(licensePlate);
    }

    public LicensePlate addInventoryToLicensePlate(LicensePlateId licensePlateId, 
                                                 SkuCode item, Quantity quantity) {
        logger.info("Adding inventory to license plate: {} - item: {} quantity: {}", 
                   licensePlateId, item, quantity);
        
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        
        if (!licensePlate.canReceiveInventory()) {
            throw new IllegalStateException("License plate cannot receive inventory in status: " + 
                                          licensePlate.getStatus());
        }
        
        licensePlate.addInventory(item, quantity);
        return repository.save(licensePlate);
    }

    public LicensePlate removeInventoryFromLicensePlate(LicensePlateId licensePlateId, 
                                                      SkuCode item, Quantity quantity) {
        logger.info("Removing inventory from license plate: {} - item: {} quantity: {}", 
                   licensePlateId, item, quantity);
        
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        licensePlate.removeInventory(item, quantity);
        
        return repository.save(licensePlate);
    }

    public LicensePlate pickLicensePlate(LicensePlateId licensePlateId, String pickedBy) {
        logger.info("Picking license plate: {} by: {}", licensePlateId, pickedBy);
        
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        
        if (!licensePlate.isPickable()) {
            throw new IllegalStateException("License plate cannot be picked in status: " + 
                                          licensePlate.getStatus());
        }
        
        licensePlate.pick(pickedBy);
        return repository.save(licensePlate);
    }

    public LicensePlate stageLicensePlate(LicensePlateId licensePlateId, BinLocation stagingLocation,
                                        String stagedBy) {
        logger.info("Staging license plate: {} at location: {} by: {}", 
                   licensePlateId, stagingLocation, stagedBy);
        
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        licensePlate.stage(stagingLocation, stagedBy);
        
        return repository.save(licensePlate);
    }

    public LicensePlate shipLicensePlate(LicensePlateId licensePlateId, String shipmentReference,
                                       String shippedBy) {
        logger.info("Shipping license plate: {} with reference: {} by: {}", 
                   licensePlateId, shipmentReference, shippedBy);
        
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        licensePlate.ship(shipmentReference, shippedBy);
        
        return repository.save(licensePlate);
    }

    public List<LicensePlate> findAvailableForPicking() {
        return repository.findAvailableForPicking();
    }

    public List<LicensePlate> findAvailableForShipping() {
        return repository.findAvailableForShipping();
    }

    public List<LicensePlate> findByLocation(BinLocation location) {
        return repository.findByLocation(location);
    }

    public List<LicensePlate> findByItem(SkuCode item) {
        return repository.findByItem(item);
    }

    public Optional<LicensePlate> findById(LicensePlateId licensePlateId) {
        return repository.findById(licensePlateId);
    }

    public LicensePlate createContainerLicensePlate(String createdBy) {
        logger.info("Creating container license plate by: {}", createdBy);
        return createLicensePlate(LicensePlateType.CONTAINER, createdBy);
    }

    public LicensePlate nestLicensePlate(LicensePlateId parentId, LicensePlateId childId) {
        logger.info("Nesting license plate: {} under parent: {}", childId, parentId);
        
        LicensePlate parent = findByIdOrThrow(parentId);
        LicensePlate child = findByIdOrThrow(childId);
        
        if (!parent.getType().canHaveChildren()) {
            throw new IllegalStateException("License plate type cannot have children: " + 
                                          parent.getType());
        }
        
        if (child.hasParent()) {
            throw new IllegalStateException("License plate already has a parent: " + 
                                          child.getParentLicensePlateId());
        }
        
        parent.addChildLicensePlate(childId.getValue());
        child.setParentLicensePlate(parentId.getValue());
        
        repository.save(parent);
        repository.save(child);
        
        return parent;
    }

    public LicensePlate unnestLicensePlate(LicensePlateId parentId, LicensePlateId childId) {
        logger.info("Unnesting license plate: {} from parent: {}", childId, parentId);
        
        LicensePlate parent = findByIdOrThrow(parentId);
        LicensePlate child = findByIdOrThrow(childId);
        
        parent.removeChildLicensePlate(childId.getValue());
        child.setParentLicensePlate(null);
        
        repository.save(parent);
        repository.save(child);
        
        return parent;
    }

    public LicensePlateInventorySummary getInventorySummary(LicensePlateId licensePlateId) {
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        
        Map<SkuCode, Quantity> inventory = licensePlate.getInventory();
        int totalQuantity = licensePlate.getTotalQuantity();
        Set<SkuCode> items = licensePlate.getItems();
        
        return new LicensePlateInventorySummary(licensePlateId, inventory, totalQuantity, 
                                              items, licensePlate.isEmpty());
    }

    public List<LicensePlate> findEmptyLicensePlates() {
        return repository.findEmptyLicensePlates();
    }

    public void cancelLicensePlate(LicensePlateId licensePlateId, String reason, String cancelledBy) {
        logger.info("Cancelling license plate: {} by: {} for reason: {}", 
                   licensePlateId, cancelledBy, reason);
        
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        licensePlate.cancel(reason, cancelledBy);
        
        repository.save(licensePlate);
    }

    public LicensePlateHierarchy getLicensePlateHierarchy(LicensePlateId licensePlateId) {
        LicensePlate licensePlate = findByIdOrThrow(licensePlateId);
        
        // Get parent if exists
        LicensePlate parent = null;
        if (licensePlate.hasParent()) {
            parent = repository.findById(LicensePlateId.fromString(licensePlate.getParentLicensePlateId()))
                .orElse(null);
        }
        
        // Get all children
        List<LicensePlate> children = repository.findChildLicensePlates(licensePlateId);
        
        return new LicensePlateHierarchy(licensePlate, parent, children);
    }

    private LicensePlate findByIdOrThrow(LicensePlateId licensePlateId) {
        return repository.findById(licensePlateId)
            .orElseThrow(() -> new IllegalArgumentException("License plate not found: " + licensePlateId));
    }

    // Helper classes
    public static class LicensePlateInventorySummary {
        private final LicensePlateId licensePlateId;
        private final Map<SkuCode, Quantity> inventory;
        private final int totalQuantity;
        private final Set<SkuCode> items;
        private final boolean isEmpty;

        public LicensePlateInventorySummary(LicensePlateId licensePlateId, 
                                          Map<SkuCode, Quantity> inventory, 
                                          int totalQuantity, Set<SkuCode> items, 
                                          boolean isEmpty) {
            this.licensePlateId = licensePlateId;
            this.inventory = Collections.unmodifiableMap(new HashMap<>(inventory));
            this.totalQuantity = totalQuantity;
            this.items = Collections.unmodifiableSet(new HashSet<>(items));
            this.isEmpty = isEmpty;
        }

        public LicensePlateId getLicensePlateId() { return licensePlateId; }
        public Map<SkuCode, Quantity> getInventory() { return inventory; }
        public int getTotalQuantity() { return totalQuantity; }
        public Set<SkuCode> getItems() { return items; }
        public boolean isEmpty() { return isEmpty; }
    }

    public static class LicensePlateHierarchy {
        private final LicensePlate licensePlate;
        private final LicensePlate parent;
        private final List<LicensePlate> children;

        public LicensePlateHierarchy(LicensePlate licensePlate, LicensePlate parent, 
                                   List<LicensePlate> children) {
            this.licensePlate = licensePlate;
            this.parent = parent;
            this.children = Collections.unmodifiableList(new ArrayList<>(children));
        }

        public LicensePlate getLicensePlate() { return licensePlate; }
        public LicensePlate getParent() { return parent; }
        public List<LicensePlate> getChildren() { return children; }
        public boolean hasParent() { return parent != null; }
        public boolean hasChildren() { return !children.isEmpty(); }
    }
}