package com.parking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Schema(description = "Request payload for removing a vehicle from the parking lot")
public record ExitVehicleRequest(
        @Schema(example = "KA01AB1234")
        @NotBlank String licensePlate,
        @Schema(description = "Optional exit timestamp. Defaults to server time when omitted.", example = "2026-03-17T12:45:00")
        LocalDateTime exitTime
) {
}
