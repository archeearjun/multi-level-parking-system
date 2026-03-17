package com.parking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Simulation execution metrics")
public record SimulationResultResponse(
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
