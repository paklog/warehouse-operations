package com.paklog.warehouse.application.mobile.dto;

import java.time.Instant;
import java.util.Map;

public class MobileWorkerMetricsDto {
    private String workerId;
    private String period;
    private int completedTasks;
    private int totalTasks;
    private double completionRate;
    private double averageTaskDurationMinutes;
    private double performanceScore;
    private int totalItemsPicked;
    private int totalItemsPacked;
    private int totalItemsCounted;
    private double accuracyRate;
    private Instant lastActivity;
    private Map<String, Integer> taskTypeBreakdown;
    private Map<String, Double> performanceByType;

    public MobileWorkerMetricsDto() {}

    public MobileWorkerMetricsDto(String workerId, String period, int completedTasks, 
                                 int totalTasks, double completionRate, 
                                 double averageTaskDurationMinutes, double performanceScore, 
                                 int totalItemsPicked, int totalItemsPacked, 
                                 int totalItemsCounted, double accuracyRate, 
                                 Instant lastActivity, Map<String, Integer> taskTypeBreakdown, 
                                 Map<String, Double> performanceByType) {
        this.workerId = workerId;
        this.period = period;
        this.completedTasks = completedTasks;
        this.totalTasks = totalTasks;
        this.completionRate = completionRate;
        this.averageTaskDurationMinutes = averageTaskDurationMinutes;
        this.performanceScore = performanceScore;
        this.totalItemsPicked = totalItemsPicked;
        this.totalItemsPacked = totalItemsPacked;
        this.totalItemsCounted = totalItemsCounted;
        this.accuracyRate = accuracyRate;
        this.lastActivity = lastActivity;
        this.taskTypeBreakdown = taskTypeBreakdown;
        this.performanceByType = performanceByType;
    }

    // Getters and setters
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public double getAverageTaskDurationMinutes() { return averageTaskDurationMinutes; }
    public void setAverageTaskDurationMinutes(double averageTaskDurationMinutes) { 
        this.averageTaskDurationMinutes = averageTaskDurationMinutes; 
    }

    public double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(double performanceScore) { this.performanceScore = performanceScore; }

    public int getTotalItemsPicked() { return totalItemsPicked; }
    public void setTotalItemsPicked(int totalItemsPicked) { this.totalItemsPicked = totalItemsPicked; }

    public int getTotalItemsPacked() { return totalItemsPacked; }
    public void setTotalItemsPacked(int totalItemsPacked) { this.totalItemsPacked = totalItemsPacked; }

    public int getTotalItemsCounted() { return totalItemsCounted; }
    public void setTotalItemsCounted(int totalItemsCounted) { this.totalItemsCounted = totalItemsCounted; }

    public double getAccuracyRate() { return accuracyRate; }
    public void setAccuracyRate(double accuracyRate) { this.accuracyRate = accuracyRate; }

    public Instant getLastActivity() { return lastActivity; }
    public void setLastActivity(Instant lastActivity) { this.lastActivity = lastActivity; }

    public Map<String, Integer> getTaskTypeBreakdown() { return taskTypeBreakdown; }
    public void setTaskTypeBreakdown(Map<String, Integer> taskTypeBreakdown) { this.taskTypeBreakdown = taskTypeBreakdown; }

    public Map<String, Double> getPerformanceByType() { return performanceByType; }
    public void setPerformanceByType(Map<String, Double> performanceByType) { this.performanceByType = performanceByType; }
}