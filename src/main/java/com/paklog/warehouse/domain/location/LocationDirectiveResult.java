package com.paklog.warehouse.domain.location;

import java.util.List;
import java.util.Objects;

public class LocationDirectiveResult {
    private final ResultType type;
    private final double score;
    private final String message;
    private final List<String> violations;

    private LocationDirectiveResult(ResultType type, double score, String message, List<String> violations) {
        this.type = Objects.requireNonNull(type, "Result type cannot be null");
        this.score = score;
        this.message = message;
        this.violations = violations != null ? List.copyOf(violations) : List.of();
    }

    public static LocationDirectiveResult suitable(double score) {
        return new LocationDirectiveResult(ResultType.SUITABLE, score, null, null);
    }

    public static LocationDirectiveResult notApplicable(String message) {
        return new LocationDirectiveResult(ResultType.NOT_APPLICABLE, 0.0, message, null);
    }

    public static LocationDirectiveResult constraintViolation(List<String> violations) {
        String message = "Constraint violations: " + String.join(", ", violations);
        return new LocationDirectiveResult(ResultType.CONSTRAINT_VIOLATION, 0.0, message, violations);
    }

    public static LocationDirectiveResult error(String message) {
        return new LocationDirectiveResult(ResultType.ERROR, 0.0, message, null);
    }

    public ResultType getType() {
        return type;
    }

    public double getScore() {
        return score;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getViolations() {
        return violations;
    }

    public boolean isSuitable() {
        return type == ResultType.SUITABLE;
    }

    public boolean isNotApplicable() {
        return type == ResultType.NOT_APPLICABLE;
    }

    public boolean hasConstraintViolations() {
        return type == ResultType.CONSTRAINT_VIOLATION;
    }

    public boolean hasError() {
        return type == ResultType.ERROR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationDirectiveResult that = (LocationDirectiveResult) o;
        return Double.compare(that.score, score) == 0 &&
               type == that.type &&
               Objects.equals(message, that.message) &&
               Objects.equals(violations, that.violations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, score, message, violations);
    }

    @Override
    public String toString() {
        return "LocationDirectiveResult{" +
                "type=" + type +
                ", score=" + score +
                ", message='" + message + '\'' +
                ", violations=" + violations.size() +
                '}';
    }

    public enum ResultType {
        SUITABLE,
        NOT_APPLICABLE,
        CONSTRAINT_VIOLATION,
        ERROR
    }
}