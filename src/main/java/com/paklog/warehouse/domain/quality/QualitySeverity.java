package com.paklog.warehouse.domain.quality;

// Quality Severity
public enum QualitySeverity {
    LOW(1, "Minor issue, minimal impact"),
    MEDIUM(2, "Moderate issue, some impact"),
    HIGH(3, "Significant issue, major impact"),
    CRITICAL(4, "Critical issue, severe impact");

    private final int level;
    private final String description;

    QualitySeverity(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHigherThan(QualitySeverity other) {
        return this.level > other.level;
    }
}