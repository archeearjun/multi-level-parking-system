package com.parking.model;

public enum VehicleType {
    MOTORCYCLE("Motorcycle", false, SlotType.BIKE),
    CAR("Car", false, SlotType.COMPACT),
    SUV("SUV", false, SlotType.LARGE),
    TRUCK("Truck", false, SlotType.TRUCK),
    EV_CAR("EV Car", true, SlotType.COMPACT),
    EV_SUV("EV SUV", true, SlotType.LARGE);

    private final String displayName;
    private final boolean electric;
    private final SlotType preferredSlotType;

    VehicleType(String displayName, boolean electric, SlotType preferredSlotType) {
        this.displayName = displayName;
        this.electric = electric;
        this.preferredSlotType = preferredSlotType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isElectric() {
        return electric;
    }

    public SlotType getPreferredSlotType() {
        return preferredSlotType;
    }
}
