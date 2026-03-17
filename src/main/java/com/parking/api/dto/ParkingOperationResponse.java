package com.parking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for park and exit operations")
public record ParkingOperationResponse(
        boolean success,
        String message,
        String allocationStrategy,
        String rejectionReason,
        Long allocationNanos,
        TicketResponse ticket,
        PricingBreakdownResponse pricingBreakdown
) {
}
