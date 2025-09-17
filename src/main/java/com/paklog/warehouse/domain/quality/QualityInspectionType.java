package com.paklog.warehouse.domain.quality;

/**
 * Quality inspection types following D365 Supply Chain Management patterns
 */
public enum QualityInspectionType {
    /**
     * Incoming goods inspection upon receipt
     */
    RECEIVING_INSPECTION("Inspection of received goods", true, false),
    
    /**
     * In-process inspection during production/operations
     */
    IN_PROCESS_INSPECTION("Inspection during warehouse operations", false, false),
    
    /**
     * Final inspection before shipping
     */
    FINAL_INSPECTION("Final inspection before shipment", false, true),
    
    /**
     * Random sampling inspection
     */
    SAMPLING_INSPECTION("Random sampling quality check", true, true),
    
    /**
     * Quarantine inspection for held items
     */
    QUARANTINE_INSPECTION("Inspection of quarantined items", true, false),
    
    /**
     * Return inspection for returned goods
     */
    RETURN_INSPECTION("Inspection of returned items", true, false),
    
    /**
     * Supplier audit inspection
     */
    SUPPLIER_AUDIT("Supplier quality audit inspection", true, false),
    
    /**
     * Corrective action verification
     */
    CORRECTIVE_ACTION_VERIFICATION("Verification after corrective action", false, false);

    private final String description;
    private final boolean appliesToInbound;
    private final boolean appliesToOutbound;

    QualityInspectionType(String description, boolean appliesToInbound, boolean appliesToOutbound) {
        this.description = description;
        this.appliesToInbound = appliesToInbound;
        this.appliesToOutbound = appliesToOutbound;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAppliesToInbound() {
        return appliesToInbound;
    }

    public boolean isAppliesToOutbound() {
        return appliesToOutbound;
    }

    public boolean isReceivingType() {
        return this == RECEIVING_INSPECTION || this == RETURN_INSPECTION;
    }

    public boolean isShippingType() {
        return this == FINAL_INSPECTION;
    }

    public boolean requiresQuarantine() {
        return this == QUARANTINE_INSPECTION || this == RETURN_INSPECTION;
    }
}