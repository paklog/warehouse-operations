package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LocationContext {
    private final BinLocation location;
    private final SkuCode item;
    private final Map<String, Object> attributes;
    private final Set<String> availableEquipment;

    public LocationContext(BinLocation location, SkuCode item, 
                          Map<String, Object> attributes, Set<String> availableEquipment) {
        this.location = Objects.requireNonNull(location, "Location cannot be null");
        this.item = item; // Can be null for general location queries
        this.attributes = new HashMap<>(Objects.requireNonNull(attributes, "Attributes cannot be null"));
        this.availableEquipment = Set.copyOf(Objects.requireNonNull(availableEquipment, "Equipment set cannot be null"));
    }

    public BinLocation getLocation() {
        return location;
    }

    public SkuCode getItem() {
        return item;
    }

    public String getLocationZone() {
        return getAttributeAsString("zone");
    }

    public Double getAvailableCapacity() {
        return getAttributeAsDouble("available_capacity");
    }

    public String getAccessibilityLevel() {
        return getAttributeAsString("accessibility");
    }

    public String getSafetyLevel() {
        return getAttributeAsString("safety_level");
    }

    public Double getTemperature() {
        return getAttributeAsDouble("temperature");
    }

    public Boolean isHazmatCompatible() {
        return getAttributeAsBoolean("hazmat_compatible");
    }

    public Integer getAvailableInventory() {
        return getAttributeAsInteger("available_inventory");
    }

    public Double getMaxWeight() {
        return getAttributeAsDouble("max_weight");
    }

    public Double getMaxHeight() {
        return getAttributeAsDouble("max_height");
    }

    public boolean hasEquipment(String equipment) {
        return availableEquipment.contains(equipment);
    }

    public Set<String> getAvailableEquipment() {
        return availableEquipment;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public String getAttributeAsString(String key) {
        Object value = attributes.get(key);
        return value != null ? value.toString() : null;
    }

    public Integer getAttributeAsInteger(String key) {
        Object value = attributes.get(key);
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

    public Double getAttributeAsDouble(String key) {
        Object value = attributes.get(key);
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

    public Boolean getAttributeAsBoolean(String key) {
        Object value = attributes.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    public Map<String, Object> getAllAttributes() {
        return new HashMap<>(attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationContext that = (LocationContext) o;
        return Objects.equals(location, that.location) &&
               Objects.equals(item, that.item) &&
               Objects.equals(attributes, that.attributes) &&
               Objects.equals(availableEquipment, that.availableEquipment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, item, attributes, availableEquipment);
    }

    @Override
    public String toString() {
        return "LocationContext{" +
                "location=" + location +
                ", item=" + item +
                ", zone=" + getLocationZone() +
                ", capacity=" + getAvailableCapacity() +
                '}';
    }
}