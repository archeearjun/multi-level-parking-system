package com.parking.simulation;

import com.parking.analytics.AnalyticsSnapshot;
import com.parking.model.Vehicle;
import com.parking.model.VehicleType;
import com.parking.service.ExitVehicleResult;
import com.parking.service.ParkVehicleResult;
import com.parking.service.ParkingManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public final class SimulationEngine {
    public List<VehicleArrivalRequest> generateRequests(SimulationConfig config) {
        Random random = new Random(config.seed());
        List<VehicleArrivalRequest> requests = new ArrayList<>();
        int sequence = 1;

        for (int step = 0; step < config.totalSteps(); step++) {
            LocalDateTime arrivalTime = config.startTime().plusMinutes((long) step * config.stepMinutes());
            if (random.nextDouble() > config.arrivalProbability()) {
                continue;
            }

            int arrivalsThisStep = 1 + random.nextInt(config.maxArrivalsPerStep());
            for (int index = 0; index < arrivalsThisStep; index++) {
                VehicleType vehicleType = pickVehicleType(random);
                boolean chargingRequested = vehicleType.isElectric() && random.nextDouble() < 0.80;
                int dwellMinutes = config.minParkingMinutes()
                        + random.nextInt(config.maxParkingMinutes() - config.minParkingMinutes() + 1);
                requests.add(new VehicleArrivalRequest(
                        "SIM-%05d".formatted(sequence++),
                        vehicleType,
                        chargingRequested,
                        arrivalTime.plusMinutes(index),
                        dwellMinutes
                ));
            }
        }
        return requests;
    }

    public SimulationResult runSimulation(ParkingManager parkingManager, SimulationConfig config) {
        return runSimulation(parkingManager, generateRequests(config));
    }

    public SimulationResult runSimulation(ParkingManager parkingManager, List<VehicleArrivalRequest> requests) {
        record ExitSchedule(String licensePlate, LocalDateTime exitTime) {
        }

        long startedAt = System.nanoTime();
        long totalAllocationNanos = 0L;
        long occupiedMinutesArea = 0L;
        LocalDateTime firstEventTime = requests.isEmpty() ? LocalDateTime.now() : requests.get(0).arrivalTime();
        LocalDateTime previousEventTime = firstEventTime;
        LocalDateTime lastEventTime = firstEventTime;
        PriorityQueue<ExitSchedule> pendingExits = new PriorityQueue<>((left, right) -> left.exitTime().compareTo(right.exitTime()));

        for (VehicleArrivalRequest request : requests) {
            while (!pendingExits.isEmpty() && !pendingExits.peek().exitTime().isAfter(request.arrivalTime())) {
                ExitSchedule dueExit = pendingExits.poll();
                occupiedMinutesArea += occupancyMinutesSince(previousEventTime, dueExit.exitTime(), parkingManager);
                previousEventTime = dueExit.exitTime();
                ExitVehicleResult exitResult = parkingManager.exitVehicle(dueExit.licensePlate(), dueExit.exitTime());
                if (exitResult.success()) {
                    lastEventTime = dueExit.exitTime();
                }
            }

            occupiedMinutesArea += occupancyMinutesSince(previousEventTime, request.arrivalTime(), parkingManager);
            previousEventTime = request.arrivalTime();
            lastEventTime = request.arrivalTime();
            ParkVehicleResult parkResult = parkingManager.parkVehicle(new Vehicle(
                    request.licensePlate(),
                    request.vehicleType(),
                    request.arrivalTime(),
                    request.chargingRequested()
            ));
            totalAllocationNanos += parkResult.allocationNanos();
            if (parkResult.success()) {
                pendingExits.add(new ExitSchedule(
                        request.licensePlate(),
                        request.arrivalTime().plusMinutes(request.plannedParkingMinutes())
                ));
            }
        }

        while (!pendingExits.isEmpty()) {
            ExitSchedule dueExit = pendingExits.poll();
            occupiedMinutesArea += occupancyMinutesSince(previousEventTime, dueExit.exitTime(), parkingManager);
            previousEventTime = dueExit.exitTime();
            ExitVehicleResult exitResult = parkingManager.exitVehicle(dueExit.licensePlate(), dueExit.exitTime());
            if (exitResult.success()) {
                lastEventTime = dueExit.exitTime();
            }
        }

        long wallClockNanos = System.nanoTime() - startedAt;
        AnalyticsSnapshot snapshot = parkingManager.getAnalyticsSnapshot();
        long totalRequests = requests.size();
        long totalDurationMinutes = Math.max(1, Duration.between(firstEventTime, lastEventTime).toMinutes());
        double averageOccupancyPercentage = roundToTwoDecimals(
                occupiedMinutesArea * 100.0 / (parkingManager.getParkingLot().getTotalCapacity() * (double) totalDurationMinutes)
        );
        double throughput = wallClockNanos == 0
                ? 0.0
                : roundToTwoDecimals(totalRequests * 1_000_000_000.0 / wallClockNanos);
        double averageAllocationMicros = totalRequests == 0
                ? 0.0
                : roundToTwoDecimals(totalAllocationNanos / 1_000.0 / totalRequests);

        return new SimulationResult(
                parkingManager.getAllocationStrategyName(),
                totalRequests,
                snapshot.totalVehiclesParked(),
                snapshot.rejectedVehicles(),
                snapshot.completedSessions(),
                roundToTwoDecimals(totalRequests == 0 ? 0.0 : snapshot.rejectedVehicles() * 100.0 / totalRequests),
                averageAllocationMicros,
                throughput,
                averageOccupancyPercentage,
                snapshot.evChargingFulfillmentRate(),
                snapshot.totalRevenue().setScale(2, RoundingMode.HALF_UP),
                wallClockNanos
        );
    }

    private long occupancyMinutesSince(LocalDateTime previousTime, LocalDateTime currentTime, ParkingManager manager) {
        // Time-weighted occupancy lets the benchmark compare capacity utilization, not just end-state counts.
        long minutes = Math.max(0, Duration.between(previousTime, currentTime).toMinutes());
        return minutes * manager.getAnalyticsSnapshot().currentOccupiedSlots();
    }

    private VehicleType pickVehicleType(Random random) {
        int bucket = random.nextInt(100);
        if (bucket < 15) {
            return VehicleType.MOTORCYCLE;
        }
        if (bucket < 50) {
            return VehicleType.CAR;
        }
        if (bucket < 70) {
            return VehicleType.SUV;
        }
        if (bucket < 80) {
            return VehicleType.TRUCK;
        }
        if (bucket < 95) {
            return VehicleType.EV_CAR;
        }
        return VehicleType.EV_SUV;
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
