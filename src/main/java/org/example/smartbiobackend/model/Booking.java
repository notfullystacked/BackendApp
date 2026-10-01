package org.example.smartbiobackend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn
    private Showing showing;

    @ManyToOne
    @JoinColumn
    private User user;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    public Booking(Showing showing, User user, Seat seat) {
        this.showing = showing;
        this.user = user;
        this.seat = seat;
    }


    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingSeat> bookingSeats = new ArrayList<>();


    public Booking(Showing showing, String customerName, String customerEmail) {
        this.showing = showing;
        this.customerName = customerName;
        this.customerEmail = customerEmail;

    }

    public Booking() {
        //TODO Auto-generated constructor stub
    }

    public int getId() {
        return id;
    }

    public void setId(int bookingId) {
        this.id = bookingId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Showing getShowing() {
        return showing;
    }

    public void setShowing(Showing showing) {
        this.showing = showing;
    }
    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", showing=" + showing +
                ", user=" + user +
                ", seat=" + seat +
                '}';
    }

    public Optional<Booking> getBookingSeats() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBookingSeats'");
    }

    public Object getTicketType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTicketType'");
    }
    }
