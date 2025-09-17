package com.paklog.warehouse.application.mobile.dto;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class MobileWorkCompletionDto {
    private boolean success;
    private String workId;
    private String message;
    private Instant completedAt;
    private Duration totalDuration;
    private int totalSteps;
    private List<String> completionNotes;
    private double performanceScore;
    private String nextRecommendedAction;
    private List<String> warnings;

    public MobileWorkCompletionDto() {}

    public MobileWorkCompletionDto(boolean success, String workId, String message, 
                                  Instant completedAt, Duration totalDuration, 
                                  int totalSteps, List<String> completionNotes, 
                                  double performanceScore, String nextRecommendedAction, 
                                  List<String> warnings) {
        this.success = success;
        this.workId = workId;
        this.message = message;
        this.completedAt = completedAt;
        this.totalDuration = totalDuration;
        this.totalSteps = totalSteps;
        this.completionNotes = completionNotes;
        this.performanceScore = performanceScore;
        this.nextRecommendedAction = nextRecommendedAction;
        this.warnings = warnings;
    }

    public static MobileWorkCompletionDto success(String workId, Instant completedAt, 
                                                Duration totalDuration, int totalSteps, 
                                                double performanceScore) {
        return new MobileWorkCompletionDto(true, workId, "Work completed successfully", 
                                         completedAt, totalDuration, totalSteps, null, 
                                         performanceScore, "GET_NEXT_WORK", null);
    }

    public static MobileWorkCompletionDto failure(String workId, String message) {
        return new MobileWorkCompletionDto(false, workId, message, null, null, 0, 
                                         null, 0.0, "RETRY", null);
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Duration getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Duration totalDuration) { this.totalDuration = totalDuration; }

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public List<String> getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(List<String> completionNotes) { this.completionNotes = completionNotes; }

    public double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(double performanceScore) { this.performanceScore = performanceScore; }

    public String getNextRecommendedAction() { return nextRecommendedAction; }
    public void setNextRecommendedAction(String nextRecommendedAction) { this.nextRecommendedAction = nextRecommendedAction; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}