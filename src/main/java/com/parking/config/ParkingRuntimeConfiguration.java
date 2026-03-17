package com.parking.config;

import com.parking.pricing.PricingService;
import com.parking.simulation.SimulationEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParkingRuntimeConfiguration {
    @Bean
    PricingService pricingService() {
        return com.parking.bootstrap.DemoDataFactory.createPricingService();
    }

    @Bean
    SimulationEngine simulationEngine() {
        return new SimulationEngine();
    }
}
