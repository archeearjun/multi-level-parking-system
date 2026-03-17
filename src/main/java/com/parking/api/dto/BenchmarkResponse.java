package com.parking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Benchmark comparison of naive and optimized allocation strategies")
public record BenchmarkResponse(
        SimulationResultResponse naiveResult,
        SimulationResultResponse optimizedResult,
        double allocationLatencyImprovementPct,
        double throughputImprovementPct,
        double occupancyDeltaPctPoints,
        double rejectionRateDeltaPctPoints
) {
}
