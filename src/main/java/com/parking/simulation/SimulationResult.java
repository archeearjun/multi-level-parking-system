package com.parking.simulation;

import java.math.BigDecimal;

public record SimulationResult(
        String strategyName,
        long totalRequests,
        long acceptedRequests,
        long rejectedRequests,
        long completedSessions,
        double rejectionRatePercentage,
        double averageAllocationMicros,
        double throughputRequestsPerSecond,
        double averageOccupancyPercentage,
        double evChargingFulfillmentRate,
        BigDecimal totalRevenue,
        long wallClockNanos
) {
}
