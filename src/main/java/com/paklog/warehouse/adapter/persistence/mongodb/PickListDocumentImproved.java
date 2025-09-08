package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.picklist.PickListStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Improved PickListDocument following MongoDB best practices
 */
@Document(collection = "picklists")
@CompoundIndexes({
    @CompoundIndex(name = "orderId_status_idx", def = "{'orderId': 1, 'status': 1}"),
    @CompoundIndex(name = "assignedPickerId_status_idx", def = "{'assignedPickerId': 1, 'status': 1}"),
    @CompoundIndex(name = "status_createdAt_idx", def = "{'status': 1, 'createdAt': 1}")
})
public class PickListDocumentImproved {

    @Id
    private String id;
    
    @Indexed
    private String orderId;
    
    @Indexed
    private PickListStatus status;
    
    // Embedded instructions - OK since limited in number
    private List<PickInstructionDocument> instructions;
    
    @Indexed
    private String assignedPickerId;
    
    // Use Date for better MongoDB performance and storage efficiency
    private Date assignedAt;
    
    @CreatedDate
    private Date createdAt;
    
    @LastModifiedDate
    private Date updatedAt;
    
    private Date completedAt;
    
    @Version
    private Long version;
    
    // Summary fields for quick access without scanning instructions
    private int totalInstructions;
    private int completedInstructions;
    private boolean allInstructionsCompleted;
    
    // Audit fields
    private String createdBy;
    private String assignedBy;
    
    // Geographic/zone information for picker optimization
    @Indexed
    private String warehouseZone;
    
    // Priority level for picker scheduling
    @Indexed
    private Integer priority = 0; // Default normal priority
    
    public PickListDocumentImproved() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    /**
     * Embedded PickInstruction with proper field naming
     */
    public static class PickInstructionDocument {
        private String skuCode;
        private int quantity;
        private String binLocation;
        private boolean completed;
        
        // Additional fields for better warehouse operations
        private String pickSequence; // For optimized picking routes
        private Date completedAt;
        private String actualBinLocation; // Where item was actually found
        private int actualQuantity; // What was actually picked
        
        public PickInstructionDocument() {}
        
        // Constructors, getters, setters...
    }
    
    // Helper method to update summary fields
    public void updateInstructionSummary() {
        if (instructions != null) {
            this.totalInstructions = instructions.size();
            this.completedInstructions = (int) instructions.stream()
                .mapToInt(i -> i.completed ? 1 : 0)
                .sum();
            this.allInstructionsCompleted = (completedInstructions == totalInstructions);
        }
        this.updatedAt = new Date();
    }
    
    // Getters and setters...
}