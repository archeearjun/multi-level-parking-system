package com.parking.model;

public enum SlotType {
    BIKE("Bike Slot", 1),
    COMPACT("Compact Slot", 2),
    LARGE("Large Slot", 3),
    TRUCK("Truck Slot", 4);

    private final String displayName;
    private final int sizeRank;

    SlotType(String displayName, int sizeRank) {
        this.displayName = displayName;
        this.sizeRank = sizeRank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSizeRank() {
        return sizeRank;
    }
}
