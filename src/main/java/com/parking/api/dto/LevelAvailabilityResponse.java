package com.parking.api.dto;

import com.parking.model.SlotType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Availability summary for a single parking level")
public record LevelAvailabilityResponse(
        String levelId,
        int levelNumber,
        int capacity,
        int occupied,
        int available,
        int availableEvChargingSlots,
        Map<SlotType, Integer> availableBySlotType
) {
}
