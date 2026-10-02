package org.example.smartbiobackend.model;

import jakarta.persistence.*;

@Entity
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    public BookingSeat() {
    }

    public BookingSeat(Booking booking, Seat seat, TicketType ticketType) {
        this.booking = booking;
        this.seat = seat;
        this.ticketType = ticketType;
    }

    public Seat getSeat() {
        return seat;
    }

    public TicketType getTicketType() {
        return ticketType;
    }
}