package com.parking.api.controller;

import com.parking.api.dto.BenchmarkResponse;
import com.parking.api.dto.ResetSystemResponse;
import com.parking.api.dto.SimulationRequest;
import com.parking.api.dto.SimulationResultResponse;
import com.parking.api.mapper.ParkingApiMapper;
import com.parking.api.service.SimulationRequestResolver;
import com.parking.simulation.SimulationConfig;
import com.parking.service.LiveParkingState;
import com.parking.service.ParkingPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations")
@Tag(name = "Operations", description = "Simulation, benchmarking, and administrative operations")
public class OperationsController {
    private final ParkingPlatformService parkingPlatformService;
    private final SimulationRequestResolver simulationRequestResolver;
    private final ParkingApiMapper parkingApiMapper;

    public OperationsController(
            ParkingPlatformService parkingPlatformService,
            SimulationRequestResolver simulationRequestResolver,
            ParkingApiMapper parkingApiMapper
    ) {
        this.parkingPlatformService = parkingPlatformService;
        this.simulationRequestResolver = simulationRequestResolver;
        this.parkingApiMapper = parkingApiMapper;
    }

    @PostMapping("/simulations")
    @Operation(summary = "Run a fresh simulation workload against the optimized allocator")
    public SimulationResultResponse runSimulation(@Valid @RequestBody(required = false) SimulationRequest request) {
        SimulationConfig config = simulationRequestResolver.resolve(request);
        return parkingApiMapper.toSimulationResultResponse(parkingPlatformService.runSimulation(config));
    }

    @PostMapping("/benchmarks/allocators")
    @Operation(summary = "Compare naive and optimized allocators on the same demand stream")
    public BenchmarkResponse runBenchmark(@Valid @RequestBody(required = false) SimulationRequest request) {
        SimulationConfig config = simulationRequestResolver.resolve(request);
        return parkingApiMapper.toBenchmarkResponse(parkingPlatformService.runBenchmark(config));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset the live parking lot back to the default seeded state")
    public ResetSystemResponse resetSystem() {
        LiveParkingState state = parkingPlatformService.resetLiveSystem();
        return parkingApiMapper.toResetResponse(state);
    }
}
