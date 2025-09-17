package com.paklog.warehouse.application.mobile.dto;

import com.paklog.warehouse.domain.work.WorkType;
import com.paklog.warehouse.domain.work.WorkStatus;
import com.paklog.warehouse.domain.shared.Priority;

import java.time.Instant;

public class MobileWorkSummaryDto {
    private String workId;
    private WorkType workType;
    private WorkStatus status;
    private Priority priority;
    private String location;
    private String item;
    private int quantity;
    private String assignedTo;
    private Instant assignedAt;
    private Instant dueDate;
    private int totalSteps;
    private int completedSteps;
    private boolean isUrgent;
    private String description;

    public MobileWorkSummaryDto() {}

    public MobileWorkSummaryDto(String workId, WorkType workType, WorkStatus status, 
                               Priority priority, String location, String item, 
                               int quantity, String assignedTo, Instant assignedAt, 
                               Instant dueDate, int totalSteps, int completedSteps, 
                               boolean isUrgent, String description) {
        this.workId = workId;
        this.workType = workType;
        this.status = status;
        this.priority = priority;
        this.location = location;
        this.item = item;
        this.quantity = quantity;
        this.assignedTo = assignedTo;
        this.assignedAt = assignedAt;
        this.dueDate = dueDate;
        this.totalSteps = totalSteps;
        this.completedSteps = completedSteps;
        this.isUrgent = isUrgent;
        this.description = description;
    }

    // Getters and setters
    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }

    public WorkType getWorkType() { return workType; }
    public void setWorkType(WorkType workType) { this.workType = workType; }

    public WorkStatus getStatus() { return status; }
    public void setStatus(WorkStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public int getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(int completedSteps) { this.completedSteps = completedSteps; }

    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getProgressPercentage() {
        if (totalSteps == 0) return 0.0;
        return (double) completedSteps / totalSteps * 100.0;
    }

    public boolean isOverdue() {
        return dueDate != null && Instant.now().isAfter(dueDate);
    }
}