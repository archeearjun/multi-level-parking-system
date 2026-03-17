package com.parking;

import com.parking.allocator.OptimizedAllocationStrategy;
import com.parking.analytics.AnalyticsSnapshot;
import com.parking.model.ParkingLevel;
import com.parking.model.ParkingLot;
import com.parking.model.RejectionReason;
import com.parking.model.SlotType;
import com.parking.model.Vehicle;
import com.parking.model.VehicleType;
import com.parking.service.ExitVehicleResult;
import com.parking.service.ParkVehicleResult;
import com.parking.service.ParkingManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkingManagerTest {
    @Test
    void optimizedAllocatorChoosesBestFitCompactSlotForCar() {
        ParkingLot lot = TestParkingFactory.createLot(
                "LOT-A",
                TestParkingFactory.createLevel(
                        "L1",
                        1,
                        new BigDecimal("1.00"),
                        TestParkingFactory.slot("L1-L01", "L1", 1, new BigDecimal("1.00"), SlotType.LARGE, false, 1),
                        TestParkingFactory.slot("L1-C01", "L1", 1, new BigDecimal("1.00"), SlotType.COMPACT, false, 2)
                )
        );
        ParkingManager manager = TestParkingFactory.createManager(lot, new OptimizedAllocationStrategy());

        ParkVehicleResult result = manager.parkVehicle(new Vehicle("CAR-101", VehicleType.CAR, LocalDateTime.now(), false));

        assertTrue(result.success());
        assertEquals("L1-C01", result.ticket().getSlotId());
    }

    @Test
    void truckIsRejectedWhenLotHasNoCompatibleSlotCategory() {
        ParkingLot lot = TestParkingFactory.createLot(
                "LOT-B",
                TestParkingFactory.createLevel(
                        "L1",
                        1,
                        new BigDecimal("1.00"),
                        TestParkingFactory.slot("L1-B01", "L1", 1, new BigDecimal("1.00"), SlotType.BIKE, false, 1),
                        TestParkingFactory.slot("L1-C01", "L1", 1, new BigDecimal("1.00"), SlotType.COMPACT, false, 2)
                )
        );
        ParkingManager manager = TestParkingFactory.createManager(lot, new OptimizedAllocationStrategy());

        ParkVehicleResult result = manager.parkVehicle(new Vehicle("TRK-001", VehicleType.TRUCK, LocalDateTime.now(), false));

        assertFalse(result.success());
        assertEquals(RejectionReason.NO_COMPATIBLE_SLOT_CONFIGURED, result.rejectionReason());
    }

    @Test
    void evVehicleGetsChargingSlotWhenAvailable() {
        ParkingLot lot = TestParkingFactory.createLot(
                "LOT-C",
                TestParkingFactory.createLevel(
                        "L1",
                        1,
                        new BigDecimal("1.00"),
                        TestParkingFactory.slot("L1-CE01", "L1", 1, new BigDecimal("1.00"), SlotType.COMPACT, true, 1),
                        TestParkingFactory.slot("L1-C01", "L1", 1, new BigDecimal("1.00"), SlotType.COMPACT, false, 2)
                )
        );
        ParkingManager manager = TestParkingFactory.createManager(lot, new OptimizedAllocationStrategy());

        ParkVehicleResult result = manager.parkVehicle(new Vehicle("EVC-101", VehicleType.EV_CAR, LocalDateTime.now(), true));

        assertTrue(result.success());
        assertTrue(result.ticket().isChargingAllocated());
        assertEquals("L1-CE01", result.ticket().getSlotId());
    }

    @Test
    void evVehicleFallsBackWhenChargingSlotUnavailable() {
        ParkingLot lot = TestParkingFactory.createLot(
                "LOT-D",
                TestParkingFactory.createLevel(
                        "L1",
                        1,
                        new BigDecimal("1.00"),
                        TestParkingFactory.slot("L1-C01", "L1", 1, new BigDecimal("1.00"), SlotType.COMPACT, false, 1)
                )
        );
        ParkingManager manager = TestParkingFactory.createManager(lot, new OptimizedAllocationStrategy());

        ParkVehicleResult result = manager.parkVehicle(new Vehicle("EVC-102", VehicleType.EV_CAR, LocalDateTime.now(), true));

        assertTrue(result.success());
        assertFalse(result.ticket().isChargingAllocated());
        assertEquals("L1-C01", result.ticket().getSlotId());
    }

    @Test
    void slotIsReleasedAndReusableAfterExit() {
        ParkingLot lot = TestParkingFactory.createLot(
                "LOT-E",
                TestParkingFactory.createLevel(
                        "L1",
                        1,
                        new BigDecimal("1.00"),
                        TestParkingFactory.slot("L1-C01", "L1", 1, new BigDecimal("1.00"), SlotType.COMPACT, false, 1)
                )
        );
        ParkingManager manager = TestParkingFactory.createManager(lot, new OptimizedAllocationStrategy());
        LocalDateTime entryTime = LocalDateTime.of(2026, 3, 17, 8, 0);

        ParkVehicleResult firstPark = manager.parkVehicle(new Vehicle("CAR-201", VehicleType.CAR, entryTime, false));
        ExitVehicleResult exitResult = manager.exitVehicle("CAR-201", entryTime.plusMinutes(65));
        ParkVehicleResult secondPark = manager.parkVehicle(new Vehicle("CAR-202", VehicleType.CAR, entryTime.plusMinutes(70), false));

        assertTrue(firstPark.success());
        assertTrue(exitResult.success());
        assertTrue(secondPark.success());
        assertEquals("L1-C01", secondPark.ticket().getSlotId());
    }

    @Test
    void analyticsTrackRevenueDurationAndRejections() {
        ParkingLot lot = TestParkingFactory.createLot(
                "LOT-F",
                TestParkingFactory.createLevel(
                        "L1",
                        1,
                        new BigDecimal("1.00"),
                        TestParkingFactory.slot("L1-C01", "L1", 1, new BigDecimal("1.00"), SlotType.COMPACT, false, 1)
                )
        );
        ParkingManager manager = TestParkingFactory.createManager(lot, new OptimizedAllocationStrategy());
        LocalDateTime entryTime = LocalDateTime.of(2026, 3, 17, 9, 0);

        ParkVehicleResult first = manager.parkVehicle(new Vehicle("CAR-301", VehicleType.CAR, entryTime, false));
        ParkVehicleResult second = manager.parkVehicle(new Vehicle("CAR-302", VehicleType.CAR, entryTime.plusMinutes(5), false));
        ExitVehicleResult exit = manager.exitVehicle("CAR-301", entryTime.plusMinutes(125));
        AnalyticsSnapshot snapshot = manager.getAnalyticsSnapshot();

        assertTrue(first.success());
        assertFalse(second.success());
        assertTrue(exit.success());
        assertEquals(1, snapshot.totalVehiclesParked());
        assertEquals(1, snapshot.completedSessions());
        assertEquals(1, snapshot.rejectedVehicles());
        assertEquals(125.0, snapshot.averageParkingDurationMinutes());
        assertNotNull(snapshot.totalRevenue());
        assertEquals(RejectionReason.CAPACITY_EXHAUSTED, second.rejectionReason());
    }
}
