package com.parking.api.controller;

import com.parking.analytics.AnalyticsSnapshot;
import com.parking.api.dto.DashboardResponse;
import com.parking.api.dto.RevenueSummaryResponse;
import com.parking.api.mapper.ParkingApiMapper;
import com.parking.service.LiveParkingState;
import com.parking.service.ParkingPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Occupancy, utilization, and revenue analytics")
public class AnalyticsController {
    private final ParkingPlatformService parkingPlatformService;
    private final ParkingApiMapper parkingApiMapper;

    public AnalyticsController(ParkingPlatformService parkingPlatformService, ParkingApiMapper parkingApiMapper) {
        this.parkingPlatformService = parkingPlatformService;
        this.parkingApiMapper = parkingApiMapper;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get the consolidated live dashboard payload")
    public DashboardResponse getDashboard() {
        LiveParkingState state = parkingPlatformService.getLiveState();
        return parkingApiMapper.toDashboardResponse(state);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get the raw analytics snapshot")
    public AnalyticsSnapshot getAnalyticsSummary() {
        return parkingPlatformService.getLiveState().analyticsSnapshot();
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue-centric analytics")
    public RevenueSummaryResponse getRevenueSummary() {
        return parkingApiMapper.toRevenueSummary(parkingPlatformService.getLiveState());
    }
}
