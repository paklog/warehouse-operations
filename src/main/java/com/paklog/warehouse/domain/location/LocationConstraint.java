package com.paklog.warehouse.domain.location;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocationConstraint {
    private final LocationConstraintType type;
    private final String operator;
    private final Object value;
    private final Map<String, Object> parameters;

    public LocationConstraint(LocationConstraintType type, String operator, Object value) {
        this(type, operator, value, new HashMap<>());
    }

    public LocationConstraint(LocationConstraintType type, String operator, Object value,
                            Map<String, Object> parameters) {
        this.type = Objects.requireNonNull(type, "Constraint type cannot be null");
        this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
        this.value = Objects.requireNonNull(value, "Value cannot be null");
        this.parameters = new HashMap<>(Objects.requireNonNull(parameters, "Parameters cannot be null"));
    }

    // Constructor for document compatibility
    public LocationConstraint(LocationConstraintType type, Object parameter, Object value, boolean enabled) {
        this.type = Objects.requireNonNull(type, "Constraint type cannot be null");
        this.operator = parameter != null ? parameter.toString() : "=";
        this.value = Objects.requireNonNull(value, "Value cannot be null");
        this.parameters = new HashMap<>();
    }

    public LocationConstraintType getType() {
        return type;
    }

    public String getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    // For document compatibility
    public Object getParameter() {
        return operator; // Use operator as the parameter for backwards compatibility
    }

    public boolean isEnabled() {
        return true; // Default enabled, could be configurable
    }

    public String getValueAsString() {
        return value != null ? value.toString() : null;
    }

    public Integer getValueAsInteger() {
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

    public Double getValueAsDouble() {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public boolean evaluate(LocationContext context) {
        switch (type) {
            case ZONE_RESTRICTION:
                return evaluateZoneConstraint(context);
            case CAPACITY_REQUIREMENT:
                return evaluateCapacityConstraint(context);
            case ACCESSIBILITY:
                return evaluateAccessibilityConstraint(context);
            case EQUIPMENT_REQUIREMENT:
                return evaluateEquipmentConstraint(context);
            case SAFETY_RESTRICTION:
                return evaluateSafetyConstraint(context);
            case TEMPERATURE_RANGE:
                return evaluateTemperatureConstraint(context);
            case HAZMAT_COMPATIBLE:
                return evaluateHazmatConstraint(context);
            case INVENTORY_AVAILABLE:
                return evaluateInventoryConstraint(context);
            default:
                return true;
        }
    }

    private boolean evaluateZoneConstraint(LocationContext context) {
        String locationZone = context.getLocationZone();
        String requiredZone = getValueAsString();
        
        switch (operator.toLowerCase()) {
            case "equals":
            case "eq":
                return Objects.equals(locationZone, requiredZone);
            case "not_equals":
            case "ne":
                return !Objects.equals(locationZone, requiredZone);
            case "in":
                return requiredZone != null && requiredZone.contains(locationZone);
            default:
                return false;
        }
    }

    private boolean evaluateCapacityConstraint(LocationContext context) {
        Double availableCapacity = context.getAvailableCapacity();
        Double requiredCapacity = getValueAsDouble();
        
        if (availableCapacity == null || requiredCapacity == null) {
            return false;
        }
        
        switch (operator.toLowerCase()) {
            case "greater_than":
            case "gt":
                return availableCapacity > requiredCapacity;
            case "greater_equal":
            case "gte":
                return availableCapacity >= requiredCapacity;
            case "less_than":
            case "lt":
                return availableCapacity < requiredCapacity;
            case "less_equal":
            case "lte":
                return availableCapacity <= requiredCapacity;
            case "equals":
            case "eq":
                return availableCapacity.equals(requiredCapacity);
            default:
                return false;
        }
    }

    private boolean evaluateAccessibilityConstraint(LocationContext context) {
        String accessibility = context.getAccessibilityLevel();
        String required = getValueAsString();
        return Objects.equals(accessibility, required);
    }

    private boolean evaluateEquipmentConstraint(LocationContext context) {
        String equipment = getValueAsString();
        return context.hasEquipment(equipment);
    }

    private boolean evaluateSafetyConstraint(LocationContext context) {
        String safetyLevel = context.getSafetyLevel();
        String required = getValueAsString();
        return Objects.equals(safetyLevel, required);
    }

    private boolean evaluateTemperatureConstraint(LocationContext context) {
        Double temperature = context.getTemperature();
        Double required = getValueAsDouble();
        
        if (temperature == null || required == null) {
            return false;
        }
        
        Double tolerance = (Double) parameters.getOrDefault("tolerance", 0.0);
        return Math.abs(temperature - required) <= tolerance;
    }

    private boolean evaluateHazmatConstraint(LocationContext context) {
        Boolean hazmatCompatible = context.isHazmatCompatible();
        Boolean required = Boolean.valueOf(getValueAsString());
        return Objects.equals(hazmatCompatible, required);
    }

    private boolean evaluateInventoryConstraint(LocationContext context) {
        Integer available = context.getAvailableInventory();
        Integer required = getValueAsInteger();
        
        if (available == null || required == null) {
            return false;
        }
        
        switch (operator.toLowerCase()) {
            case "greater_than":
            case "gt":
                return available > required;
            case "greater_equal":
            case "gte":
                return available >= required;
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationConstraint that = (LocationConstraint) o;
        return type == that.type &&
               Objects.equals(operator, that.operator) &&
               Objects.equals(value, that.value) &&
               Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, operator, value, parameters);
    }

    @Override
    public String toString() {
        return "LocationConstraint{" +
                "type=" + type +
                ", operator='" + operator + '\'' +
                ", value=" + value +
                '}';
    }
}