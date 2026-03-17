package com.parking.bootstrap;

import com.parking.allocator.AllocationStrategy;
import com.parking.analytics.AnalyticsService;
import com.parking.model.ParkingLevel;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSlot;
import com.parking.model.SlotType;
import com.parking.model.VehicleType;
import com.parking.pricing.PricingService;
import com.parking.pricing.StandardPricingService;
import com.parking.repository.InMemoryTicketRepository;
import com.parking.service.ParkingManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class DemoDataFactory {
    private DemoDataFactory() {
    }

    public static ParkingManager createParkingManager(AllocationStrategy allocationStrategy) {
        ParkingLot parkingLot = createDemoParkingLot();
        return new ParkingManager(
                parkingLot,
                allocationStrategy,
                new InMemoryTicketRepository(),
                createPricingService(),
                new AnalyticsService(parkingLot)
        );
    }

    public static ParkingLot createDemoParkingLot() {
        return new ParkingLot(
                "LOT-001",
                "Downtown Multi-Level Parking",
                List.of(
                        createLevel("L1", 1, new BigDecimal("1.20"), 6, 18, 10, 2, 6, 3),
                        createLevel("L2", 2, new BigDecimal("1.10"), 8, 22, 12, 3, 4, 2),
                        createLevel("L3", 3, new BigDecimal("1.00"), 10, 24, 14, 4, 3, 2)
                )
        );
    }

    public static PricingService createPricingService() {
        Map<VehicleType, BigDecimal> rates = new EnumMap<>(VehicleType.class);
        rates.put(VehicleType.MOTORCYCLE, new BigDecimal("2.00"));
        rates.put(VehicleType.CAR, new BigDecimal("4.00"));
        rates.put(VehicleType.SUV, new BigDecimal("5.50"));
        rates.put(VehicleType.TRUCK, new BigDecimal("7.50"));
        rates.put(VehicleType.EV_CAR, new BigDecimal("4.50"));
        rates.put(VehicleType.EV_SUV, new BigDecimal("6.00"));
        return new StandardPricingService(rates, new BigDecimal("2.50"));
    }

    private static ParkingLevel createLevel(
            String levelId,
            int levelNumber,
            BigDecimal pricingMultiplier,
            int bikeSlots,
            int compactSlots,
            int largeSlots,
            int truckSlots,
            int compactEvSlots,
            int largeEvSlots
    ) {
        List<ParkingSlot> slots = new ArrayList<>();
        int convenience = 1;

        convenience = addSlots(slots, levelId, levelNumber, pricingMultiplier, SlotType.COMPACT, compactEvSlots, true, convenience);
        convenience = addSlots(slots, levelId, levelNumber, pricingMultiplier, SlotType.LARGE, largeEvSlots, true, convenience);
        convenience = addSlots(slots, levelId, levelNumber, pricingMultiplier, SlotType.BIKE, bikeSlots, false, convenience);
        convenience = addSlots(slots, levelId, levelNumber, pricingMultiplier, SlotType.COMPACT, compactSlots - compactEvSlots, false, convenience);
        convenience = addSlots(slots, levelId, levelNumber, pricingMultiplier, SlotType.LARGE, largeSlots - largeEvSlots, false, convenience);
        addSlots(slots, levelId, levelNumber, pricingMultiplier, SlotType.TRUCK, truckSlots, false, convenience);

        return new ParkingLevel(levelId, levelNumber, pricingMultiplier, slots);
    }

    private static int addSlots(
            List<ParkingSlot> slots,
            String levelId,
            int levelNumber,
            BigDecimal pricingMultiplier,
            SlotType slotType,
            int count,
            boolean evEnabled,
            int startConvenience
    ) {
        int convenience = startConvenience;
        String prefix = switch (slotType) {
            case BIKE -> "B";
            case COMPACT -> "C";
            case LARGE -> "L";
            case TRUCK -> "T";
        };
        for (int index = 1; index <= count; index++) {
            slots.add(new ParkingSlot(
                    levelId + "-" + prefix + "%02d".formatted(index) + (evEnabled ? "E" : ""),
                    levelId,
                    levelNumber,
                    pricingMultiplier,
                    slotType,
                    evEnabled,
                    convenience++
            ));
        }
        return convenience;
    }
}
