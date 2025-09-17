package com.paklog.warehouse.domain.licenseplate;

/**
 * License plate status following D365 Supply Chain Management lifecycle
 */
public enum LicensePlateStatus {
    /**
     * License plate has been created but not yet received
     */
    CREATED,
    
    /**
     * License plate is in transit to the warehouse
     */
    IN_TRANSIT,
    
    /**
     * License plate has been received at the warehouse
     */
    RECEIVED,
    
    /**
     * License plate is available for picking/movement operations
     */
    AVAILABLE,
    
    /**
     * License plate has been picked for an order
     */
    PICKED,
    
    /**
     * License plate has been staged for shipping
     */
    STAGED,
    
    /**
     * License plate has been shipped
     */
    SHIPPED,
    
    /**
     * License plate has been cancelled/voided
     */
    CANCELLED;

    public boolean isActive() {
        return this != SHIPPED && this != CANCELLED;
    }

    public boolean canBeMoveded() {
        return this != SHIPPED && this != CANCELLED;
    }

    public boolean canReceiveInventory() {
        return this == CREATED || this == RECEIVED || this == AVAILABLE;
    }

    public boolean canBePicked() {
        return this == AVAILABLE || this == RECEIVED;
    }

    public boolean canBeShipped() {
        return this == PICKED || this == STAGED;
    }
}