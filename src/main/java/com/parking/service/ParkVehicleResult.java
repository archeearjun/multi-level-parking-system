package com.parking.service;

import com.parking.model.ParkingTicket;
import com.parking.model.RejectionReason;

public record ParkVehicleResult(
        boolean success,
        String message,
        ParkingTicket ticket,
        RejectionReason rejectionReason,
        long allocationNanos
) {
}
