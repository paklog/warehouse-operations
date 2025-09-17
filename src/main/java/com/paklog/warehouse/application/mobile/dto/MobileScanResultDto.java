package com.paklog.warehouse.application.mobile.dto;

import java.util.List;
import java.util.Map;

public class MobileScanResultDto {
    private boolean valid;
    private String scannedValue;
    private String resolvedValue;
    private String type; // ITEM, LOCATION, LICENSE_PLATE
    private String message;
    private Map<String, Object> additionalData;
    private List<String> warnings;
    private List<String> suggestions;
    private boolean requiresConfirmation;
    private String confirmationMessage;

    public MobileScanResultDto() {}

    public MobileScanResultDto(boolean valid, String scannedValue, String resolvedValue, 
                              String type, String message, Map<String, Object> additionalData, 
                              List<String> warnings, List<String> suggestions, 
                              boolean requiresConfirmation, String confirmationMessage) {
        this.valid = valid;
        this.scannedValue = scannedValue;
        this.resolvedValue = resolvedValue;
        this.type = type;
        this.message = message;
        this.additionalData = additionalData;
        this.warnings = warnings;
        this.suggestions = suggestions;
        this.requiresConfirmation = requiresConfirmation;
        this.confirmationMessage = confirmationMessage;
    }

    public static MobileScanResultDto valid(String scannedValue, String resolvedValue, 
                                          String type, String message) {
        return new MobileScanResultDto(true, scannedValue, resolvedValue, type, 
                                     message, null, null, null, false, null);
    }

    public static MobileScanResultDto invalid(String scannedValue, String message, 
                                            List<String> suggestions) {
        return new MobileScanResultDto(false, scannedValue, null, null, 
                                     message, null, null, suggestions, false, null);
    }

    // Getters and setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getScannedValue() { return scannedValue; }
    public void setScannedValue(String scannedValue) { this.scannedValue = scannedValue; }

    public String getResolvedValue() { return resolvedValue; }
    public void setResolvedValue(String resolvedValue) { this.resolvedValue = resolvedValue; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getAdditionalData() { return additionalData; }
    public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public boolean isRequiresConfirmation() { return requiresConfirmation; }
    public void setRequiresConfirmation(boolean requiresConfirmation) { this.requiresConfirmation = requiresConfirmation; }

    public String getConfirmationMessage() { return confirmationMessage; }
    public void setConfirmationMessage(String confirmationMessage) { this.confirmationMessage = confirmationMessage; }
}