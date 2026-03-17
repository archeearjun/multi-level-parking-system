package com.parking.pricing;

import com.parking.model.ParkingSlot;
import com.parking.model.ParkingTicket;
import com.parking.model.VehicleType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

public final class StandardPricingService implements PricingService {
    private final Map<VehicleType, BigDecimal> hourlyRates;
    private final BigDecimal evChargingSurcharge;

    public StandardPricingService(Map<VehicleType, BigDecimal> hourlyRates, BigDecimal evChargingSurcharge) {
        this.hourlyRates = new EnumMap<>(hourlyRates);
        this.evChargingSurcharge = evChargingSurcharge.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PricingBreakdown calculate(ParkingTicket ticket, ParkingSlot slot, LocalDateTime exitTime) {
        if (exitTime.isBefore(ticket.getEntryTime())) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }

        long parkedMinutes = Math.max(1, Duration.between(ticket.getEntryTime(), exitTime).toMinutes());
        long billedHours = Math.max(1, (parkedMinutes + 59) / 60);
        BigDecimal baseRate = hourlyRates.get(ticket.getVehicle().getVehicleType());
        BigDecimal baseCharge = baseRate.multiply(BigDecimal.valueOf(billedHours));
        BigDecimal multiplier = slot.getLevelPricingMultiplier();
        BigDecimal adjustedCharge = baseCharge.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal levelPremiumCharge = adjustedCharge.subtract(baseCharge).setScale(2, RoundingMode.HALF_UP);
        BigDecimal chargingCharge = ticket.isChargingAllocated() ? evChargingSurcharge : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = adjustedCharge.add(chargingCharge).setScale(2, RoundingMode.HALF_UP);

        return new PricingBreakdown(
                parkedMinutes,
                billedHours,
                baseRate.setScale(2, RoundingMode.HALF_UP),
                baseCharge.setScale(2, RoundingMode.HALF_UP),
                levelPremiumCharge,
                chargingCharge,
                total
        );
    }
}
