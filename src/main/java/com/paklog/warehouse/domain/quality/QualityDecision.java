package com.paklog.warehouse.domain.quality;

// Quality Decision
public enum QualityDecision {
    APPROVED("Approved for use/shipment"),
    REJECTED("Rejected - cannot be used"),
    CONDITIONAL_APPROVAL("Approved with conditions"),
    REWORK_REQUIRED("Requires rework"),
    QUARANTINE("Move to quarantine");

    private final String description;

    QualityDecision(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isApproved() {
        return this == APPROVED || this == CONDITIONAL_APPROVAL;
    }

    public boolean isRejected() {
        return this == REJECTED || this == QUARANTINE;
    }
}