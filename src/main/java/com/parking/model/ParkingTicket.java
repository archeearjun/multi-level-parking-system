package com.parking.model;

import com.parking.pricing.PricingBreakdown;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class ParkingTicket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final String slotId;
    private final String levelId;
    private final LocalDateTime entryTime;
    private final boolean chargingAllocated;
    private LocalDateTime exitTime;
    private long parkedMinutes;
    private BigDecimal totalFee;
    private PricingBreakdown pricingBreakdown;

    public ParkingTicket(String ticketId, Vehicle vehicle, String slotId, String levelId, LocalDateTime entryTime, boolean chargingAllocated) {
        this.ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle must not be null");
        this.slotId = Objects.requireNonNull(slotId, "slotId must not be null");
        this.levelId = Objects.requireNonNull(levelId, "levelId must not be null");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime must not be null");
        this.chargingAllocated = chargingAllocated;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getSlotId() {
        return slotId;
    }

    public String getLevelId() {
        return levelId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public boolean isChargingAllocated() {
        return chargingAllocated;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public long getParkedMinutes() {
        return parkedMinutes;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public PricingBreakdown getPricingBreakdown() {
        return pricingBreakdown;
    }

    public boolean isActive() {
        return exitTime == null;
    }

    public void close(LocalDateTime exitTime, PricingBreakdown pricingBreakdown) {
        if (!isActive()) {
            throw new IllegalStateException("Ticket is already closed: " + ticketId);
        }
        this.exitTime = Objects.requireNonNull(exitTime, "exitTime must not be null");
        this.pricingBreakdown = Objects.requireNonNull(pricingBreakdown, "pricingBreakdown must not be null");
        this.parkedMinutes = pricingBreakdown.parkedMinutes();
        this.totalFee = pricingBreakdown.total();
    }
}
