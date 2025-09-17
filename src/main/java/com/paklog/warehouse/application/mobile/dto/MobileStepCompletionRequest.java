package com.paklog.warehouse.application.mobile.dto;

import java.util.List;
import java.util.Map;

public class MobileStepCompletionRequest {
    private String workerId;
    private List<String> scannedCodes;
    private Map<String, Object> parameters;
    private String notes;
    private boolean forceComplete;
    private int actualQuantity;
    private String actualLocation;

    public MobileStepCompletionRequest() {}

    public MobileStepCompletionRequest(String workerId, List<String> scannedCodes, 
                                     Map<String, Object> parameters, String notes, 
                                     boolean forceComplete, int actualQuantity, 
                                     String actualLocation) {
        this.workerId = workerId;
        this.scannedCodes = scannedCodes;
        this.parameters = parameters;
        this.notes = notes;
        this.forceComplete = forceComplete;
        this.actualQuantity = actualQuantity;
        this.actualLocation = actualLocation;
    }

    // Getters and setters
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public List<String> getScannedCodes() { return scannedCodes; }
    public void setScannedCodes(List<String> scannedCodes) { this.scannedCodes = scannedCodes; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isForceComplete() { return forceComplete; }
    public void setForceComplete(boolean forceComplete) { this.forceComplete = forceComplete; }

    public int getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(int actualQuantity) { this.actualQuantity = actualQuantity; }

    public String getActualLocation() { return actualLocation; }
    public void setActualLocation(String actualLocation) { this.actualLocation = actualLocation; }
}