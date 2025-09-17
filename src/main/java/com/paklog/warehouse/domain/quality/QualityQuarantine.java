package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Location;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class QualityQuarantine extends AggregateRoot {
    private final QualityQuarantineId quarantineId;
    private final SkuCode item;
    private final String batchNumber;
    private final int quantity;
    private final QualityQuarantineReason reason;
    private final String quarantinedBy;
    private final Instant quarantinedAt;
    private QualityQuarantineStatus status;
    private Location quarantineLocation;
    private Instant expiryDate;
    private String releasedBy;
    private Instant releasedAt;
    private String releaseNotes;
    private String disposedBy;
    private Instant disposedAt;
    private String disposeNotes;
    private boolean isExpired;

    public QualityQuarantine(QualityQuarantineId quarantineId, SkuCode item, String batchNumber,
                           int quantity, QualityQuarantineReason reason, String quarantinedBy) {
        this.quarantineId = Objects.requireNonNull(quarantineId, "Quarantine ID cannot be null");
        this.item = Objects.requireNonNull(item, "Item cannot be null");
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.reason = Objects.requireNonNull(reason, "Reason cannot be null");
        this.quarantinedBy = Objects.requireNonNull(quarantinedBy, "Quarantined by cannot be null");
        this.quarantinedAt = Instant.now();
        this.status = QualityQuarantineStatus.ACTIVE;
        this.expiryDate = calculateExpiryDate(reason);
        this.isExpired = false;

        // Register domain event
        addDomainEvent(new QualityQuarantineCreatedEvent(quarantineId, item, batchNumber, 
                      quantity, reason, quarantinedBy, quarantinedAt));
    }

    public void moveToLocation(Location quarantineLocation) {
        if (this.status != QualityQuarantineStatus.ACTIVE) {
            throw new IllegalStateException("Cannot move quarantined item - status is " + status);
        }

        this.quarantineLocation = Objects.requireNonNull(quarantineLocation, 
                                                       "Quarantine location cannot be null");
        
        addDomainEvent(new QualityQuarantineLocationChangedEvent(quarantineId, quarantineLocation, 
                      Instant.now()));
    }

    public void release(String releasedBy, String notes) {
        if (this.status != QualityQuarantineStatus.ACTIVE) {
            throw new IllegalStateException("Cannot release quarantine - status is " + status);
        }

        this.status = QualityQuarantineStatus.RELEASED;
        this.releasedBy = Objects.requireNonNull(releasedBy, "Released by cannot be null");
        this.releasedAt = Instant.now();
        this.releaseNotes = notes;

        addDomainEvent(new QualityQuarantineReleasedEvent(quarantineId, releasedBy, releasedAt, notes));
    }

    public void dispose(String disposedBy, String notes) {
        if (this.status != QualityQuarantineStatus.ACTIVE) {
            throw new IllegalStateException("Cannot dispose quarantine - status is " + status);
        }

        this.status = QualityQuarantineStatus.DISPOSED;
        this.disposedBy = Objects.requireNonNull(disposedBy, "Disposed by cannot be null");
        this.disposedAt = Instant.now();
        this.disposeNotes = notes;

        addDomainEvent(new QualityQuarantineDisposedEvent(quarantineId, disposedBy, disposedAt, notes));
    }

    public void extendPeriod(Instant newExpiryDate, String extendedBy, String reason) {
        if (this.status != QualityQuarantineStatus.ACTIVE) {
            throw new IllegalStateException("Cannot extend quarantine - status is " + status);
        }

        Instant oldExpiryDate = this.expiryDate;
        this.expiryDate = Objects.requireNonNull(newExpiryDate, "New expiry date cannot be null");
        this.isExpired = false;

        addDomainEvent(new QualityQuarantineExtendedEvent(quarantineId, oldExpiryDate, 
                      newExpiryDate, extendedBy, reason, Instant.now()));
    }

    public void checkExpiry() {
        if (status == QualityQuarantineStatus.ACTIVE && 
            expiryDate != null && 
            Instant.now().isAfter(expiryDate) && 
            !isExpired) {
            
            this.isExpired = true;
            addDomainEvent(new QualityQuarantineExpiredEvent(quarantineId, expiryDate, Instant.now()));
        }
    }

    private Instant calculateExpiryDate(QualityQuarantineReason reason) {
        Instant now = Instant.now();
        return switch (reason) {
            case FAILED_INSPECTION -> now.plus(7, ChronoUnit.DAYS);
            case CONTAMINATION_SUSPECTED -> now.plus(14, ChronoUnit.DAYS);
            case SUPPLIER_RECALL -> now.plus(30, ChronoUnit.DAYS);
            case REGULATORY_HOLD -> now.plus(60, ChronoUnit.DAYS);
            case CUSTOMER_COMPLAINT -> now.plus(21, ChronoUnit.DAYS);
            case DAMAGE_SUSPECTED -> now.plus(7, ChronoUnit.DAYS);
            case BATCH_INVESTIGATION -> now.plus(45, ChronoUnit.DAYS);
            case PREVENTIVE_HOLD -> now.plus(14, ChronoUnit.DAYS);
        };
    }

    // Getters
    public QualityQuarantineId getQuarantineId() { return quarantineId; }
    public SkuCode getItem() { return item; }
    public String getBatchNumber() { return batchNumber; }
    public int getQuantity() { return quantity; }
    public QualityQuarantineReason getReason() { return reason; }
    public String getQuarantinedBy() { return quarantinedBy; }
    public Instant getQuarantinedAt() { return quarantinedAt; }
    public QualityQuarantineStatus getStatus() { return status; }
    public Location getQuarantineLocation() { return quarantineLocation; }
    public Instant getExpiryDate() { return expiryDate; }
    public String getReleasedBy() { return releasedBy; }
    public Instant getReleasedAt() { return releasedAt; }
    public String getReleaseNotes() { return releaseNotes; }
    public String getDisposedBy() { return disposedBy; }
    public Instant getDisposedAt() { return disposedAt; }
    public String getDisposeNotes() { return disposeNotes; }
    public boolean isExpired() { return isExpired; }
    public boolean isActive() { return status == QualityQuarantineStatus.ACTIVE && !isExpired; }
}