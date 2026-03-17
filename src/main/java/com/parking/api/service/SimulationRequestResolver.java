package com.parking.api.service;

import com.parking.api.dto.SimulationRequest;
import com.parking.service.ParkingPlatformService;
import com.parking.simulation.SimulationConfig;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class SimulationRequestResolver {
    private final ParkingPlatformService parkingPlatformService;

    public SimulationRequestResolver(ParkingPlatformService parkingPlatformService) {
        this.parkingPlatformService = parkingPlatformService;
    }

    public SimulationConfig resolve(@Nullable SimulationRequest request) {
        SimulationConfig defaults = parkingPlatformService.loadDefaultSimulationConfig();
        if (request == null) {
            return defaults;
        }
        return new SimulationConfig(
                request.scenarioName() != null ? request.scenarioName() : defaults.scenarioName(),
                request.startTime() != null ? request.startTime() : defaults.startTime(),
                request.totalSteps() != null ? request.totalSteps() : defaults.totalSteps(),
                request.stepMinutes() != null ? request.stepMinutes() : defaults.stepMinutes(),
                request.arrivalProbability() != null ? request.arrivalProbability() : defaults.arrivalProbability(),
                request.maxArrivalsPerStep() != null ? request.maxArrivalsPerStep() : defaults.maxArrivalsPerStep(),
                request.minParkingMinutes() != null ? request.minParkingMinutes() : defaults.minParkingMinutes(),
                request.maxParkingMinutes() != null ? request.maxParkingMinutes() : defaults.maxParkingMinutes(),
                request.seed() != null ? request.seed() : defaults.seed()
        );
    }
}
