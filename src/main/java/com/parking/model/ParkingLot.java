package com.parking.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ParkingLot {
    private final String lotId;
    private final String name;
    private final List<ParkingLevel> levels;
    private final Map<String, ParkingSlot> slotById;
    private final EnumSet<SlotType> configuredSlotTypes;
    private final int evChargingCapacity;

    public ParkingLot(String lotId, String name, List<ParkingLevel> levels) {
        this.lotId = Objects.requireNonNull(lotId, "lotId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.levels = List.copyOf(Objects.requireNonNull(levels, "levels must not be null"));
        this.slotById = new LinkedHashMap<>();
        this.configuredSlotTypes = EnumSet.noneOf(SlotType.class);
        int evSlots = 0;
        for (ParkingLevel level : levels) {
            for (ParkingSlot slot : level.getSlots()) {
                slotById.put(slot.getSlotId(), slot);
                configuredSlotTypes.add(slot.getSlotType());
                if (slot.isEvChargingAvailable()) {
                    evSlots++;
                }
            }
        }
        this.evChargingCapacity = evSlots;
    }

    public String getLotId() {
        return lotId;
    }

    public String getName() {
        return name;
    }

    public List<ParkingLevel> getLevels() {
        return levels;
    }

    public Collection<ParkingSlot> getAllSlots() {
        return slotById.values();
    }

    public ParkingSlot getSlot(String slotId) {
        ParkingSlot slot = slotById.get(slotId);
        if (slot == null) {
            throw new IllegalArgumentException("Unknown slot id: " + slotId);
        }
        return slot;
    }

    public int getTotalCapacity() {
        return slotById.size();
    }

    public int getEvChargingCapacity() {
        return evChargingCapacity;
    }

    public boolean hasConfiguredSlotType(SlotType slotType) {
        return configuredSlotTypes.contains(slotType);
    }

    public List<ParkingSlot> getAvailableSlots() {
        List<ParkingSlot> availableSlots = new ArrayList<>();
        for (ParkingSlot slot : slotById.values()) {
            if (!slot.isOccupied()) {
                availableSlots.add(slot);
            }
        }
        return availableSlots;
    }
}
