package com.parking.api.dto;

import com.parking.analytics.AnalyticsSnapshot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Top-level live dashboard payload for the parking facility")
public record DashboardResponse(
        String allocationStrategy,
        AvailabilityResponse availability,
        AnalyticsSnapshot analytics,
        int activeTicketCount,
        List<TicketResponse> activeTickets
) {
}
