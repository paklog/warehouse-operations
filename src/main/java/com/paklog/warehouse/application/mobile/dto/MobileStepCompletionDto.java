package com.paklog.warehouse.application.mobile.dto;

import java.time.Instant;
import java.util.List;

public class MobileStepCompletionDto {
    private boolean success;
    private String message;
    private int completedStepNumber;
    private Integer nextStepNumber;
    private boolean workCompleted;
    private List<String> warnings;
    private List<String> validationErrors;
    private Instant completedAt;
    private String nextAction;

    public MobileStepCompletionDto() {}

    public MobileStepCompletionDto(boolean success, String message, int completedStepNumber, 
                                  Integer nextStepNumber, boolean workCompleted, 
                                  List<String> warnings, List<String> validationErrors, 
                                  Instant completedAt, String nextAction) {
        this.success = success;
        this.message = message;
        this.completedStepNumber = completedStepNumber;
        this.nextStepNumber = nextStepNumber;
        this.workCompleted = workCompleted;
        this.warnings = warnings;
        this.validationErrors = validationErrors;
        this.completedAt = completedAt;
        this.nextAction = nextAction;
    }

    public static MobileStepCompletionDto success(int completedStep, Integer nextStep, 
                                                boolean workCompleted, String message) {
        return new MobileStepCompletionDto(true, message, completedStep, nextStep, 
                                         workCompleted, null, null, Instant.now(), 
                                         nextStep != null ? "NEXT_STEP" : "COMPLETE_WORK");
    }

    public static MobileStepCompletionDto failure(String message, List<String> validationErrors) {
        return new MobileStepCompletionDto(false, message, -1, null, false, 
                                         null, validationErrors, null, "RETRY");
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getCompletedStepNumber() { return completedStepNumber; }
    public void setCompletedStepNumber(int completedStepNumber) { this.completedStepNumber = completedStepNumber; }

    public Integer getNextStepNumber() { return nextStepNumber; }
    public void setNextStepNumber(Integer nextStepNumber) { this.nextStepNumber = nextStepNumber; }

    public boolean isWorkCompleted() { return workCompleted; }
    public void setWorkCompleted(boolean workCompleted) { this.workCompleted = workCompleted; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public List<String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }
}