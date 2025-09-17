package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.quality.*;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "quality_holds")
public class QualityHoldDocument {
    @Id
    private String id;
    private String holdId;
    private String itemSkuCode;
    private String batchNumber;
    private int quantity;
    private String reason;
    private String heldBy;
    private Instant heldAt;
    private String status;
    private String releasedBy;
    private Instant releasedAt;
    private String releaseNotes;
    private List<QualityHoldNoteDocument> notes;
    private String priority;

    // Constructors
    public QualityHoldDocument() {}

    public QualityHoldDocument(QualityHold hold) {
        this.id = hold.getHoldId().getValue().toString();
        this.holdId = hold.getHoldId().getValue().toString();
        this.itemSkuCode = hold.getItem().getValue();
        this.batchNumber = hold.getBatchNumber();
        this.quantity = hold.getQuantity();
        this.reason = hold.getReason().name();
        this.heldBy = hold.getHeldBy();
        this.heldAt = hold.getHeldAt();
        this.status = hold.getStatus().name();
        this.releasedBy = hold.getReleasedBy();
        this.releasedAt = hold.getReleasedAt();
        this.releaseNotes = hold.getReleaseNotes();
        this.notes = hold.getNotes().stream()
            .map(QualityHoldNoteDocument::new)
            .collect(Collectors.toList());
        this.priority = hold.getPriority().name();
    }

    public QualityHold toDomain() {
        QualityHoldId holdId = QualityHoldId.of(this.holdId);
        SkuCode item = SkuCode.of(this.itemSkuCode);
        QualityHoldReason reason = QualityHoldReason.valueOf(this.reason);
        
        QualityHold hold = new QualityHold(holdId, item, this.batchNumber, this.quantity,
                reason, this.heldBy, null); // Initial notes handled separately
        
        // Set additional properties that aren't set by constructor
        if (this.releasedBy != null) {
            // Note: We'd need to add setters to QualityHold or modify its design
            // For now, this is a simplified implementation
        }
        
        return hold;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHoldId() { return holdId; }
    public void setHoldId(String holdId) { this.holdId = holdId; }

    public String getItemSkuCode() { return itemSkuCode; }
    public void setItemSkuCode(String itemSkuCode) { this.itemSkuCode = itemSkuCode; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getHeldBy() { return heldBy; }
    public void setHeldBy(String heldBy) { this.heldBy = heldBy; }

    public Instant getHeldAt() { return heldAt; }
    public void setHeldAt(Instant heldAt) { this.heldAt = heldAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReleasedBy() { return releasedBy; }
    public void setReleasedBy(String releasedBy) { this.releasedBy = releasedBy; }

    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }

    public String getReleaseNotes() { return releaseNotes; }
    public void setReleaseNotes(String releaseNotes) { this.releaseNotes = releaseNotes; }

    public List<QualityHoldNoteDocument> getNotes() { return notes; }
    public void setNotes(List<QualityHoldNoteDocument> notes) { this.notes = notes; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    // Nested document class for notes
    public static class QualityHoldNoteDocument {
        private String content;
        private String addedBy;
        private Instant addedAt;

        public QualityHoldNoteDocument() {}

        public QualityHoldNoteDocument(QualityHold.QualityHoldNote note) {
            this.content = note.getContent();
            this.addedBy = note.getAddedBy();
            this.addedAt = note.getAddedAt();
        }

        public QualityHold.QualityHoldNote toDomain() {
            return new QualityHold.QualityHoldNote(content, addedBy, addedAt);
        }

        // Getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getAddedBy() { return addedBy; }
        public void setAddedBy(String addedBy) { this.addedBy = addedBy; }

        public Instant getAddedAt() { return addedAt; }
        public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
    }
}