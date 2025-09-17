package com.paklog.warehouse.application.mobile.dto;

public class MobileScanRequest {
    private String workerId;
    private String workId;
    private Integer stepNumber;
    private String scannedCode;
    private String scanType; // BARCODE, QR_CODE, RFID
    private String expectedType; // ITEM, LOCATION, LICENSE_PLATE
    private String context; // Additional context for validation

    public MobileScanRequest() {}

    public MobileScanRequest(String workerId, String workId, Integer stepNumber, 
                           String scannedCode, String scanType, String expectedType, 
                           String context) {
        this.workerId = workerId;
        this.workId = workId;
        this.stepNumber = stepNumber;
        this.scannedCode = scannedCode;
        this.scanType = scanType;
        this.expectedType = expectedType;
        this.context = context;
    }

    // Getters and setters
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }

    public Integer getStepNumber() { return stepNumber; }
    public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }

    public String getScannedCode() { return scannedCode; }
    public void setScannedCode(String scannedCode) { this.scannedCode = scannedCode; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }

    public String getExpectedType() { return expectedType; }
    public void setExpectedType(String expectedType) { this.expectedType = expectedType; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}