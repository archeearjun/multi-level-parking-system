package com.parking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

@Schema(description = "Optional overrides for the default simulation scenario")
public record SimulationRequest(
        String scenarioName,
        LocalDateTime startTime,
        @Min(1) Integer totalSteps,
        @Min(1) Integer stepMinutes,
        @DecimalMin("0.0") @DecimalMax("1.0") Double arrivalProbability,
        @Min(1) Integer maxArrivalsPerStep,
        @Min(1) Integer minParkingMinutes,
        @Min(1) Integer maxParkingMinutes,
        Long seed
) {
}
