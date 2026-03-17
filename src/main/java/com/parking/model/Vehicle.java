package com.parking.model;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

public final class Vehicle {
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final LocalDateTime entryTime;
    private final boolean chargingRequested;

    public Vehicle(String licensePlate, VehicleType vehicleType, LocalDateTime entryTime, boolean chargingRequested) {
        this.licensePlate = normalizePlate(licensePlate);
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType must not be null");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime must not be null");
        if (chargingRequested && !vehicleType.isElectric()) {
            throw new IllegalArgumentException("Charging can only be requested for EV vehicles");
        }
        this.chargingRequested = chargingRequested;
    }

    public static String normalizePlate(String licensePlate) {
        String normalized = Objects.requireNonNull(licensePlate, "licensePlate must not be null").trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("licensePlate must not be blank");
        }
        return normalized;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public boolean isChargingRequested() {
        return chargingRequested;
    }
}
