package com.paklog.warehouse.domain.wave;

import java.util.Objects;
import java.util.UUID;

public class WaveId {
    private final UUID value;

    private WaveId(UUID value) {
        this.value = Objects.requireNonNull(value, "Wave ID cannot be null");
    }

    public static WaveId generate() {
        return new WaveId(UUID.randomUUID());
    }

    public static WaveId of(UUID value) {
        return new WaveId(value);
    }

    public static WaveId of(String value) {
        return new WaveId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaveId waveId = (WaveId) o;
        return Objects.equals(value, waveId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "WaveId{" + "value=" + value + '}';
    }
}