package com.parking.service;

import com.parking.model.ParkingTicket;
import com.parking.model.RejectionReason;
import com.parking.pricing.PricingBreakdown;

public record ExitVehicleResult(
        boolean success,
        String message,
        ParkingTicket ticket,
        PricingBreakdown pricingBreakdown,
        RejectionReason rejectionReason
) {
}
