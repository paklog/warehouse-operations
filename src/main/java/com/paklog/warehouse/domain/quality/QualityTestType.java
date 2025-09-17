package com.paklog.warehouse.domain.quality;

public enum QualityTestType {
    VISUAL_INSPECTION("Visual inspection", false, true),
    MEASUREMENT("Numeric measurement", true, false),
    WEIGHT("Weight measurement", true, false),
    DIMENSION("Dimension measurement", true, false),
    APPEARANCE("Appearance check", false, true),
    FUNCTIONALITY("Functionality test", false, true),
    PACKAGING("Packaging inspection", false, true),
    LABELING("Label verification", false, true),
    DOCUMENTATION("Documentation check", false, true),
    CHEMICAL_TEST("Chemical analysis", true, false),
    TEMPERATURE("Temperature measurement", true, false),
    PRESSURE("Pressure test", true, false),
    ELECTRICAL("Electrical test", true, false),
    MECHANICAL("Mechanical test", true, false);

    private final String description;
    private final boolean requiresNumericValue;
    private final boolean requiresVisualAssessment;

    QualityTestType(String description, boolean requiresNumericValue, boolean requiresVisualAssessment) {
        this.description = description;
        this.requiresNumericValue = requiresNumericValue;
        this.requiresVisualAssessment = requiresVisualAssessment;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresNumericValue() {
        return requiresNumericValue;
    }

    public boolean requiresVisualAssessment() {
        return requiresVisualAssessment;
    }
}