package com.parking.api.dto;

import com.parking.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Request payload for parking a vehicle")
public record ParkVehicleRequest(
        @Schema(example = "KA01AB1234")
        @NotBlank String licensePlate,
        @NotNull VehicleType vehicleType,
        @Schema(description = "Whether the EV requires charger allocation during this session", example = "true")
        boolean chargingRequested,
        @Schema(description = "Optional entry timestamp. Defaults to server time when omitted.", example = "2026-03-17T09:30:00")
        LocalDateTime entryTime
) {
}
