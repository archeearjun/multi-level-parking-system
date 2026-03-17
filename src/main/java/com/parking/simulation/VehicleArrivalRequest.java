package com.parking.simulation;

import com.parking.model.VehicleType;

import java.time.LocalDateTime;

public record VehicleArrivalRequest(
        String licensePlate,
        VehicleType vehicleType,
        boolean chargingRequested,
        LocalDateTime arrivalTime,
        int plannedParkingMinutes
) {
}
