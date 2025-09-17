package com.paklog.warehouse.domain.work;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WorkStep {
    private final int sequence;
    private final WorkAction action;
    private final ValidationType validation;
    private final String description;
    private final Map<String, Object> parameters;
    private final boolean mandatory;
    private final boolean skipOnError;

    public WorkStep(int sequence, WorkAction action, ValidationType validation, 
                   String description, Map<String, Object> parameters) {
        this(sequence, action, validation, description, parameters, true, false);
    }

    public WorkStep(int sequence, WorkAction action, ValidationType validation, 
                   String description, Map<String, Object> parameters, 
                   boolean mandatory, boolean skipOnError) {
        if (sequence < 1) {
            throw new IllegalArgumentException("Work step sequence must be positive");
        }
        
        this.sequence = sequence;
        this.action = Objects.requireNonNull(action, "Work action cannot be null");
        this.validation = Objects.requireNonNull(validation, "Validation type cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.parameters = new HashMap<>(Objects.requireNonNull(parameters, "Parameters cannot be null"));
        this.mandatory = mandatory;
        this.skipOnError = skipOnError;
    }

    public int getSequence() {
        return sequence;
    }

    public WorkAction getAction() {
        return action;
    }

    public ValidationType getValidation() {
        return validation;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean canSkipOnError() {
        return skipOnError;
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public String getParameterAsString(String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }

    public Integer getParameterAsInteger(String key) {
        Object value = parameters.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public boolean requiresValidation() {
        return validation != ValidationType.NONE;
    }

    public boolean isSystemAction() {
        return action.isSystemAction();
    }

    public boolean requiresUserInput() {
        return action.requiresUserInput() || validation.requiresInput();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkStep workStep = (WorkStep) o;
        return sequence == workStep.sequence &&
               mandatory == workStep.mandatory &&
               skipOnError == workStep.skipOnError &&
               action == workStep.action &&
               validation == workStep.validation &&
               Objects.equals(description, workStep.description) &&
               Objects.equals(parameters, workStep.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequence, action, validation, description, parameters, mandatory, skipOnError);
    }

    @Override
    public String toString() {
        return "WorkStep{" +
                "sequence=" + sequence +
                ", action=" + action +
                ", validation=" + validation +
                ", description='" + description + '\'' +
                ", mandatory=" + mandatory +
                ", skipOnError=" + skipOnError +
                '}';
    }
}