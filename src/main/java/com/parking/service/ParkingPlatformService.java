package com.parking.service;

import com.parking.model.ParkingTicket;
import com.parking.model.Vehicle;
import com.parking.simulation.BenchmarkResult;
import com.parking.simulation.BenchmarkRunner;
import com.parking.simulation.SimulationConfig;
import com.parking.simulation.SimulationConfigLoader;
import com.parking.simulation.SimulationEngine;
import com.parking.simulation.SimulationResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParkingPlatformService {
    private static final String DEFAULT_SIMULATION_RESOURCE = "simulation/sample-config.properties";

    private final ParkingManagerFactory parkingManagerFactory;
    private final SimulationEngine simulationEngine;
    private ParkingManager liveParkingManager;

    public ParkingPlatformService(ParkingManagerFactory parkingManagerFactory, SimulationEngine simulationEngine) {
        this.parkingManagerFactory = parkingManagerFactory;
        this.simulationEngine = simulationEngine;
        this.liveParkingManager = parkingManagerFactory.createOptimizedManager();
    }

    public synchronized ParkVehicleResult parkVehicle(Vehicle vehicle) {
        return liveParkingManager.parkVehicle(vehicle);
    }

    public synchronized ExitVehicleResult exitVehicle(String licensePlate, LocalDateTime exitTime) {
        return liveParkingManager.exitVehicle(licensePlate, exitTime);
    }

    public synchronized LiveParkingState getLiveState() {
        List<ParkingTicket> activeTickets = liveParkingManager.getActiveTickets();
        return new LiveParkingState(
                liveParkingManager.getAllocationStrategyName(),
                liveParkingManager.getParkingLot(),
                liveParkingManager.getAnalyticsSnapshot(),
                activeTickets
        );
    }

    public synchronized LiveParkingState resetLiveSystem() {
        this.liveParkingManager = parkingManagerFactory.createOptimizedManager();
        return getLiveState();
    }

    public SimulationResult runSimulation(SimulationConfig config) {
        return simulationEngine.runSimulation(parkingManagerFactory.createOptimizedManager(), config);
    }

    public BenchmarkResult runBenchmark(SimulationConfig config) {
        BenchmarkRunner benchmarkRunner = new BenchmarkRunner(parkingManagerFactory::createManager, simulationEngine);
        return benchmarkRunner.runComparison(config);
    }

    public SimulationConfig loadDefaultSimulationConfig() {
        return SimulationConfigLoader.loadFromResource(DEFAULT_SIMULATION_RESOURCE);
    }
}
