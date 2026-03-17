package com.parking.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class ParkingSlot {
    private final String slotId;
    private final String levelId;
    private final int levelNumber;
    private final BigDecimal levelPricingMultiplier;
    private final SlotType slotType;
    private final boolean evChargingAvailable;
    private final int convenienceRank;
    private boolean occupied;

    public ParkingSlot(
            String slotId,
            String levelId,
            int levelNumber,
            BigDecimal levelPricingMultiplier,
            SlotType slotType,
            boolean evChargingAvailable,
            int convenienceRank
    ) {
        this.slotId = Objects.requireNonNull(slotId, "slotId must not be null");
        this.levelId = Objects.requireNonNull(levelId, "levelId must not be null");
        this.levelNumber = levelNumber;
        this.levelPricingMultiplier = Objects.requireNonNull(levelPricingMultiplier, "levelPricingMultiplier must not be null");
        this.slotType = Objects.requireNonNull(slotType, "slotType must not be null");
        this.evChargingAvailable = evChargingAvailable;
        this.convenienceRank = convenienceRank;
    }

    public String getSlotId() {
        return slotId;
    }

    public String getLevelId() {
        return levelId;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public BigDecimal getLevelPricingMultiplier() {
        return levelPricingMultiplier;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public boolean isEvChargingAvailable() {
        return evChargingAvailable;
    }

    public int getConvenienceRank() {
        return convenienceRank;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void occupy() {
        if (occupied) {
            throw new IllegalStateException("Slot is already occupied: " + slotId);
        }
        occupied = true;
    }

    public void release() {
        if (!occupied) {
            throw new IllegalStateException("Slot is already free: " + slotId);
        }
        occupied = false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ParkingSlot slot)) {
            return false;
        }
        return slotId.equals(slot.slotId);
    }

    @Override
    public int hashCode() {
        return slotId.hashCode();
    }

    @Override
    public String toString() {
        return slotId + "[" + slotType + ", level=" + levelId + ", ev=" + evChargingAvailable + "]";
    }
}
