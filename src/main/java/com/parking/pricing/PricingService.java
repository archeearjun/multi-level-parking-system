package com.parking.pricing;

import com.parking.model.ParkingSlot;
import com.parking.model.ParkingTicket;

import java.time.LocalDateTime;

public interface PricingService {
    PricingBreakdown calculate(ParkingTicket ticket, ParkingSlot slot, LocalDateTime exitTime);
}
