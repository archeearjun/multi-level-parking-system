package com.parking.allocator;

import com.parking.model.ParkingAssignment;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSlot;
import com.parking.model.Vehicle;

import java.util.Optional;

public interface AllocationStrategy {
    String getName();

    void initialize(ParkingLot parkingLot);

    Optional<ParkingAssignment> allocate(Vehicle vehicle);

    void release(ParkingSlot slot);
}
