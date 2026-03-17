package com.parking.simulation;

import java.time.LocalDateTime;

public record SimulationConfig(
        String scenarioName,
        LocalDateTime startTime,
        int totalSteps,
        int stepMinutes,
        double arrivalProbability,
        int maxArrivalsPerStep,
        int minParkingMinutes,
        int maxParkingMinutes,
        long seed
) {
    public SimulationConfig {
        if (totalSteps <= 0) {
            throw new IllegalArgumentException("totalSteps must be positive");
        }
        if (stepMinutes <= 0) {
            throw new IllegalArgumentException("stepMinutes must be positive");
        }
        if (arrivalProbability < 0 || arrivalProbability > 1) {
            throw new IllegalArgumentException("arrivalProbability must be between 0 and 1");
        }
        if (maxArrivalsPerStep <= 0) {
            throw new IllegalArgumentException("maxArrivalsPerStep must be positive");
        }
        if (minParkingMinutes <= 0 || maxParkingMinutes < minParkingMinutes) {
            throw new IllegalArgumentException("Invalid parking duration range");
        }
    }
}
