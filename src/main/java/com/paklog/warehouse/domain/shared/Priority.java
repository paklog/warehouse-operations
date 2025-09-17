package com.paklog.warehouse.domain.shared;

public enum Priority {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4),
    CRITICAL(5);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }

    public boolean isLowerThan(Priority other) {
        return this.level < other.level;
    }

    public static Priority fromString(String priorityString) {
        if (priorityString == null) return NORMAL;
        
        return switch (priorityString.toUpperCase()) {
            case "LOW" -> LOW;
            case "NORMAL" -> NORMAL;
            case "HIGH" -> HIGH;
            case "URGENT" -> URGENT;
            case "CRITICAL" -> CRITICAL;
            default -> NORMAL;
        };
    }
}