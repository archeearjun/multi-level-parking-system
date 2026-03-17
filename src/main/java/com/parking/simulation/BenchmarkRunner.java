package com.parking.simulation;

import com.parking.allocator.AllocationStrategy;
import com.parking.allocator.NaiveAllocationStrategy;
import com.parking.allocator.OptimizedAllocationStrategy;
import com.parking.service.ParkingManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

public final class BenchmarkRunner {
    private final Function<AllocationStrategy, ParkingManager> parkingManagerFactory;
    private final SimulationEngine simulationEngine;

    public BenchmarkRunner(
            Function<AllocationStrategy, ParkingManager> parkingManagerFactory,
            SimulationEngine simulationEngine
    ) {
        this.parkingManagerFactory = parkingManagerFactory;
        this.simulationEngine = simulationEngine;
    }

    public BenchmarkResult runComparison(SimulationConfig config) {
        List<VehicleArrivalRequest> requests = simulationEngine.generateRequests(config);

        SimulationResult naiveResult = simulationEngine.runSimulation(
                parkingManagerFactory.apply(new NaiveAllocationStrategy()),
                requests
        );
        SimulationResult optimizedResult = simulationEngine.runSimulation(
                parkingManagerFactory.apply(new OptimizedAllocationStrategy()),
                requests
        );

        return new BenchmarkResult(
                naiveResult,
                optimizedResult,
                percentageReduction(naiveResult.averageAllocationMicros(), optimizedResult.averageAllocationMicros()),
                percentageGain(naiveResult.throughputRequestsPerSecond(), optimizedResult.throughputRequestsPerSecond()),
                roundToTwoDecimals(optimizedResult.averageOccupancyPercentage() - naiveResult.averageOccupancyPercentage()),
                roundToTwoDecimals(naiveResult.rejectionRatePercentage() - optimizedResult.rejectionRatePercentage())
        );
    }

    private double percentageReduction(double baseline, double improved) {
        if (baseline <= 0) {
            return 0.0;
        }
        return roundToTwoDecimals((baseline - improved) * 100.0 / baseline);
    }

    private double percentageGain(double baseline, double improved) {
        if (baseline <= 0) {
            return 0.0;
        }
        return roundToTwoDecimals((improved - baseline) * 100.0 / baseline);
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
