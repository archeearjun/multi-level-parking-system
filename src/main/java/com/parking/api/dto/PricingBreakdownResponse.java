package com.parking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Detailed pricing information for a completed parking session")
public record PricingBreakdownResponse(
        long parkedMinutes,
        long billedHours,
        BigDecimal baseRatePerHour,
        BigDecimal baseCharge,
        BigDecimal levelPremiumCharge,
        BigDecimal evChargingSurcharge,
        BigDecimal total
) {
}
