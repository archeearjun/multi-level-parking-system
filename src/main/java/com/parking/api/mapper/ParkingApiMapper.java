package com.parking.api.mapper;

import com.parking.api.dto.AvailabilityResponse;
import com.parking.api.dto.BenchmarkResponse;
import com.parking.api.dto.DashboardResponse;
import com.parking.api.dto.LevelAvailabilityResponse;
import com.parking.api.dto.ParkingOperationResponse;
import com.parking.api.dto.PricingBreakdownResponse;
import com.parking.api.dto.ResetSystemResponse;
import com.parking.api.dto.RevenueSummaryResponse;
import com.parking.api.dto.SimulationResultResponse;
import com.parking.api.dto.TicketResponse;
import com.parking.model.ParkingLevel;
import com.parking.model.ParkingSlot;
import com.parking.model.ParkingTicket;
import com.parking.model.SlotType;
import com.parking.pricing.PricingBreakdown;
import com.parking.service.ExitVehicleResult;
import com.parking.service.LiveParkingState;
import com.parking.service.ParkVehicleResult;
import com.parking.simulation.BenchmarkResult;
import com.parking.simulation.SimulationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ParkingApiMapper {
    public ParkingOperationResponse toParkResponse(ParkVehicleResult result, String strategyName) {
        return new ParkingOperationResponse(
                result.success(),
                result.message(),
                strategyName,
                result.rejectionReason() != null ? result.rejectionReason().name() : null,
                result.allocationNanos(),
                result.ticket() != null ? toTicketResponse(result.ticket()) : null,
                null
        );
    }

    public ParkingOperationResponse toExitResponse(ExitVehicleResult result, String strategyName) {
        return new ParkingOperationResponse(
                result.success(),
                result.message(),
                strategyName,
                result.rejectionReason() != null ? result.rejectionReason().name() : null,
                null,
                result.ticket() != null ? toTicketResponse(result.ticket()) : null,
                result.pricingBreakdown() != null ? toPricingBreakdownResponse(result.pricingBreakdown()) : null
        );
    }

    public TicketResponse toTicketResponse(ParkingTicket ticket) {
        return new TicketResponse(
                ticket.getTicketId(),
                ticket.getVehicle().getLicensePlate(),
                ticket.getVehicle().getVehicleType(),
                ticket.getSlotId(),
                ticket.getLevelId(),
                ticket.getEntryTime(),
                ticket.getExitTime(),
                ticket.getVehicle().isChargingRequested(),
                ticket.isChargingAllocated(),
                ticket.getParkedMinutes(),
                ticket.getTotalFee()
        );
    }

    public PricingBreakdownResponse toPricingBreakdownResponse(PricingBreakdown pricingBreakdown) {
        return new PricingBreakdownResponse(
                pricingBreakdown.parkedMinutes(),
                pricingBreakdown.billedHours(),
                pricingBreakdown.baseRatePerHour(),
                pricingBreakdown.baseCharge(),
                pricingBreakdown.levelPremiumCharge(),
                pricingBreakdown.evChargingSurcharge(),
                pricingBreakdown.total()
        );
    }

    public AvailabilityResponse toAvailabilityResponse(LiveParkingState state) {
        Map<SlotType, Integer> availableBySlotType = emptySlotTypeMap();
        List<LevelAvailabilityResponse> levels = new ArrayList<>();
        int availableEvChargingSlots = 0;

        for (ParkingLevel level : state.parkingLot().getLevels()) {
            Map<SlotType, Integer> levelAvailability = emptySlotTypeMap();
            int occupied = 0;
            int freeEv = 0;
            for (ParkingSlot slot : level.getSlots()) {
                if (slot.isOccupied()) {
                    occupied++;
                    continue;
                }
                levelAvailability.merge(slot.getSlotType(), 1, Integer::sum);
                availableBySlotType.merge(slot.getSlotType(), 1, Integer::sum);
                if (slot.isEvChargingAvailable()) {
                    freeEv++;
                    availableEvChargingSlots++;
                }
            }
            levels.add(new LevelAvailabilityResponse(
                    level.getLevelId(),
                    level.getLevelNumber(),
                    level.getCapacity(),
                    occupied,
                    level.getCapacity() - occupied,
                    freeEv,
                    Map.copyOf(levelAvailability)
            ));
        }

        return new AvailabilityResponse(
                state.parkingLot().getLotId(),
                state.parkingLot().getName(),
                state.strategyName(),
                state.analyticsSnapshot().totalCapacity(),
                state.analyticsSnapshot().currentOccupiedSlots(),
                state.analyticsSnapshot().currentAvailableSlots(),
                state.analyticsSnapshot().occupancyPercentage(),
                state.analyticsSnapshot().evChargingCapacity(),
                availableEvChargingSlots,
                Map.copyOf(availableBySlotType),
                List.copyOf(levels)
        );
    }

    public RevenueSummaryResponse toRevenueSummary(LiveParkingState state) {
        return new RevenueSummaryResponse(
                state.analyticsSnapshot().totalRevenue(),
                state.analyticsSnapshot().revenueByVehicleType(),
                state.analyticsSnapshot().completedSessions(),
                state.analyticsSnapshot().averageParkingDurationMinutes()
        );
    }

    public DashboardResponse toDashboardResponse(LiveParkingState state) {
        List<TicketResponse> activeTickets = state.activeTickets().stream()
                .map(this::toTicketResponse)
                .toList();
        return new DashboardResponse(
                state.strategyName(),
                toAvailabilityResponse(state),
                state.analyticsSnapshot(),
                activeTickets.size(),
                activeTickets
        );
    }

    public ResetSystemResponse toResetResponse(LiveParkingState state) {
        return new ResetSystemResponse(
                "Live parking system reset to the default optimized layout",
                toDashboardResponse(state)
        );
    }

    public SimulationResultResponse toSimulationResultResponse(SimulationResult result) {
        return new SimulationResultResponse(
                result.strategyName(),
                result.totalRequests(),
                result.acceptedRequests(),
                result.rejectedRequests(),
                result.completedSessions(),
                result.rejectionRatePercentage(),
                result.averageAllocationMicros(),
                result.throughputRequestsPerSecond(),
                result.averageOccupancyPercentage(),
                result.evChargingFulfillmentRate(),
                result.totalRevenue(),
                result.wallClockNanos()
        );
    }

    public BenchmarkResponse toBenchmarkResponse(BenchmarkResult result) {
        return new BenchmarkResponse(
                toSimulationResultResponse(result.naiveResult()),
                toSimulationResultResponse(result.optimizedResult()),
                result.allocationLatencyImprovementPct(),
                result.throughputImprovementPct(),
                result.occupancyDeltaPctPoints(),
                result.rejectionRateDeltaPctPoints()
        );
    }

    private Map<SlotType, Integer> emptySlotTypeMap() {
        Map<SlotType, Integer> slotMap = new EnumMap<>(SlotType.class);
        for (SlotType slotType : SlotType.values()) {
            slotMap.put(slotType, 0);
        }
        return slotMap;
    }
}
