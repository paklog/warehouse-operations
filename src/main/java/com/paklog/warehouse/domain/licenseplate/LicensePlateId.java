package com.paklog.warehouse.domain.licenseplate;

import java.util.Objects;
import java.util.UUID;

public class LicensePlateId {
    private final String value;

    public LicensePlateId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate ID cannot be null or empty");
        }
        
        // D365 license plate format validation
        if (!isValidFormat(value)) {
            throw new IllegalArgumentException("Invalid license plate format: " + value);
        }
        
        this.value = value.trim().toUpperCase();
    }

    public static LicensePlateId generate() {
        // Generate D365-style license plate ID: LP + 10 alphanumeric characters
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return new LicensePlateId("LP" + uuid.toUpperCase());
    }

    public static LicensePlateId fromString(String value) {
        return new LicensePlateId(value);
    }

    public static LicensePlateId of(String value) {
        return new LicensePlateId(value);
    }

    private static boolean isValidFormat(String value) {
        // LP followed by 8-12 alphanumeric characters
        return value.matches("^LP[A-Z0-9]{8,12}$");
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LicensePlateId that = (LicensePlateId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}