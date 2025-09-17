package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.AggregateRoot;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QualityHold extends AggregateRoot {
    private final QualityHoldId holdId;
    private final SkuCode item;
    private final String batchNumber;
    private final int quantity;
    private QualityHoldReason reason;
    private final String heldBy;
    private final Instant heldAt;
    private QualityHoldStatus status;
    private String releasedBy;
    private Instant releasedAt;
    private String releaseNotes;
    private final List<QualityHoldNote> notes;
    private QualityHoldPriority priority;

    public QualityHold(QualityHoldId holdId, SkuCode item, String batchNumber, int quantity,
                      QualityHoldReason reason, String heldBy, String initialNotes) {
        this.holdId = Objects.requireNonNull(holdId, "Hold ID cannot be null");
        this.item = Objects.requireNonNull(item, "Item cannot be null");
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.reason = Objects.requireNonNull(reason, "Reason cannot be null");
        this.heldBy = Objects.requireNonNull(heldBy, "Held by cannot be null");
        this.heldAt = Instant.now();
        this.status = QualityHoldStatus.ACTIVE;
        this.notes = new ArrayList<>();
        this.priority = determinePriority(reason);

        if (initialNotes != null && !initialNotes.trim().isEmpty()) {
            this.notes.add(new QualityHoldNote(initialNotes, heldBy, heldAt));
        }

        addDomainEvent(new QualityHoldCreatedEvent(holdId, item, batchNumber, quantity, 
                      reason, heldBy, heldAt, initialNotes));
    }

    public void release(String releasedBy, String notes) {
        if (this.status != QualityHoldStatus.ACTIVE) {
            throw new IllegalStateException("Cannot release hold - status is " + status);
        }

        this.status = QualityHoldStatus.RELEASED;
        this.releasedBy = Objects.requireNonNull(releasedBy, "Released by cannot be null");
        this.releasedAt = Instant.now();
        this.releaseNotes = notes;

        if (notes != null && !notes.trim().isEmpty()) {
            this.notes.add(new QualityHoldNote(notes, releasedBy, releasedAt));
        }

        addDomainEvent(new QualityHoldReleasedEvent(holdId, releasedBy, releasedAt, notes));
    }

    public void escalate(QualityHoldReason newReason, String escalatedBy) {
        if (this.status != QualityHoldStatus.ACTIVE) {
            throw new IllegalStateException("Cannot escalate hold - status is " + status);
        }

        QualityHoldReason previousReason = this.reason;
        this.reason = Objects.requireNonNull(newReason, "New reason cannot be null");
        this.priority = determinePriority(newReason);

        String escalationNote = String.format("Hold escalated from %s to %s", 
                                            previousReason.getDescription(), 
                                            newReason.getDescription());
        this.notes.add(new QualityHoldNote(escalationNote, escalatedBy, Instant.now()));

        addDomainEvent(new QualityHoldEscalatedEvent(holdId, previousReason, newReason, 
                      escalatedBy, Instant.now()));
    }

    public void addNote(String note, String addedBy) {
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Note cannot be null or empty");
        }

        QualityHoldNote holdNote = new QualityHoldNote(note, addedBy, Instant.now());
        this.notes.add(holdNote);

        addDomainEvent(new QualityHoldNoteAddedEvent(holdId, note, addedBy, Instant.now()));
    }

    private QualityHoldPriority determinePriority(QualityHoldReason reason) {
        return switch (reason) {
            case PENDING_TEST_RESULTS, SAMPLE_PREPARATION -> QualityHoldPriority.LOW;
            case INSPECTOR_UNAVAILABLE, DOCUMENTATION_MISSING -> QualityHoldPriority.MEDIUM;
            case EQUIPMENT_MALFUNCTION, CUSTOMER_REQUEST -> QualityHoldPriority.HIGH;
            case SUPPLIER_NOTIFICATION, CORRECTIVE_ACTION_PENDING -> QualityHoldPriority.CRITICAL;
        };
    }

    // Getters
    public QualityHoldId getHoldId() { return holdId; }
    public SkuCode getItem() { return item; }
    public String getBatchNumber() { return batchNumber; }
    public int getQuantity() { return quantity; }
    public QualityHoldReason getReason() { return reason; }
    public String getHeldBy() { return heldBy; }
    public Instant getHeldAt() { return heldAt; }
    public QualityHoldStatus getStatus() { return status; }
    public String getReleasedBy() { return releasedBy; }
    public Instant getReleasedAt() { return releasedAt; }
    public String getReleaseNotes() { return releaseNotes; }
    public List<QualityHoldNote> getNotes() { return new ArrayList<>(notes); }
    public QualityHoldPriority getPriority() { return priority; }
    public boolean isActive() { return status == QualityHoldStatus.ACTIVE; }

    // Quality Hold Note inner class
    public static class QualityHoldNote {
        private final String content;
        private final String addedBy;
        private final Instant addedAt;

        public QualityHoldNote(String content, String addedBy, Instant addedAt) {
            this.content = Objects.requireNonNull(content, "Content cannot be null");
            this.addedBy = Objects.requireNonNull(addedBy, "Added by cannot be null");
            this.addedAt = Objects.requireNonNull(addedAt, "Added at cannot be null");
        }

        public String getContent() { return content; }
        public String getAddedBy() { return addedBy; }
        public Instant getAddedAt() { return addedAt; }
    }
}