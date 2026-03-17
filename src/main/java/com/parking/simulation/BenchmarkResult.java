package com.parking.simulation;

public record BenchmarkResult(
        SimulationResult naiveResult,
        SimulationResult optimizedResult,
        double allocationLatencyImprovementPct,
        double throughputImprovementPct,
        double occupancyDeltaPctPoints,
        double rejectionRateDeltaPctPoints
) {
}
