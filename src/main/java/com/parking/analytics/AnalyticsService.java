package com.parking.analytics;

import com.parking.model.ParkingLevel;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSlot;
import com.parking.model.ParkingTicket;
import com.parking.model.RejectionReason;
import com.parking.model.SlotType;
import com.parking.model.Vehicle;
import com.parking.model.VehicleType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class AnalyticsService {
    private final ParkingLot parkingLot;
    private final Map<String, Integer> capacityByLevel = new LinkedHashMap<>();
    private final Map<String, Integer> occupancyByLevel = new LinkedHashMap<>();
    private final Map<SlotType, Integer> totalSlotsByType = new EnumMap<>(SlotType.class);
    private final Map<SlotType, Integer> occupiedSlotsByType = new EnumMap<>(SlotType.class);
    private final Map<RejectionReason, Long> rejectedByReason = new EnumMap<>(RejectionReason.class);
    private final Map<VehicleType, BigDecimal> revenueByVehicleType = new EnumMap<>(VehicleType.class);
    private final Map<Integer, Long> acceptedEntriesByHour = new TreeMap<>();

    private long totalVehiclesParked;
    private long completedSessions;
    private long rejectedVehicles;
    private long evChargingRequests;
    private long evChargingAssignments;
    private long totalParkingMinutes;
    private int currentOccupiedSlots;
    private int currentActiveChargingSessions;
    private int peakOccupiedSlots;
    private LocalDateTime peakObservedAt;
    private BigDecimal totalRevenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    public AnalyticsService(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
        for (ParkingLevel level : parkingLot.getLevels()) {
            capacityByLevel.put(level.getLevelId(), level.getCapacity());
            occupancyByLevel.put(level.getLevelId(), 0);
        }
        for (SlotType slotType : SlotType.values()) {
            totalSlotsByType.put(slotType, 0);
            occupiedSlotsByType.put(slotType, 0);
        }
        for (RejectionReason reason : RejectionReason.values()) {
            rejectedByReason.put(reason, 0L);
        }
        for (VehicleType vehicleType : VehicleType.values()) {
            revenueByVehicleType.put(vehicleType, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        for (ParkingSlot slot : parkingLot.getAllSlots()) {
            totalSlotsByType.merge(slot.getSlotType(), 1, Integer::sum);
        }
    }

    public void recordParking(ParkingTicket ticket, ParkingSlot slot) {
        totalVehiclesParked++;
        currentOccupiedSlots++;
        occupancyByLevel.merge(slot.getLevelId(), 1, Integer::sum);
        occupiedSlotsByType.merge(slot.getSlotType(), 1, Integer::sum);
        acceptedEntriesByHour.merge(ticket.getEntryTime().getHour(), 1L, Long::sum);
        if (ticket.getVehicle().isChargingRequested()) {
            evChargingRequests++;
        }
        if (ticket.isChargingAllocated()) {
            evChargingAssignments++;
            currentActiveChargingSessions++;
        }
        if (currentOccupiedSlots > peakOccupiedSlots) {
            peakOccupiedSlots = currentOccupiedSlots;
            peakObservedAt = ticket.getEntryTime();
        }
    }

    public void recordExit(ParkingTicket ticket, ParkingSlot slot) {
        completedSessions++;
        currentOccupiedSlots--;
        occupancyByLevel.merge(slot.getLevelId(), -1, Integer::sum);
        occupiedSlotsByType.merge(slot.getSlotType(), -1, Integer::sum);
        if (ticket.isChargingAllocated()) {
            currentActiveChargingSessions--;
        }
        totalParkingMinutes += ticket.getParkedMinutes();
        totalRevenue = totalRevenue.add(ticket.getTotalFee()).setScale(2, RoundingMode.HALF_UP);
        revenueByVehicleType.merge(ticket.getVehicle().getVehicleType(), ticket.getTotalFee(), BigDecimal::add);
    }

    public void recordRejection(Vehicle vehicle, RejectionReason reason) {
        rejectedVehicles++;
        rejectedByReason.merge(reason, 1L, Long::sum);
    }

    public AnalyticsSnapshot createSnapshot() {
        Map<String, Double> occupancyPercentageByLevel = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : occupancyByLevel.entrySet()) {
            occupancyPercentageByLevel.put(entry.getKey(), percentage(entry.getValue(), capacityByLevel.get(entry.getKey())));
        }

        List<Integer> peakUsageHours = acceptedEntriesByHour.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        Map<VehicleType, BigDecimal> revenueCopy = new EnumMap<>(VehicleType.class);
        for (Map.Entry<VehicleType, BigDecimal> entry : revenueByVehicleType.entrySet()) {
            revenueCopy.put(entry.getKey(), entry.getValue().setScale(2, RoundingMode.HALF_UP));
        }

        return new AnalyticsSnapshot(
                parkingLot.getTotalCapacity(),
                currentOccupiedSlots,
                parkingLot.getTotalCapacity() - currentOccupiedSlots,
                percentage(currentOccupiedSlots, parkingLot.getTotalCapacity()),
                Map.copyOf(occupancyByLevel),
                Map.copyOf(occupancyPercentageByLevel),
                Map.copyOf(occupiedSlotsByType),
                Map.copyOf(totalSlotsByType),
                parkingLot.getEvChargingCapacity(),
                evChargingRequests,
                evChargingAssignments,
                currentActiveChargingSessions,
                percentage(evChargingAssignments, evChargingRequests),
                percentage(currentActiveChargingSessions, parkingLot.getEvChargingCapacity()),
                totalVehiclesParked,
                completedSessions,
                rejectedVehicles,
                Map.copyOf(rejectedByReason),
                totalRevenue.setScale(2, RoundingMode.HALF_UP),
                Map.copyOf(revenueCopy),
                completedSessions == 0 ? 0.0 : roundToTwoDecimals((double) totalParkingMinutes / completedSessions),
                peakOccupiedSlots,
                peakObservedAt,
                List.copyOf(new ArrayList<>(peakUsageHours))
        );
    }

    public int getCurrentOccupiedSlots() {
        return currentOccupiedSlots;
    }

    private double percentage(long value, long total) {
        if (total == 0) {
            return 0.0;
        }
        return roundToTwoDecimals(value * 100.0 / total);
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
