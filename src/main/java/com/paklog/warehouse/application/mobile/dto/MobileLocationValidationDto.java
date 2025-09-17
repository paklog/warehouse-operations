package com.paklog.warehouse.application.mobile.dto;

import java.util.List;
import java.util.Map;

public class MobileLocationValidationDto {
    private boolean valid;
    private String location;
    private String message;
    private double suitabilityScore;
    private List<String> violations;
    private List<String> warnings;
    private Map<String, Object> locationAttributes;
    private List<String> suggestions;
    private boolean requiresConfirmation;

    public MobileLocationValidationDto() {}

    public MobileLocationValidationDto(boolean valid, String location, String message, 
                                     double suitabilityScore, List<String> violations, 
                                     List<String> warnings, Map<String, Object> locationAttributes, 
                                     List<String> suggestions, boolean requiresConfirmation) {
        this.valid = valid;
        this.location = location;
        this.message = message;
        this.suitabilityScore = suitabilityScore;
        this.violations = violations;
        this.warnings = warnings;
        this.locationAttributes = locationAttributes;
        this.suggestions = suggestions;
        this.requiresConfirmation = requiresConfirmation;
    }

    public static MobileLocationValidationDto valid(String location, double score, String message) {
        return new MobileLocationValidationDto(true, location, message, score, 
                                             null, null, null, null, false);
    }

    public static MobileLocationValidationDto invalid(String location, String message, 
                                                    List<String> violations) {
        return new MobileLocationValidationDto(false, location, message, 0.0, 
                                             violations, null, null, null, false);
    }

    // Getters and setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public double getSuitabilityScore() { return suitabilityScore; }
    public void setSuitabilityScore(double suitabilityScore) { this.suitabilityScore = suitabilityScore; }

    public List<String> getViolations() { return violations; }
    public void setViolations(List<String> violations) { this.violations = violations; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public Map<String, Object> getLocationAttributes() { return locationAttributes; }
    public void setLocationAttributes(Map<String, Object> locationAttributes) { this.locationAttributes = locationAttributes; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public boolean isRequiresConfirmation() { return requiresConfirmation; }
    public void setRequiresConfirmation(boolean requiresConfirmation) { this.requiresConfirmation = requiresConfirmation; }
}