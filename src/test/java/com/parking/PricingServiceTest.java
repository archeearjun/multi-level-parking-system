package com.parking;

import com.parking.bootstrap.DemoDataFactory;
import com.parking.model.ParkingSlot;
import com.parking.model.ParkingTicket;
import com.parking.model.SlotType;
import com.parking.model.Vehicle;
import com.parking.model.VehicleType;
import com.parking.pricing.PricingBreakdown;
import com.parking.pricing.PricingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {
    @Test
    void calculatesLevelPremiumAndChargingSurcharge() {
        PricingService pricingService = DemoDataFactory.createPricingService();
        LocalDateTime entryTime = LocalDateTime.of(2026, 3, 17, 10, 0);
        ParkingSlot slot = new ParkingSlot(
                "L1-CE01",
                "L1",
                1,
                new BigDecimal("1.20"),
                SlotType.COMPACT,
                true,
                1
        );
        ParkingTicket ticket = new ParkingTicket(
                "T000001",
                new Vehicle("EV-500", VehicleType.EV_CAR, entryTime, true),
                slot.getSlotId(),
                slot.getLevelId(),
                entryTime,
                true
        );

        PricingBreakdown breakdown = pricingService.calculate(ticket, slot, entryTime.plusMinutes(90));

        assertEquals(90, breakdown.parkedMinutes());
        assertEquals(2, breakdown.billedHours());
        assertEquals(new BigDecimal("9.00"), breakdown.baseCharge());
        assertEquals(new BigDecimal("1.80"), breakdown.levelPremiumCharge());
        assertEquals(new BigDecimal("2.50"), breakdown.evChargingSurcharge());
        assertEquals(new BigDecimal("13.30"), breakdown.total());
    }
}
