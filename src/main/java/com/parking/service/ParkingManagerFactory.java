package com.parking.service;

import com.parking.allocator.AllocationStrategy;
import com.parking.allocator.OptimizedAllocationStrategy;
import com.parking.analytics.AnalyticsService;
import com.parking.bootstrap.DemoDataFactory;
import com.parking.model.ParkingLot;
import com.parking.pricing.PricingService;
import com.parking.repository.InMemoryTicketRepository;
import org.springframework.stereotype.Component;

@Component
public class ParkingManagerFactory {
    private final PricingService pricingService;

    public ParkingManagerFactory(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    public ParkingManager createManager(AllocationStrategy allocationStrategy) {
        ParkingLot parkingLot = DemoDataFactory.createDemoParkingLot();
        return new ParkingManager(
                parkingLot,
                allocationStrategy,
                new InMemoryTicketRepository(),
                pricingService,
                new AnalyticsService(parkingLot)
        );
    }

    public ParkingManager createOptimizedManager() {
        return createManager(new OptimizedAllocationStrategy());
    }
}
