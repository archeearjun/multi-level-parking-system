package com.parking.api.dto;

import com.parking.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Revenue and parking-duration analytics")
public record RevenueSummaryResponse(
        BigDecimal totalRevenue,
        Map<VehicleType, BigDecimal> revenueByVehicleType,
        long completedSessions,
        double averageParkingDurationMinutes
) {
}
