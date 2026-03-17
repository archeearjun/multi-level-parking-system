package com.parking.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class VehicleSlotCompatibility {
    private static final Map<VehicleType, List<SlotType>> SLOT_PREFERENCES = new EnumMap<>(VehicleType.class);

    static {
        SLOT_PREFERENCES.put(VehicleType.MOTORCYCLE, List.of(SlotType.BIKE, SlotType.COMPACT, SlotType.LARGE));
        SLOT_PREFERENCES.put(VehicleType.CAR, List.of(SlotType.COMPACT, SlotType.LARGE));
        SLOT_PREFERENCES.put(VehicleType.SUV, List.of(SlotType.LARGE, SlotType.TRUCK));
        SLOT_PREFERENCES.put(VehicleType.TRUCK, List.of(SlotType.TRUCK));
        SLOT_PREFERENCES.put(VehicleType.EV_CAR, List.of(SlotType.COMPACT, SlotType.LARGE));
        SLOT_PREFERENCES.put(VehicleType.EV_SUV, List.of(SlotType.LARGE, SlotType.TRUCK));
    }

    private VehicleSlotCompatibility() {
    }

    public static List<SlotType> getPreferredSlotTypes(VehicleType vehicleType) {
        return SLOT_PREFERENCES.get(vehicleType);
    }

    public static boolean isCompatible(VehicleType vehicleType, SlotType slotType) {
        return SLOT_PREFERENCES.get(vehicleType).contains(slotType);
    }

    public static boolean lotCanSupport(ParkingLot parkingLot, VehicleType vehicleType) {
        for (SlotType slotType : SLOT_PREFERENCES.get(vehicleType)) {
            if (parkingLot.hasConfiguredSlotType(slotType)) {
                return true;
            }
        }
        return false;
    }
}
