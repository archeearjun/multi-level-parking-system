package com.parking.api;

import com.parking.service.ParkingPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ParkingApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkingPlatformService parkingPlatformService;

    @BeforeEach
    void setUp() {
        parkingPlatformService.resetLiveSystem();
    }

    @Test
    void parkVehicleAndExposeDashboard() throws Exception {
        mockMvc.perform(post("/api/v1/parking/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "licensePlate": "API-1001",
                                  "vehicleType": "EV_CAR",
                                  "chargingRequested": true,
                                  "entryTime": "2026-03-17T09:15:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.allocationStrategy").value("Optimized Best-Fit"))
                .andExpect(jsonPath("$.ticket.ticketId", notNullValue()))
                .andExpect(jsonPath("$.ticket.slotId", notNullValue()))
                .andExpect(jsonPath("$.ticket.chargingAllocated").value(true));

        mockMvc.perform(get("/api/v1/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocationStrategy").value("Optimized Best-Fit"))
                .andExpect(jsonPath("$.activeTicketCount").value(1))
                .andExpect(jsonPath("$.availability.currentOccupiedSlots").value(1))
                .andExpect(jsonPath("$.analytics.totalVehiclesParked").value(1));
    }

    @Test
    void benchmarkAndDocumentationEndpointsAreAvailable() throws Exception {
        mockMvc.perform(post("/api/v1/operations/benchmarks/allocators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scenarioName": "api-benchmark",
                                  "startTime": "2026-03-17T08:00:00",
                                  "totalSteps": 12,
                                  "stepMinutes": 5,
                                  "arrivalProbability": 1.0,
                                  "maxArrivalsPerStep": 2,
                                  "minParkingMinutes": 30,
                                  "maxParkingMinutes": 90,
                                  "seed": 98765
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naiveResult.totalRequests", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.optimizedResult.totalRequests", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.allocationLatencyImprovementPct", notNullValue()));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Multi-Level Parking System API"))
                .andExpect(jsonPath("$['paths']['/api/v1/parking/entries']").exists());
    }
}
