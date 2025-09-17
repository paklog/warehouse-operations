package com.paklog.warehouse.domain.licenseplate;

/**
 * License plate types following D365 Supply Chain Management patterns
 */
public enum LicensePlateType {
    /**
     * Standard receiving license plate for inbound goods
     */
    RECEIVING("Used for receiving inbound shipments", true, true),
    
    /**
     * Put-away license plate for storing inventory
     */
    PUT_AWAY("Used for put-away operations", true, true),
    
    /**
     * Picking license plate for outbound operations
     */
    PICKING("Used for picking operations", true, false),
    
    /**
     * Shipping license plate for outbound shipments
     */
    SHIPPING("Used for shipping operations", false, false),
    
    /**
     * Transfer license plate for internal movements
     */
    TRANSFER("Used for internal transfers", true, true),
    
    /**
     * Container license plate for nested containers
     */
    CONTAINER("Container for holding other license plates", false, true),
    
    /**
     * Quality license plate for quality control processes
     */
    QUALITY("Used for quality control operations", true, false),
    
    /**
     * Return license plate for returned goods
     */
    RETURN("Used for return processing", true, true);

    private final String description;
    private final boolean canReceiveInventory;
    private final boolean canHaveChildren;

    LicensePlateType(String description, boolean canReceiveInventory, boolean canHaveChildren) {
        this.description = description;
        this.canReceiveInventory = canReceiveInventory;
        this.canHaveChildren = canHaveChildren;
    }

    public String getDescription() {
        return description;
    }

    public boolean canReceiveInventory() {
        return canReceiveInventory;
    }

    public boolean canHaveChildren() {
        return canHaveChildren;
    }

    public boolean isContainerType() {
        return this == CONTAINER;
    }

    public boolean isShippingType() {
        return this == SHIPPING || this == PICKING;
    }

    public boolean isReceivingType() {
        return this == RECEIVING || this == PUT_AWAY;
    }
}