package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.work.WorkType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LocationQuery {
    private final WorkType workType;
    private final SkuCode item;
    private final Quantity quantity;
    private final BinLocation referenceLocation;
    private final Map<String, Object> queryParameters;
    private final List<BinLocation> candidateLocations;

    public LocationQuery(WorkType workType, SkuCode item, Quantity quantity) {
        this(workType, item, quantity, null, new HashMap<>(), null);
    }

    public LocationQuery(WorkType workType, SkuCode item, Quantity quantity,
                        BinLocation referenceLocation, Map<String, Object> queryParameters,
                        List<BinLocation> candidateLocations) {
        this.workType = Objects.requireNonNull(workType, "Work type cannot be null");
        this.item = Objects.requireNonNull(item, "Item cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
        this.referenceLocation = referenceLocation;
        this.queryParameters = new HashMap<>(Objects.requireNonNull(queryParameters, "Parameters cannot be null"));
        this.candidateLocations = candidateLocations != null ? List.copyOf(candidateLocations) : null;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public SkuCode getItem() {
        return item;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public BinLocation getReferenceLocation() {
        return referenceLocation;
    }

    public Map<String, Object> getQueryParameters() {
        return new HashMap<>(queryParameters);
    }

    public List<BinLocation> getCandidateLocations() {
        return candidateLocations;
    }

    public Object getParameter(String key) {
        return queryParameters.get(key);
    }

    public String getParameterAsString(String key) {
        Object value = queryParameters.get(key);
        return value != null ? value.toString() : null;
    }

    public Integer getParameterAsInteger(String key) {
        Object value = queryParameters.get(key);
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

    public Double getParameterAsDouble(String key) {
        Object value = queryParameters.get(key);
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

    public Boolean getParameterAsBoolean(String key) {
        Object value = queryParameters.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    public String getRequiredZone() {
        return getParameterAsString("required_zone");
    }

    public Double getMinimumCapacity() {
        return getParameterAsDouble("minimum_capacity");
    }

    public String getAccessibilityLevel() {
        return getParameterAsString("accessibility_level");
    }

    public Boolean requiresSpecialEquipment() {
        return getParameterAsBoolean("requires_special_equipment");
    }

    public boolean hasCandidateLocations() {
        return candidateLocations != null && !candidateLocations.isEmpty();
    }

    public LocationContext createContextForLocation(BinLocation location) {
        return createContextForLocation(location, new HashMap<>());
    }

    public LocationContext createContextForLocation(BinLocation location, Map<String, Object> locationAttributes) {
        Map<String, Object> contextAttributes = new HashMap<>();
        
        // Add query parameters as context attributes
        contextAttributes.putAll(queryParameters);
        
        // Add location-specific attributes
        contextAttributes.putAll(locationAttributes);
        
        // Add default attributes based on location structure
        contextAttributes.put("aisle", location.getAisle());
        contextAttributes.put("rack", location.getRack());
        contextAttributes.put("level", location.getLevel());
        
        // Add work type context
        contextAttributes.put("work_type", workType.toString());
        
        // Default equipment set - in real implementation would come from location service
        Set<String> equipment = new HashSet<>();
        equipment.add("scanner");
        equipment.add("printer");
        
        return new LocationContext(location, item, contextAttributes, equipment);
    }

    public boolean isPickQuery() {
        return workType == WorkType.PICK;
    }

    public boolean isPutQuery() {
        return workType == WorkType.PUT;
    }

    public boolean isCountQuery() {
        return workType == WorkType.COUNT;
    }

    public boolean isMoveQuery() {
        return workType == WorkType.MOVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationQuery that = (LocationQuery) o;
        return workType == that.workType &&
               Objects.equals(item, that.item) &&
               Objects.equals(quantity, that.quantity) &&
               Objects.equals(referenceLocation, that.referenceLocation) &&
               Objects.equals(queryParameters, that.queryParameters) &&
               Objects.equals(candidateLocations, that.candidateLocations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workType, item, quantity, referenceLocation, queryParameters, candidateLocations);
    }

    @Override
    public String toString() {
        return "LocationQuery{" +
                "workType=" + workType +
                ", item=" + item +
                ", quantity=" + quantity +
                ", referenceLocation=" + referenceLocation +
                ", candidateCount=" + (candidateLocations != null ? candidateLocations.size() : 0) +
                '}';
    }
}