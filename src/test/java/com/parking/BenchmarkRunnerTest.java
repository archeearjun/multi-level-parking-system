package com.parking;

import com.parking.allocator.AllocationStrategy;
import com.parking.allocator.OptimizedAllocationStrategy;
import com.parking.analytics.AnalyticsService;
import com.parking.model.ParkingLot;
import com.parking.model.SlotType;
import com.parking.repository.InMemoryTicketRepository;
import com.parking.service.ParkingManager;
import com.parking.simulation.BenchmarkResult;
import com.parking.simulation.BenchmarkRunner;
import com.parking.simulation.SimulationConfig;
import com.parking.simulation.SimulationEngine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BenchmarkRunnerTest {
    @Test
    void benchmarkRunnerProducesComparableResultsForSameDemandStream() {
        Function<AllocationStrategy, ParkingManager> managerFactory = strategy -> {
            ParkingLot lot = TestParkingFactory.createLot(
                    "LOT-BENCH",
                    TestParkingFactory.createLevel(
                            "L1",
                            1,
                            new BigDecimal("1.00"),
                            TestParkingFactory.slot("L1-CE01", "L1", 1, new BigDecimal("1.00"), SlotType.COMPACT, true, 1),
                            TestParkingFactory.slot("L1-L01", "L1", 1, new BigDecimal("1.00"), SlotType.LARGE, false, 2),
                            TestParkingFactory.slot("L1-C01", "L1", 1, new BigDecimal("1.00"), SlotType.COMPACT, false, 3),
                            TestParkingFactory.slot("L1-B01", "L1", 1, new BigDecimal("1.00"), SlotType.BIKE, false, 4)
                    )
            );
            return new ParkingManager(
                    lot,
                    strategy,
                    new InMemoryTicketRepository(),
                    com.parking.bootstrap.DemoDataFactory.createPricingService(),
                    new AnalyticsService(lot)
            );
        };
        BenchmarkRunner runner = new BenchmarkRunner(managerFactory, new SimulationEngine());
        SimulationConfig config = new SimulationConfig(
                "benchmark-smoke",
                LocalDateTime.of(2026, 3, 17, 8, 0),
                20,
                5,
                1.0,
                2,
                30,
                120,
                12345L
        );

        BenchmarkResult result = runner.runComparison(config);

        assertEquals(result.naiveResult().totalRequests(), result.optimizedResult().totalRequests());
        assertEquals(result.naiveResult().completedSessions(), result.naiveResult().acceptedRequests());
        assertEquals(result.optimizedResult().completedSessions(), result.optimizedResult().acceptedRequests());
        assertFalse(Double.isNaN(result.allocationLatencyImprovementPct()));
        assertFalse(Double.isNaN(result.throughputImprovementPct()));
        assertFalse(Double.isNaN(result.occupancyDeltaPctPoints()));
        assertFalse(Double.isNaN(result.rejectionRateDeltaPctPoints()));
    }
}
