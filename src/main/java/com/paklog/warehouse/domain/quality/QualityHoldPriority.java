package com.paklog.warehouse.domain.quality;

// Quality Hold Priority
public enum QualityHoldPriority {
    LOW(1, "Low priority"),
    MEDIUM(2, "Medium priority"),
    HIGH(3, "High priority"),
    CRITICAL(4, "Critical priority");

    private final int level;
    private final String description;

    QualityHoldPriority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() { return level; }
    public String getDescription() { return description; }

    public boolean isHigherThan(QualityHoldPriority other) {
        return this.level > other.level;
    }
}