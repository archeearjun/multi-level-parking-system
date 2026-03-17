package com.parking.repository;

import com.parking.model.ParkingTicket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    void saveActive(ParkingTicket ticket);

    Optional<ParkingTicket> findActiveByLicensePlate(String licensePlate);

    Optional<ParkingTicket> findActiveByTicketId(String ticketId);

    boolean existsActiveTicketFor(String licensePlate);

    List<ParkingTicket> findAllActive();

    void archive(ParkingTicket ticket);

    List<ParkingTicket> findCompletedTickets();
}
