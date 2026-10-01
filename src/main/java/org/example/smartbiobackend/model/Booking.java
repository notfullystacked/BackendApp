package org.example.smartbiobackend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


<<<<<<< HEAD
=======
import java.util.ArrayList;
import java.util.List;

>>>>>>> ed37f68 (ISSUE-15: feat: add ticket retrieval endpoint)
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

<<<<<<< HEAD
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

=======
    @ManyToOne(optional = false)
    @JoinColumn(name = "showing_id", nullable = false)
    private Showing showing;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    public Booking() {
    }

    public Booking(Showing showing, String customerName, String customerEmail) {
        this.showing = showing;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
>>>>>>> ed37f68 (ISSUE-15: feat: add ticket retrieval endpoint)
    }

    public int getId() {
        return id;
    }

<<<<<<< HEAD
    public void setId(int bookingId) {
        this.id = bookingId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

=======
>>>>>>> ed37f68 (ISSUE-15: feat: add ticket retrieval endpoint)
    public Showing getShowing() {
        return showing;
    }

<<<<<<< HEAD
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
<<<<<<< HEAD
    }
=======
}
=======
    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<BookingSeat> getBookingSeats() {
        return bookingSeats;
    }
}
>>>>>>> ed37f68 (ISSUE-15: feat: add ticket retrieval endpoint)
>>>>>>> b87b4a2 (ISSUE-15: feat: add ticket retrieval endpoint)
