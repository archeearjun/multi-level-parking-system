package com.parking.service;

import com.parking.analytics.AnalyticsSnapshot;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingTicket;

import java.util.List;

public record LiveParkingState(
        String strategyName,
        ParkingLot parkingLot,
        AnalyticsSnapshot analyticsSnapshot,
        List<ParkingTicket> activeTickets
) {
}
