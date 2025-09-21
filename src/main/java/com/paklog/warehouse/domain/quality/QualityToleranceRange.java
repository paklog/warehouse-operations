package com.paklog.warehouse.domain.quality;

import java.util.Objects;

public class QualityToleranceRange {
    private final double minValue;
    private final double maxValue;
    private final String unit;

    public QualityToleranceRange(double minValue, double maxValue, String unit) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("Min value cannot be greater than max value");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
    }

    public boolean isWithinRange(double value) {
        return value >= minValue && value <= maxValue;
    }

    public boolean isWithinTolerance(String expectedValue, String actualValue) {
        try {
            double expected = Double.parseDouble(expectedValue);
            double actual = Double.parseDouble(actualValue);
            return isWithinRange(actual);
        } catch (NumberFormatException e) {
            return expectedValue.equals(actualValue);
        }
    }

    public double getVariance(double actualValue) {
        if (actualValue < minValue) {
            return minValue - actualValue;
        } else if (actualValue > maxValue) {
            return actualValue - maxValue;
        }
        return 0.0; // Within range
    }

    // Getters
    public double getMinValue() { return minValue; }
    public double getMaxValue() { return maxValue; }
    public String getUnit() { return unit; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityToleranceRange that = (QualityToleranceRange) o;
        return Double.compare(that.minValue, minValue) == 0 &&
               Double.compare(that.maxValue, maxValue) == 0 &&
               Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minValue, maxValue, unit);
    }

    @Override
    public String toString() {
        return String.format("%.2f - %.2f %s", minValue, maxValue, unit);
    }
}