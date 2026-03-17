package com.parking.repository;

import com.parking.model.ParkingTicket;
import com.parking.model.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryTicketRepository implements TicketRepository {
    private final Map<String, ParkingTicket> activeByLicense = new HashMap<>();
    private final Map<String, ParkingTicket> activeByTicketId = new HashMap<>();
    private final List<ParkingTicket> completedTickets = new ArrayList<>();

    @Override
    public void saveActive(ParkingTicket ticket) {
        String plate = ticket.getVehicle().getLicensePlate();
        activeByLicense.put(plate, ticket);
        activeByTicketId.put(ticket.getTicketId(), ticket);
    }

    @Override
    public Optional<ParkingTicket> findActiveByLicensePlate(String licensePlate) {
        return Optional.ofNullable(activeByLicense.get(Vehicle.normalizePlate(licensePlate)));
    }

    @Override
    public Optional<ParkingTicket> findActiveByTicketId(String ticketId) {
        return Optional.ofNullable(activeByTicketId.get(ticketId));
    }

    @Override
    public boolean existsActiveTicketFor(String licensePlate) {
        return activeByLicense.containsKey(Vehicle.normalizePlate(licensePlate));
    }

    @Override
    public List<ParkingTicket> findAllActive() {
        return List.copyOf(activeByLicense.values());
    }

    @Override
    public void archive(ParkingTicket ticket) {
        activeByLicense.remove(ticket.getVehicle().getLicensePlate());
        activeByTicketId.remove(ticket.getTicketId());
        completedTickets.add(ticket);
    }

    @Override
    public List<ParkingTicket> findCompletedTickets() {
        return List.copyOf(completedTickets);
    }
}
