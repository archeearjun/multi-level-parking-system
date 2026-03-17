package com.parking.api.controller;

import com.parking.api.dto.AvailabilityResponse;
import com.parking.api.dto.ExitVehicleRequest;
import com.parking.api.dto.ParkVehicleRequest;
import com.parking.api.dto.ParkingOperationResponse;
import com.parking.api.dto.TicketResponse;
import com.parking.api.mapper.ParkingApiMapper;
import com.parking.model.RejectionReason;
import com.parking.model.Vehicle;
import com.parking.service.ExitVehicleResult;
import com.parking.service.LiveParkingState;
import com.parking.service.ParkVehicleResult;
import com.parking.service.ParkingPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/parking")
@Tag(name = "Parking", description = "Live parking entry, exit, availability, and ticket endpoints")
public class ParkingController {
    private final ParkingPlatformService parkingPlatformService;
    private final ParkingApiMapper parkingApiMapper;

    public ParkingController(ParkingPlatformService parkingPlatformService, ParkingApiMapper parkingApiMapper) {
        this.parkingPlatformService = parkingPlatformService;
        this.parkingApiMapper = parkingApiMapper;
    }

    @PostMapping("/entries")
    @Operation(summary = "Park a vehicle in the live parking lot")
    public ResponseEntity<ParkingOperationResponse> parkVehicle(@Valid @RequestBody ParkVehicleRequest request) {
        ParkVehicleResult result = parkingPlatformService.parkVehicle(new Vehicle(
                request.licensePlate(),
                request.vehicleType(),
                request.entryTime() != null ? request.entryTime() : LocalDateTime.now(),
                request.chargingRequested()
        ));
        LiveParkingState state = parkingPlatformService.getLiveState();
        return ResponseEntity.status(resolveParkStatus(result))
                .body(parkingApiMapper.toParkResponse(result, state.strategyName()));
    }

    @PostMapping("/exits")
    @Operation(summary = "Exit a vehicle from the live parking lot")
    public ResponseEntity<ParkingOperationResponse> exitVehicle(@Valid @RequestBody ExitVehicleRequest request) {
        ExitVehicleResult result = parkingPlatformService.exitVehicle(
                request.licensePlate(),
                request.exitTime() != null ? request.exitTime() : LocalDateTime.now()
        );
        LiveParkingState state = parkingPlatformService.getLiveState();
        return ResponseEntity.status(resolveExitStatus(result))
                .body(parkingApiMapper.toExitResponse(result, state.strategyName()));
    }

    @GetMapping("/active-tickets")
    @Operation(summary = "List active parking tickets in the live lot")
    public List<TicketResponse> getActiveTickets() {
        return parkingPlatformService.getLiveState().activeTickets()
                .stream()
                .map(parkingApiMapper::toTicketResponse)
                .toList();
    }

    @GetMapping("/availability")
    @Operation(summary = "Get current availability by level and slot type")
    public AvailabilityResponse getAvailability() {
        return parkingApiMapper.toAvailabilityResponse(parkingPlatformService.getLiveState());
    }

    private HttpStatus resolveParkStatus(ParkVehicleResult result) {
        if (result.success()) {
            return HttpStatus.CREATED;
        }
        if (result.rejectionReason() == RejectionReason.DUPLICATE_ACTIVE_ENTRY) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.CONFLICT;
    }

    private HttpStatus resolveExitStatus(ExitVehicleResult result) {
        return result.success() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
    }
}
