package com.paklog.warehouse.domain.quality;

/**
 * Quality inspection status lifecycle
 */
public enum QualityInspectionStatus {
    /**
     * Inspection has been scheduled but not started
     */
    SCHEDULED,
    
    /**
     * Inspection is currently in progress
     */
    IN_PROGRESS,
    
    /**
     * Inspection is temporarily on hold
     */
    ON_HOLD,
    
    /**
     * Inspection has been completed
     */
    COMPLETED,
    
    /**
     * Inspection has been cancelled
     */
    CANCELLED;

    public boolean isActive() {
        return this == SCHEDULED || this == IN_PROGRESS || this == ON_HOLD;
    }

    public boolean canBeStarted() {
        return this == SCHEDULED;
    }

    public boolean canBeCompleted() {
        return this == IN_PROGRESS;
    }

    public boolean canBePutOnHold() {
        return this == SCHEDULED || this == IN_PROGRESS;
    }

    public boolean canBeReleased() {
        return this == ON_HOLD;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }
}