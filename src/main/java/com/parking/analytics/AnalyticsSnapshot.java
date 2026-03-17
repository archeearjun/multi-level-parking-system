package com.parking.analytics;

import com.parking.model.RejectionReason;
import com.parking.model.SlotType;
import com.parking.model.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AnalyticsSnapshot(
        int totalCapacity,
        int currentOccupiedSlots,
        int currentAvailableSlots,
        double occupancyPercentage,
        Map<String, Integer> occupancyByLevel,
        Map<String, Double> occupancyPercentageByLevel,
        Map<SlotType, Integer> occupiedSlotsByType,
        Map<SlotType, Integer> totalSlotsByType,
        int evChargingCapacity,
        long evChargingRequests,
        long evChargingAssignments,
        int currentActiveChargingSessions,
        double evChargingFulfillmentRate,
        double evChargerUtilizationRate,
        long totalVehiclesParked,
        long completedSessions,
        long rejectedVehicles,
        Map<RejectionReason, Long> rejectedByReason,
        BigDecimal totalRevenue,
        Map<VehicleType, BigDecimal> revenueByVehicleType,
        double averageParkingDurationMinutes,
        int peakOccupiedSlots,
        LocalDateTime peakObservedAt,
        List<Integer> peakUsageHours
) {
}
