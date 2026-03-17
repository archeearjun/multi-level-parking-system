package com.parking.api.dto;

import com.parking.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Parking ticket details")
public record TicketResponse(
        String ticketId,
        String licensePlate,
        VehicleType vehicleType,
        String slotId,
        String levelId,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        boolean chargingRequested,
        boolean chargingAllocated,
        long parkedMinutes,
        BigDecimal totalFee
) {
}
