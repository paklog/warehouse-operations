package com.paklog.warehouse.application.mobile.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class MobileStepDto {
    private int stepNumber;
    private String name;
    private String instruction;
    private String location;
    private String action;
    private Map<String, Object> parameters;
    private boolean completed;
    private boolean skippable;
    private boolean requiresScan;
    private List<String> expectedScans;
    private String validationRule;
    private Instant completedAt;
    private String completedBy;
    private String notes;
    private int estimatedDurationMinutes;

    public MobileStepDto() {}

    public MobileStepDto(int stepNumber, String name, String instruction, String location, 
                        String action, Map<String, Object> parameters, boolean completed, 
                        boolean skippable, boolean requiresScan, List<String> expectedScans, 
                        String validationRule, Instant completedAt, String completedBy, 
                        String notes, int estimatedDurationMinutes) {
        this.stepNumber = stepNumber;
        this.name = name;
        this.instruction = instruction;
        this.location = location;
        this.action = action;
        this.parameters = parameters;
        this.completed = completed;
        this.skippable = skippable;
        this.requiresScan = requiresScan;
        this.expectedScans = expectedScans;
        this.validationRule = validationRule;
        this.completedAt = completedAt;
        this.completedBy = completedBy;
        this.notes = notes;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    // Getters and setters
    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isSkippable() { return skippable; }
    public void setSkippable(boolean skippable) { this.skippable = skippable; }

    public boolean isRequiresScan() { return requiresScan; }
    public void setRequiresScan(boolean requiresScan) { this.requiresScan = requiresScan; }

    public List<String> getExpectedScans() { return expectedScans; }
    public void setExpectedScans(List<String> expectedScans) { this.expectedScans = expectedScans; }

    public String getValidationRule() { return validationRule; }
    public void setValidationRule(String validationRule) { this.validationRule = validationRule; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getCompletedBy() { return completedBy; }
    public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { 
        this.estimatedDurationMinutes = estimatedDurationMinutes; 
    }

    public boolean canBeCompleted() {
        return !completed && (!requiresScan || expectedScans == null || expectedScans.isEmpty());
    }
}