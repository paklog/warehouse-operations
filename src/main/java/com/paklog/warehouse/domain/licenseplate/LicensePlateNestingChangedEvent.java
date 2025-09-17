package com.paklog.warehouse.domain.licenseplate;

import com.paklog.warehouse.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class LicensePlateNestingChangedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final LicensePlateId parentLicensePlateId;
    private final String childLicensePlateId;
    private final boolean added;

    public LicensePlateNestingChangedEvent(LicensePlateId parentLicensePlateId, String childLicensePlateId, boolean added) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.parentLicensePlateId = parentLicensePlateId;
        this.childLicensePlateId = childLicensePlateId;
        this.added = added;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public LicensePlateId getParentLicensePlateId() {
        return parentLicensePlateId;
    }

    public String getChildLicensePlateId() {
        return childLicensePlateId;
    }

    public boolean isAdded() {
        return added;
    }
}