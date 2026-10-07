package org.example.smartbiobackend.model;

import jakarta.persistence.*;

// Unik regel på (forestilling, sæde): databasen kan ikke gemme samme sæde to gange til samme forestilling
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"showing_id", "seat_id"}))
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(optional = false)
    @JoinColumn(name = "showing_id", nullable = false)
    private Showing showing;

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
        this.showing = booking != null ? booking.getShowing() : null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Showing getShowing() { return showing; }

    public Seat getSeat() { return seat; }
    public void setSeat(Seat seat) { this.seat = seat; }

    public TicketType getTicketType() { return ticketType; }
    public void setTicketType(TicketType ticketType) { this.ticketType = ticketType; }
}
