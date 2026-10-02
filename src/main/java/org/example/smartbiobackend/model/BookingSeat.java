package org.example.smartbiobackend.model;

import jakarta.persistence.*;

@Entity
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn
    private Booking booking;

    @ManyToOne
    @JoinColumn
    private Seat seat;

    @ManyToOne
    @JoinColumn
    private TicketType ticketType;

    public BookingSeat() {}

    public BookingSeat(Booking booking, Seat seat, TicketType ticketType) {
        this.booking = booking;
        this.seat = seat;
        this.ticketType = ticketType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }
}
