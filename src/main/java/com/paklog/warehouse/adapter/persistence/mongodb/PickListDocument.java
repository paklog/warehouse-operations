package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.picklist.PickInstruction;
import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.picklist.PickListId;
import com.paklog.warehouse.domain.picklist.PickListStatus;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Document(collection = "picklists")
@CompoundIndexes({
    @CompoundIndex(name = "orderId_status_idx", def = "{'orderId': 1, 'status': 1}"),
    @CompoundIndex(name = "assignedPickerId_status_idx", def = "{'assignedPickerId': 1, 'status': 1}"),
    @CompoundIndex(name = "status_createdAt_idx", def = "{'status': 1, 'createdAt': 1}"),
    @CompoundIndex(name = "status_priority_idx", def = "{'status': 1, 'priority': -1}")
})
public class PickListDocument {

    @Id
    private String id;
    
    @Indexed
    private String orderId;
    
    @Indexed
    private PickListStatus status;
    
    private List<PickInstructionDocument> instructions;
    
    @Indexed
    private String assignedPickerId;
    
    private Date assignedAt;
    
    @CreatedDate
    private Date createdAt;
    
    @LastModifiedDate
    private Date updatedAt;
    
    private Date completedAt;
    
    @Version
    private Long version;
    
    // Summary fields for quick access without scanning instructions array
    private int totalInstructions;
    private int completedInstructions;
    private boolean allInstructionsCompleted;
    
    // Audit fields
    private String createdBy;
    private String assignedBy;
    private String updatedBy;
    
    // Operational fields for warehouse optimization
    @Indexed
    private String warehouseZone;
    
    @Indexed
    private Integer priority = 0; // Default normal priority
    
    // Estimated time fields
    private Integer estimatedPickTimeMinutes;
    private Date estimatedCompletionTime;

    public PickListDocument() {}

    public PickListDocument(PickList pickList) {
        this.id = pickList.getId().getValue().toString();
        this.orderId = pickList.getOrderId().getValue().toString();
        this.status = pickList.getStatus();
        this.instructions = pickList.getInstructions().stream()
                .map(PickInstructionDocument::new)
                .collect(Collectors.toList());
        this.assignedPickerId = pickList.getAssignedPickerId();
        this.assignedAt = pickList.getAssignedAt() != null ? Date.from(pickList.getAssignedAt()) : null;
        this.completedAt = pickList.getCompletedAt() != null ? Date.from(pickList.getCompletedAt()) : null;
        
        // Set summary fields
        this.updateInstructionSummary();
        
        // Audit fields will be set by Spring Data MongoDB
    }

    public PickList toDomain() {
        List<PickInstruction> domainInstructions = instructions.stream()
                .map(PickInstructionDocument::toDomain)
                .collect(Collectors.toList());

        PickList pickList = new PickList(
                PickListId.of(id),
                OrderId.of(orderId),
                domainInstructions
        );

        pickList.setStatus(status);
        if (assignedPickerId != null) {
            pickList.assignTo(assignedPickerId);
        }
        if (createdAt != null) {
            pickList.setCreatedAt(createdAt.toInstant());
        }
        if (completedAt != null) {
            pickList.setCompletedAt(completedAt.toInstant());
        }

        return pickList;
    }

    // Helper method to update summary fields
    public void updateInstructionSummary() {
        if (instructions != null && !instructions.isEmpty()) {
            this.totalInstructions = instructions.size();
            this.completedInstructions = (int) instructions.stream()
                    .mapToLong(i -> i.completed ? 1 : 0)
                    .sum();
            this.allInstructionsCompleted = (completedInstructions == totalInstructions);
        } else {
            this.totalInstructions = 0;
            this.completedInstructions = 0;
            this.allInstructionsCompleted = false;
        }
    }

    // Inner class for PickInstruction
    public static class PickInstructionDocument {
        private String skuCode;
        private int quantity;
        private String binLocation;
        private boolean completed;
        
        // Additional fields for better warehouse operations
        private String pickSequence; // For optimized picking routes
        private Date completedAt;
        private String actualBinLocation; // Where item was actually found
        private Integer actualQuantity; // What was actually picked

        public PickInstructionDocument() {}

        public PickInstructionDocument(PickInstruction instruction) {
            this.skuCode = instruction.getSku().getValue();
            this.quantity = instruction.getQuantity().getValue();
            this.binLocation = instruction.getBinLocation().getLocation();
            this.completed = instruction.isCompleted();
        }

        public PickInstruction toDomain() {
            PickInstruction instruction = new PickInstruction(
                    new SkuCode(skuCode),
                    new Quantity(quantity),
                    BinLocation.of(binLocation)
            );
            if (completed) {
                instruction.markCompleted();
            }
            return instruction;
        }

        // Getters and setters
        public String getSkuCode() {
            return skuCode;
        }

        public void setSkuCode(String skuCode) {
            this.skuCode = skuCode;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getBinLocation() {
            return binLocation;
        }

        public void setBinLocation(String binLocation) {
            this.binLocation = binLocation;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public String getPickSequence() {
            return pickSequence;
        }

        public void setPickSequence(String pickSequence) {
            this.pickSequence = pickSequence;
        }

        public Date getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(Date completedAt) {
            this.completedAt = completedAt;
        }

        public String getActualBinLocation() {
            return actualBinLocation;
        }

        public void setActualBinLocation(String actualBinLocation) {
            this.actualBinLocation = actualBinLocation;
        }

        public Integer getActualQuantity() {
            return actualQuantity;
        }

        public void setActualQuantity(Integer actualQuantity) {
            this.actualQuantity = actualQuantity;
        }
    }

    // Getters and setters for main class
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PickListStatus getStatus() {
        return status;
    }

    public void setStatus(PickListStatus status) {
        this.status = status;
    }

    public List<PickInstructionDocument> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<PickInstructionDocument> instructions) {
        this.instructions = instructions;
    }

    public String getAssignedPickerId() {
        return assignedPickerId;
    }

    public void setAssignedPickerId(String assignedPickerId) {
        this.assignedPickerId = assignedPickerId;
    }

    public Date getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Date assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public int getTotalInstructions() {
        return totalInstructions;
    }

    public void setTotalInstructions(int totalInstructions) {
        this.totalInstructions = totalInstructions;
    }

    public int getCompletedInstructions() {
        return completedInstructions;
    }

    public void setCompletedInstructions(int completedInstructions) {
        this.completedInstructions = completedInstructions;
    }

    public boolean isAllInstructionsCompleted() {
        return allInstructionsCompleted;
    }

    public void setAllInstructionsCompleted(boolean allInstructionsCompleted) {
        this.allInstructionsCompleted = allInstructionsCompleted;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getWarehouseZone() {
        return warehouseZone;
    }

    public void setWarehouseZone(String warehouseZone) {
        this.warehouseZone = warehouseZone;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getEstimatedPickTimeMinutes() {
        return estimatedPickTimeMinutes;
    }

    public void setEstimatedPickTimeMinutes(Integer estimatedPickTimeMinutes) {
        this.estimatedPickTimeMinutes = estimatedPickTimeMinutes;
    }

    public Date getEstimatedCompletionTime() {
        return estimatedCompletionTime;
    }

    public void setEstimatedCompletionTime(Date estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PickListDocument that = (PickListDocument) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}