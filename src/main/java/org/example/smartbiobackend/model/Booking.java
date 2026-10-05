package org.example.smartbiobackend.model;

import jakarta.persistence.*;

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

    public Booking() {

    }

    private boolean paid;

    public boolean isPaid() {
        return paid;
    }


    public void setPaid(boolean paid) {
        this.paid = paid;
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
}
