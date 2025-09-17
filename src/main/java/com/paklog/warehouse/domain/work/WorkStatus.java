package com.paklog.warehouse.domain.work;

public enum WorkStatus {
    CREATED("Work created and ready for release"),
    RELEASED("Work released and available for assignment"),
    ASSIGNED("Work assigned to a worker"),
    IN_PROGRESS("Work in progress"),
    COMPLETED("Work completed successfully"),
    CANCELLED("Work cancelled");

    private final String description;

    WorkStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == IN_PROGRESS || this == ASSIGNED;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean canTransitionTo(WorkStatus newStatus) {
        switch (this) {
            case CREATED:
                return newStatus == RELEASED || newStatus == CANCELLED;
            case RELEASED:
                return newStatus == ASSIGNED || newStatus == CANCELLED;
            case ASSIGNED:
                return newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case IN_PROGRESS:
                return newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED:
            case CANCELLED:
                return false; // Final states
            default:
                return false;
        }
    }
}