package com.paklog.warehouse.domain.quality;

// Quality Hold Reason
public enum QualityHoldReason {
    PENDING_TEST_RESULTS("Waiting for test results"),
    EQUIPMENT_MALFUNCTION("Equipment malfunction"),
    INSPECTOR_UNAVAILABLE("Inspector not available"),
    SAMPLE_PREPARATION("Sample preparation required"),
    CUSTOMER_REQUEST("Customer hold request"),
    SUPPLIER_NOTIFICATION("Supplier notification required"),
    DOCUMENTATION_MISSING("Missing documentation"),
    CORRECTIVE_ACTION_PENDING("Pending corrective action");

    private final String description;

    QualityHoldReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}