package com.parking.model;

public enum RejectionReason {
    DUPLICATE_ACTIVE_ENTRY("Vehicle already has an active ticket"),
    NO_COMPATIBLE_SLOT_CONFIGURED("Parking lot has no compatible slot category for this vehicle"),
    CAPACITY_EXHAUSTED("All compatible slots are occupied"),
    ACTIVE_TICKET_NOT_FOUND("No active ticket found for the provided vehicle");

    private final String description;

    RejectionReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
