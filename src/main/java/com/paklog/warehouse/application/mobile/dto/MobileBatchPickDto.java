package com.paklog.warehouse.application.mobile.dto;

import java.util.List;
import java.util.Map;

public class MobileBatchPickDto {
    private String batchId;
    private String workerId;
    private List<MobileWorkSummaryDto> assignedWork;
    private String optimizedRoute;
    private List<String> routeStops;
    private int totalItems;
    private int estimatedDurationMinutes;
    private Map<String, Object> batchAttributes;
    private List<String> specialInstructions;
    private String status;

    public MobileBatchPickDto() {}

    public MobileBatchPickDto(String batchId, String workerId, List<MobileWorkSummaryDto> assignedWork, 
                             String optimizedRoute, List<String> routeStops, int totalItems, 
                             int estimatedDurationMinutes, Map<String, Object> batchAttributes, 
                             List<String> specialInstructions, String status) {
        this.batchId = batchId;
        this.workerId = workerId;
        this.assignedWork = assignedWork;
        this.optimizedRoute = optimizedRoute;
        this.routeStops = routeStops;
        this.totalItems = totalItems;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.batchAttributes = batchAttributes;
        this.specialInstructions = specialInstructions;
        this.status = status;
    }

    // Getters and setters
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public List<MobileWorkSummaryDto> getAssignedWork() { return assignedWork; }
    public void setAssignedWork(List<MobileWorkSummaryDto> assignedWork) { this.assignedWork = assignedWork; }

    public String getOptimizedRoute() { return optimizedRoute; }
    public void setOptimizedRoute(String optimizedRoute) { this.optimizedRoute = optimizedRoute; }

    public List<String> getRouteStops() { return routeStops; }
    public void setRouteStops(List<String> routeStops) { this.routeStops = routeStops; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public Map<String, Object> getBatchAttributes() { return batchAttributes; }
    public void setBatchAttributes(Map<String, Object> batchAttributes) { this.batchAttributes = batchAttributes; }

    public List<String> getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(List<String> specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}