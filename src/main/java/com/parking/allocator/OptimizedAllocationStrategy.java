package com.parking.allocator;

import com.parking.model.ParkingAssignment;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSlot;
import com.parking.model.SlotType;
import com.parking.model.Vehicle;
import com.parking.model.VehicleSlotCompatibility;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;

public final class OptimizedAllocationStrategy implements AllocationStrategy {
    // Separate ordered pools preserve O(log n) insert/remove while also protecting chargers for EV demand.
    private static final Comparator<ParkingSlot> SLOT_ORDER =
            Comparator.comparingInt(ParkingSlot::getLevelNumber)
                    .thenComparingInt(ParkingSlot::getConvenienceRank)
                    .thenComparing(ParkingSlot::getSlotId);

    private final Map<SlotType, NavigableSet<ParkingSlot>> nonEvAvailableByType = new EnumMap<>(SlotType.class);
    private final Map<SlotType, NavigableSet<ParkingSlot>> evAvailableByType = new EnumMap<>(SlotType.class);

    @Override
    public String getName() {
        return "Optimized Best-Fit";
    }

    @Override
    public void initialize(ParkingLot parkingLot) {
        nonEvAvailableByType.clear();
        evAvailableByType.clear();
        for (SlotType slotType : SlotType.values()) {
            nonEvAvailableByType.put(slotType, new TreeSet<>(SLOT_ORDER));
            evAvailableByType.put(slotType, new TreeSet<>(SLOT_ORDER));
        }
        for (ParkingSlot slot : parkingLot.getAllSlots()) {
            if (!slot.isOccupied()) {
                addToIndex(slot);
            }
        }
    }

    @Override
    public Optional<ParkingAssignment> allocate(Vehicle vehicle) {
        if (vehicle.isChargingRequested()) {
            Optional<ParkingAssignment> chargingSlot = allocateChargingSlot(vehicle);
            if (chargingSlot.isPresent()) {
                return chargingSlot;
            }
        }
        return allocateCompatibleSlot(vehicle);
    }

    @Override
    public void release(ParkingSlot slot) {
        addToIndex(slot);
    }

    private Optional<ParkingAssignment> allocateChargingSlot(Vehicle vehicle) {
        for (SlotType slotType : VehicleSlotCompatibility.getPreferredSlotTypes(vehicle.getVehicleType())) {
            NavigableSet<ParkingSlot> pool = evAvailableByType.get(slotType);
            if (!pool.isEmpty()) {
                ParkingSlot slot = pool.pollFirst();
                return Optional.of(new ParkingAssignment(slot, true));
            }
        }
        return Optional.empty();
    }

    private Optional<ParkingAssignment> allocateCompatibleSlot(Vehicle vehicle) {
        for (SlotType slotType : VehicleSlotCompatibility.getPreferredSlotTypes(vehicle.getVehicleType())) {
            NavigableSet<ParkingSlot> pool = nonEvAvailableByType.get(slotType);
            if (!pool.isEmpty()) {
                ParkingSlot slot = pool.pollFirst();
                return Optional.of(new ParkingAssignment(slot, false));
            }
        }
        for (SlotType slotType : VehicleSlotCompatibility.getPreferredSlotTypes(vehicle.getVehicleType())) {
            NavigableSet<ParkingSlot> pool = evAvailableByType.get(slotType);
            if (!pool.isEmpty()) {
                ParkingSlot slot = pool.pollFirst();
                return Optional.of(new ParkingAssignment(slot, false));
            }
        }
        return Optional.empty();
    }

    private void addToIndex(ParkingSlot slot) {
        Map<SlotType, NavigableSet<ParkingSlot>> index = slot.isEvChargingAvailable() ? evAvailableByType : nonEvAvailableByType;
        index.get(slot.getSlotType()).add(slot);
    }
}
