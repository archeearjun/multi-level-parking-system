package com.parking.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ParkingLevel {
    private final String levelId;
    private final int levelNumber;
    private final BigDecimal pricingMultiplier;
    private final Map<String, ParkingSlot> slots;

    public ParkingLevel(String levelId, int levelNumber, BigDecimal pricingMultiplier, Collection<ParkingSlot> slots) {
        this.levelId = Objects.requireNonNull(levelId, "levelId must not be null");
        this.levelNumber = levelNumber;
        this.pricingMultiplier = Objects.requireNonNull(pricingMultiplier, "pricingMultiplier must not be null");
        this.slots = new LinkedHashMap<>();
        for (ParkingSlot slot : Objects.requireNonNull(slots, "slots must not be null")) {
            this.slots.put(slot.getSlotId(), slot);
        }
    }

    public String getLevelId() {
        return levelId;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public BigDecimal getPricingMultiplier() {
        return pricingMultiplier;
    }

    public Collection<ParkingSlot> getSlots() {
        return slots.values();
    }

    public int getCapacity() {
        return slots.size();
    }
}
