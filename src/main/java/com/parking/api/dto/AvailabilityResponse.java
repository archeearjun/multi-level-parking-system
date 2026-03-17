package com.parking.api.dto;

import com.parking.model.SlotType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Current parking lot availability snapshot")
public record AvailabilityResponse(
        String lotId,
        String lotName,
        String allocationStrategy,
        int totalCapacity,
        int currentOccupiedSlots,
        int currentAvailableSlots,
        double occupancyPercentage,
        int totalEvChargingSlots,
        int availableEvChargingSlots,
        Map<SlotType, Integer> availableBySlotType,
        List<LevelAvailabilityResponse> levels
) {
}
