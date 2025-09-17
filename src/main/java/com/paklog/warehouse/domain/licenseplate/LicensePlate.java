package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class LicensePlate extends AggregateRoot {
    private final LicensePlateId licensePlateId;
    private LicensePlateStatus status;
    private LicensePlateType type;
    private BinLocation currentLocation;
    private String parentLicensePlateId;
    private final Map<SkuCode, Quantity> inventory;
    private final Set<String> childLicensePlates;
    private String receivingReference;
    private String shipmentReference;
    private Instant createdAt;
    private Instant receivedAt;
    private Instant shippedAt;
    private String createdBy;
    private String lastMovedBy;
    private Instant lastMovedAt;
    private final Map<String, Object> attributes;
    private int version;

    public LicensePlate(LicensePlateId licensePlateId, LicensePlateType type, String createdBy) {
        this.licensePlateId = Objects.requireNonNull(licensePlateId, "License plate ID cannot be null");
        this.type = Objects.requireNonNull(type, "License plate type cannot be null");
        this.createdBy = Objects.requireNonNull(createdBy, "Created by cannot be null");
        
        this.status = LicensePlateStatus.CREATED;
        this.inventory = new HashMap<>();
        this.childLicensePlates = new HashSet<>();
        this.attributes = new HashMap<>();
        this.createdAt = Instant.now();
        this.version = 1;

        registerEvent(new LicensePlateCreatedEvent(this.licensePlateId, this.type, this.createdBy, this.createdAt));
    }

    // Full constructor for loading from persistence
    public LicensePlate(LicensePlateId licensePlateId, LicensePlateStatus status, LicensePlateType type,
                       BinLocation currentLocation, String parentLicensePlateId, 
                       Map<SkuCode, Quantity> inventory, Set<String> childLicensePlates,
                       String receivingReference, String shipmentReference, 
                       Instant createdAt, Instant receivedAt, Instant shippedAt,
                       String createdBy, String lastMovedBy, Instant lastMovedAt,
                       Map<String, Object> attributes, int version) {
        this.licensePlateId = licensePlateId;
        this.status = status;
        this.type = type;
        this.currentLocation = currentLocation;
        this.parentLicensePlateId = parentLicensePlateId;
        this.inventory = new HashMap<>(inventory != null ? inventory : Map.of());
        this.childLicensePlates = new HashSet<>(childLicensePlates != null ? childLicensePlates : Set.of());
        this.receivingReference = receivingReference;
        this.shipmentReference = shipmentReference;
        this.createdAt = createdAt;
        this.receivedAt = receivedAt;
        this.shippedAt = shippedAt;
        this.createdBy = createdBy;
        this.lastMovedBy = lastMovedBy;
        this.lastMovedAt = lastMovedAt;
        this.attributes = new HashMap<>(attributes != null ? attributes : Map.of());
        this.version = version;
    }

    public void receive(BinLocation location, String receivedBy, String receivingReference) {
        if (status != LicensePlateStatus.CREATED && status != LicensePlateStatus.IN_TRANSIT) {
            throw new IllegalStateException("Cannot receive license plate in status: " + status);
        }

        this.status = LicensePlateStatus.RECEIVED;
        this.currentLocation = Objects.requireNonNull(location, "Receiving location cannot be null");
        this.receivedAt = Instant.now();
        this.receivingReference = receivingReference;
        this.lastMovedBy = receivedBy;
        this.lastMovedAt = this.receivedAt;

        registerEvent(new LicensePlateReceivedEvent(this.licensePlateId, location, receivedBy, 
                                                  this.receivedAt, receivingReference));
    }

    public void moveTo(BinLocation newLocation, String movedBy) {
        if (status == LicensePlateStatus.SHIPPED || status == LicensePlateStatus.CANCELLED) {
            throw new IllegalStateException("Cannot move license plate in status: " + status);
        }

        BinLocation previousLocation = this.currentLocation;
        this.currentLocation = Objects.requireNonNull(newLocation, "New location cannot be null");
        this.lastMovedBy = movedBy;
        this.lastMovedAt = Instant.now();

        registerEvent(new LicensePlateMovedEvent(this.licensePlateId, previousLocation, 
                                               newLocation, movedBy, this.lastMovedAt));
    }

    public void addInventory(SkuCode item, Quantity quantity) {
        Objects.requireNonNull(item, "Item cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        
        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Quantity currentQuantity = inventory.getOrDefault(item, new Quantity(0));
        Quantity newQuantity = new Quantity(currentQuantity.getValue() + quantity.getValue());
        inventory.put(item, newQuantity);

        registerEvent(new LicensePlateInventoryAddedEvent(this.licensePlateId, item, quantity, newQuantity));
    }

    public void removeInventory(SkuCode item, Quantity quantity) {
        Objects.requireNonNull(item, "Item cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        
        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Quantity currentQuantity = inventory.get(item);
        if (currentQuantity == null) {
            throw new IllegalStateException("Item " + item + " not found on license plate");
        }

        if (currentQuantity.getValue() < quantity.getValue()) {
            throw new IllegalStateException("Insufficient quantity. Available: " + currentQuantity.getValue() + 
                                          ", Requested: " + quantity.getValue());
        }

        Quantity remainingQuantity = new Quantity(currentQuantity.getValue() - quantity.getValue());
        if (remainingQuantity.getValue() == 0) {
            inventory.remove(item);
        } else {
            inventory.put(item, remainingQuantity);
        }

        registerEvent(new LicensePlateInventoryRemovedEvent(this.licensePlateId, item, quantity, remainingQuantity));
    }

    public void addChildLicensePlate(String childLicensePlateId) {
        Objects.requireNonNull(childLicensePlateId, "Child license plate ID cannot be null");
        
        if (childLicensePlateId.equals(this.licensePlateId.getValue())) {
            throw new IllegalArgumentException("License plate cannot be its own child");
        }

        this.childLicensePlates.add(childLicensePlateId);
        registerEvent(new LicensePlateNestingChangedEvent(this.licensePlateId, childLicensePlateId, true));
    }

    public void removeChildLicensePlate(String childLicensePlateId) {
        Objects.requireNonNull(childLicensePlateId, "Child license plate ID cannot be null");
        
        if (this.childLicensePlates.remove(childLicensePlateId)) {
            registerEvent(new LicensePlateNestingChangedEvent(this.licensePlateId, childLicensePlateId, false));
        }
    }

    public void setParentLicensePlate(String parentLicensePlateId) {
        this.parentLicensePlateId = parentLicensePlateId;
    }

    public void ship(String shipmentReference, String shippedBy) {
        if (status != LicensePlateStatus.PICKED && status != LicensePlateStatus.STAGED) {
            throw new IllegalStateException("Cannot ship license plate in status: " + status);
        }

        this.status = LicensePlateStatus.SHIPPED;
        this.shipmentReference = shipmentReference;
        this.shippedAt = Instant.now();
        this.lastMovedBy = shippedBy;
        this.lastMovedAt = this.shippedAt;

        registerEvent(new LicensePlateShippedEvent(this.licensePlateId, shipmentReference, 
                                                 shippedBy, this.shippedAt));
    }

    public void pick(String pickedBy) {
        if (status != LicensePlateStatus.AVAILABLE && status != LicensePlateStatus.RECEIVED) {
            throw new IllegalStateException("Cannot pick license plate in status: " + status);
        }

        this.status = LicensePlateStatus.PICKED;
        this.lastMovedBy = pickedBy;
        this.lastMovedAt = Instant.now();

        registerEvent(new LicensePlatePickedEvent(this.licensePlateId, pickedBy, this.lastMovedAt));
    }

    public void stage(BinLocation stagingLocation, String stagedBy) {
        if (status != LicensePlateStatus.PICKED) {
            throw new IllegalStateException("Cannot stage license plate in status: " + status);
        }

        this.status = LicensePlateStatus.STAGED;
        BinLocation previousLocation = this.currentLocation;
        this.currentLocation = stagingLocation;
        this.lastMovedBy = stagedBy;
        this.lastMovedAt = Instant.now();

        registerEvent(new LicensePlateStagedEvent(this.licensePlateId, previousLocation, 
                                                stagingLocation, stagedBy, this.lastMovedAt));
    }

    public void makeAvailable() {
        if (status != LicensePlateStatus.RECEIVED) {
            throw new IllegalStateException("Cannot make available license plate in status: " + status);
        }

        this.status = LicensePlateStatus.AVAILABLE;
        registerEvent(new LicensePlateStatusChangedEvent(this.licensePlateId, 
                                                       LicensePlateStatus.RECEIVED, 
                                                       LicensePlateStatus.AVAILABLE));
    }

    public void cancel(String reason, String cancelledBy) {
        if (status == LicensePlateStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel shipped license plate");
        }

        LicensePlateStatus previousStatus = this.status;
        this.status = LicensePlateStatus.CANCELLED;

        registerEvent(new LicensePlateCancelledEvent(this.licensePlateId, previousStatus, 
                                                   reason, cancelledBy, Instant.now()));
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    // Business logic methods
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    public boolean hasInventory(SkuCode item) {
        return inventory.containsKey(item);
    }

    public Quantity getInventoryQuantity(SkuCode item) {
        return inventory.getOrDefault(item, new Quantity(0));
    }

    public int getTotalQuantity() {
        return inventory.values().stream()
            .mapToInt(Quantity::getValue)
            .sum();
    }

    public Set<SkuCode> getItems() {
        return new HashSet<>(inventory.keySet());
    }

    public boolean hasChildren() {
        return !childLicensePlates.isEmpty();
    }

    public boolean hasParent() {
        return parentLicensePlateId != null;
    }

    public boolean isPickable() {
        return status == LicensePlateStatus.AVAILABLE || status == LicensePlateStatus.RECEIVED;
    }

    public boolean isMovable() {
        return status != LicensePlateStatus.SHIPPED && status != LicensePlateStatus.CANCELLED;
    }

    public boolean canReceiveInventory() {
        return status == LicensePlateStatus.CREATED || status == LicensePlateStatus.RECEIVED || 
               status == LicensePlateStatus.AVAILABLE;
    }

    // Getters
    public LicensePlateId getLicensePlateId() { return licensePlateId; }
    public LicensePlateStatus getStatus() { return status; }
    public LicensePlateType getType() { return type; }
    public BinLocation getCurrentLocation() { return currentLocation; }
    public String getParentLicensePlateId() { return parentLicensePlateId; }
    public Map<SkuCode, Quantity> getInventory() { return Collections.unmodifiableMap(inventory); }
    public Set<String> getChildLicensePlates() { return Collections.unmodifiableSet(childLicensePlates); }
    public String getReceivingReference() { return receivingReference; }
    public String getShipmentReference() { return shipmentReference; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getShippedAt() { return shippedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getLastMovedBy() { return lastMovedBy; }
    public Instant getLastMovedAt() { return lastMovedAt; }
    public Map<String, Object> getAttributes() { return Collections.unmodifiableMap(attributes); }
    public int getVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LicensePlate that = (LicensePlate) o;
        return Objects.equals(licensePlateId, that.licensePlateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licensePlateId);
    }

    @Override
    public String toString() {
        return "LicensePlate{" +
                "licensePlateId=" + licensePlateId +
                ", status=" + status +
                ", type=" + type +
                ", currentLocation=" + currentLocation +
                ", inventoryItems=" + inventory.size() +
                ", childLicensePlates=" + childLicensePlates.size() +
                '}';
    }
}