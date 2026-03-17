package com.parking;

import com.parking.allocator.AllocationStrategy;
import com.parking.analytics.AnalyticsService;
import com.parking.bootstrap.DemoDataFactory;
import com.parking.model.ParkingLevel;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSlot;
import com.parking.model.SlotType;
import com.parking.repository.InMemoryTicketRepository;
import com.parking.service.ParkingManager;

import java.math.BigDecimal;
import java.util.List;

final class TestParkingFactory {
    private TestParkingFactory() {
    }

    static ParkingManager createManager(ParkingLot parkingLot, AllocationStrategy allocationStrategy) {
        return new ParkingManager(
                parkingLot,
                allocationStrategy,
                new InMemoryTicketRepository(),
                DemoDataFactory.createPricingService(),
                new AnalyticsService(parkingLot)
        );
    }

    static ParkingLot createLot(String lotId, ParkingLevel... levels) {
        return new ParkingLot(lotId, lotId, List.of(levels));
    }

    static ParkingLevel createLevel(String levelId, int levelNumber, BigDecimal pricingMultiplier, ParkingSlot... slots) {
        return new ParkingLevel(levelId, levelNumber, pricingMultiplier, List.of(slots));
    }

    static ParkingSlot slot(
            String slotId,
            String levelId,
            int levelNumber,
            BigDecimal pricingMultiplier,
            SlotType slotType,
            boolean evEnabled,
            int convenienceRank
    ) {
        return new ParkingSlot(slotId, levelId, levelNumber, pricingMultiplier, slotType, evEnabled, convenienceRank);
    }
}
