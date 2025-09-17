package com.paklog.warehouse.domain.quality;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// Quality Decision
enum QualityDecision {
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

// Quality Hold Reason
enum QualityHoldReason {
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

// Quality Severity
enum QualitySeverity {
    LOW(1, "Minor issue, minimal impact"),
    MEDIUM(2, "Moderate issue, some impact"),
    HIGH(3, "Significant issue, major impact"),
    CRITICAL(4, "Critical issue, severe impact");

    private final int level;
    private final String description;

    QualitySeverity(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHigherThan(QualitySeverity other) {
        return this.level > other.level;
    }
}


// Quality Quarantine Reason
enum QualityQuarantineReason {
    FAILED_INSPECTION("Failed quality inspection"),
    CONTAMINATION_SUSPECTED("Contamination suspected"),
    SUPPLIER_RECALL("Supplier recall notice"),
    REGULATORY_HOLD("Regulatory hold"),
    CUSTOMER_COMPLAINT("Customer complaint"),
    DAMAGE_SUSPECTED("Damage suspected"),
    BATCH_INVESTIGATION("Batch investigation"),
    PREVENTIVE_HOLD("Preventive hold");

    private final String description;

    QualityQuarantineReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

// Quality Quarantine Status
enum QualityQuarantineStatus {
    ACTIVE, RELEASED, DISPOSED
}

// Quality Hold Status
enum QualityHoldStatus {
    ACTIVE, RELEASED
}

// Quality Hold Priority
enum QualityHoldPriority {
    LOW(1, "Low priority"),
    MEDIUM(2, "Medium priority"),
    HIGH(3, "High priority"),
    CRITICAL(4, "Critical priority");

    private final int level;
    private final String description;

    QualityHoldPriority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() { return level; }
    public String getDescription() { return description; }
    
    public boolean isHigherThan(QualityHoldPriority other) {
        return this.level > other.level;
    }
}

// Quality Sampling Strategy
enum QualitySamplingStrategy {
    FIXED_SIZE("Fixed sample size regardless of lot size"),
    PERCENTAGE("Sample size as percentage of lot size"),
    SQUARE_ROOT("Sample size based on square root of lot size"),
    MIL_STD_105E("MIL-STD-105E sampling standard");

    private final String description;

    QualitySamplingStrategy(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}

// Quality Sample Status
enum QualitySampleStatus {
    COLLECTED, TESTING_IN_PROGRESS, TESTING_COMPLETED, REJECTED
}

// Quality Sample Verdict
enum QualitySampleVerdict {
    ACCEPTED, REJECTED
}

// Quality Corrective Action Type
enum QualityCorrectiveActionType {
    IMMEDIATE_CONTAINMENT("Immediate containment action"),
    ROOT_CAUSE_ANALYSIS("Root cause analysis"),
    CORRECTIVE_ACTION("Corrective action implementation"),
    PREVENTIVE_ACTION("Preventive action implementation"),
    PROCESS_IMPROVEMENT("Process improvement"),
    TRAINING("Training and education"),
    SUPPLIER_ACTION("Supplier corrective action"),
    SYSTEM_UPDATE("System or procedure update");

    private final String description;

    QualityCorrectiveActionType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}

// Quality Corrective Action Priority
enum QualityCorrectiveActionPriority {
    LOW(1, "Low priority - routine improvement"),
    MEDIUM(2, "Medium priority - moderate impact"),
    HIGH(3, "High priority - significant impact"),
    CRITICAL(4, "Critical priority - immediate action required");

    private final int level;
    private final String description;

    QualityCorrectiveActionPriority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() { return level; }
    public String getDescription() { return description; }
    
    public boolean isHigherThan(QualityCorrectiveActionPriority other) {
        return this.level > other.level;
    }
}

// Quality Corrective Action Status
enum QualityCorrectiveActionStatus {
    ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
}

// Quality Corrective Action Effectiveness
enum QualityCorrectiveActionEffectiveness {
    EFFECTIVE("Action was effective - no recurrence"),
    PARTIALLY_EFFECTIVE("Action was partially effective - some improvement"),
    INEFFECTIVE("Action was ineffective - issue persists"),
    PENDING_VERIFICATION("Effectiveness pending verification"),
    VERIFIED("Effectiveness has been verified");

    private final String description;

    QualityCorrectiveActionEffectiveness(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}