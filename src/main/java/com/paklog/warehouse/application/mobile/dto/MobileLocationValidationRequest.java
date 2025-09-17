package com.paklog.warehouse.application.mobile.dto;

public class MobileLocationValidationRequest {
    private String workerId;
    private String workId;
    private Integer stepNumber;
    private String location;
    private String workType;
    private String item;
    private int quantity;

    public MobileLocationValidationRequest() {}

    public MobileLocationValidationRequest(String workerId, String workId, Integer stepNumber, 
                                         String location, String workType, String item, 
                                         int quantity) {
        this.workerId = workerId;
        this.workId = workId;
        this.stepNumber = stepNumber;
        this.location = location;
        this.workType = workType;
        this.item = item;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }

    public Integer getStepNumber() { return stepNumber; }
    public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}