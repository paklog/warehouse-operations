package com.paklog.warehouse.application.mobile.dto;

import com.paklog.warehouse.domain.work.WorkType;
import com.paklog.warehouse.domain.work.WorkStatus;
import com.paklog.warehouse.domain.shared.Priority;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class MobileWorkDetailDto {
    private String workId;
    private WorkType workType;
    private WorkStatus status;
    private Priority priority;
    private String location;
    private String item;
    private String itemDescription;
    private int quantity;
    private String assignedTo;
    private Instant assignedAt;
    private Instant startedAt;
    private Instant dueDate;
    private List<MobileStepDto> steps;
    private int currentStepNumber;
    private boolean isUrgent;
    private String description;
    private Map<String, Object> attributes;
    private List<String> instructions;
    private String nextAction;

    public MobileWorkDetailDto() {}

    public MobileWorkDetailDto(String workId, WorkType workType, WorkStatus status, 
                              Priority priority, String location, String item, 
                              String itemDescription, int quantity, String assignedTo, 
                              Instant assignedAt, Instant startedAt, Instant dueDate, 
                              List<MobileStepDto> steps, int currentStepNumber, 
                              boolean isUrgent, String description, 
                              Map<String, Object> attributes, List<String> instructions, 
                              String nextAction) {
        this.workId = workId;
        this.workType = workType;
        this.status = status;
        this.priority = priority;
        this.location = location;
        this.item = item;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.assignedTo = assignedTo;
        this.assignedAt = assignedAt;
        this.startedAt = startedAt;
        this.dueDate = dueDate;
        this.steps = steps;
        this.currentStepNumber = currentStepNumber;
        this.isUrgent = isUrgent;
        this.description = description;
        this.attributes = attributes;
        this.instructions = instructions;
        this.nextAction = nextAction;
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

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }

    public List<MobileStepDto> getSteps() { return steps; }
    public void setSteps(List<MobileStepDto> steps) { this.steps = steps; }

    public int getCurrentStepNumber() { return currentStepNumber; }
    public void setCurrentStepNumber(int currentStepNumber) { this.currentStepNumber = currentStepNumber; }

    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }

    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }

    public MobileStepDto getCurrentStep() {
        if (steps == null || currentStepNumber < 1 || currentStepNumber > steps.size()) {
            return null;
        }
        return steps.get(currentStepNumber - 1);
    }

    public double getProgressPercentage() {
        if (steps == null || steps.isEmpty()) return 0.0;
        long completedSteps = steps.stream()
            .mapToLong(step -> step.isCompleted() ? 1 : 0)
            .sum();
        return (double) completedSteps / steps.size() * 100.0;
    }

    public boolean isOverdue() {
        return dueDate != null && Instant.now().isAfter(dueDate);
    }
}