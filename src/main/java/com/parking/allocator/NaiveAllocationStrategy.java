package com.parking.allocator;

import com.parking.model.ParkingAssignment;
import com.parking.model.ParkingLevel;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSlot;
import com.parking.model.Vehicle;
import com.parking.model.VehicleSlotCompatibility;

import java.util.Optional;

public final class NaiveAllocationStrategy implements AllocationStrategy {
    private ParkingLot parkingLot;

    @Override
    public String getName() {
        return "Naive First-Fit";
    }

    @Override
    public void initialize(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    @Override
    public Optional<ParkingAssignment> allocate(Vehicle vehicle) {
        for (ParkingLevel level : parkingLot.getLevels()) {
            for (ParkingSlot slot : level.getSlots()) {
                if (!slot.isOccupied() && VehicleSlotCompatibility.isCompatible(vehicle.getVehicleType(), slot.getSlotType())) {
                    boolean chargingAllocated = vehicle.isChargingRequested() && slot.isEvChargingAvailable();
                    return Optional.of(new ParkingAssignment(slot, chargingAllocated));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void release(ParkingSlot slot) {
        // No secondary index to update for the naive strategy.
    }
}
