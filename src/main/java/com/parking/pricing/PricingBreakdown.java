package com.parking.pricing;

import java.math.BigDecimal;

public record PricingBreakdown(
        long parkedMinutes,
        long billedHours,
        BigDecimal baseRatePerHour,
        BigDecimal baseCharge,
        BigDecimal levelPremiumCharge,
        BigDecimal evChargingSurcharge,
        BigDecimal total
) {
}
