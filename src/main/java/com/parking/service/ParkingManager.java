package com.parking.service;

import com.parking.allocator.AllocationStrategy;
import com.parking.analytics.AnalyticsService;
import com.parking.analytics.AnalyticsSnapshot;
import com.parking.model.ParkingAssignment;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSlot;
import com.parking.model.ParkingTicket;
import com.parking.model.RejectionReason;
import com.parking.model.Vehicle;
import com.parking.model.VehicleSlotCompatibility;
import com.parking.pricing.PricingBreakdown;
import com.parking.pricing.PricingService;
import com.parking.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class ParkingManager {
    private final ParkingLot parkingLot;
    private final AllocationStrategy allocationStrategy;
    private final TicketRepository ticketRepository;
    private final PricingService pricingService;
    private final AnalyticsService analyticsService;
    private final AtomicLong ticketSequence = new AtomicLong(1);

    public ParkingManager(
            ParkingLot parkingLot,
            AllocationStrategy allocationStrategy,
            TicketRepository ticketRepository,
            PricingService pricingService,
            AnalyticsService analyticsService
    ) {
        this.parkingLot = parkingLot;
        this.allocationStrategy = allocationStrategy;
        this.ticketRepository = ticketRepository;
        this.pricingService = pricingService;
        this.analyticsService = analyticsService;
        this.allocationStrategy.initialize(parkingLot);
    }

    public ParkVehicleResult parkVehicle(Vehicle vehicle) {
        long start = System.nanoTime();
        if (ticketRepository.existsActiveTicketFor(vehicle.getLicensePlate())) {
            analyticsService.recordRejection(vehicle, RejectionReason.DUPLICATE_ACTIVE_ENTRY);
            return new ParkVehicleResult(
                    false,
                    RejectionReason.DUPLICATE_ACTIVE_ENTRY.getDescription(),
                    null,
                    RejectionReason.DUPLICATE_ACTIVE_ENTRY,
                    System.nanoTime() - start
            );
        }

        if (!VehicleSlotCompatibility.lotCanSupport(parkingLot, vehicle.getVehicleType())) {
            analyticsService.recordRejection(vehicle, RejectionReason.NO_COMPATIBLE_SLOT_CONFIGURED);
            return new ParkVehicleResult(
                    false,
                    RejectionReason.NO_COMPATIBLE_SLOT_CONFIGURED.getDescription(),
                    null,
                    RejectionReason.NO_COMPATIBLE_SLOT_CONFIGURED,
                    System.nanoTime() - start
            );
        }

        Optional<ParkingAssignment> assignment = allocationStrategy.allocate(vehicle);
        long allocationNanos = System.nanoTime() - start;
        if (assignment.isEmpty()) {
            analyticsService.recordRejection(vehicle, RejectionReason.CAPACITY_EXHAUSTED);
            return new ParkVehicleResult(
                    false,
                    RejectionReason.CAPACITY_EXHAUSTED.getDescription(),
                    null,
                    RejectionReason.CAPACITY_EXHAUSTED,
                    allocationNanos
            );
        }

        ParkingSlot slot = assignment.get().slot();
        slot.occupy();
        ParkingTicket ticket = new ParkingTicket(
                nextTicketId(),
                vehicle,
                slot.getSlotId(),
                slot.getLevelId(),
                vehicle.getEntryTime(),
                assignment.get().chargingAllocated()
        );
        ticketRepository.saveActive(ticket);
        analyticsService.recordParking(ticket, slot);
        return new ParkVehicleResult(
                true,
                "Vehicle parked successfully in slot " + slot.getSlotId(),
                ticket,
                null,
                allocationNanos
        );
    }

    public ExitVehicleResult exitVehicle(String licensePlate) {
        return exitVehicle(licensePlate, LocalDateTime.now());
    }

    public ExitVehicleResult exitVehicle(String licensePlate, LocalDateTime exitTime) {
        Optional<ParkingTicket> ticketOptional = ticketRepository.findActiveByLicensePlate(licensePlate);
        if (ticketOptional.isEmpty()) {
            return new ExitVehicleResult(
                    false,
                    RejectionReason.ACTIVE_TICKET_NOT_FOUND.getDescription(),
                    null,
                    null,
                    RejectionReason.ACTIVE_TICKET_NOT_FOUND
            );
        }

        ParkingTicket ticket = ticketOptional.get();
        ParkingSlot slot = parkingLot.getSlot(ticket.getSlotId());
        PricingBreakdown pricingBreakdown = pricingService.calculate(ticket, slot, exitTime);
        ticket.close(exitTime, pricingBreakdown);
        slot.release();
        allocationStrategy.release(slot);
        ticketRepository.archive(ticket);
        analyticsService.recordExit(ticket, slot);

        return new ExitVehicleResult(
                true,
                "Vehicle exited successfully from slot " + slot.getSlotId(),
                ticket,
                pricingBreakdown,
                null
        );
    }

    public AnalyticsSnapshot getAnalyticsSnapshot() {
        return analyticsService.createSnapshot();
    }

    public List<ParkingTicket> getActiveTickets() {
        return ticketRepository.findAllActive();
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public String getAllocationStrategyName() {
        return allocationStrategy.getName();
    }

    private String nextTicketId() {
        return "T%06d".formatted(ticketSequence.getAndIncrement());
    }
}
