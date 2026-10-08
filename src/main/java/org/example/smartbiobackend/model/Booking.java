package org.example.smartbiobackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn
    private Showing showing;

    // null ved gæstebooking
    @ManyToOne
    @JoinColumn
    private User user;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    // Sæderne ligger kun her, så én booking kan have flere sæder
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    private boolean paid;

    // Sættes af billetkontrollen, når kunden møder op
    private boolean checkedIn;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Booking() {
    }

    public Booking(Showing showing, String customerName, String customerEmail) {
        this.showing = showing;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Showing getShowing() { return showing; }
    public void setShowing(Showing showing) { this.showing = showing; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public List<BookingSeat> getBookingSeats() { return bookingSeats; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Booking{id=" + id + ", showing=" + showing + ", customerName='" + customerName + "'}";
    }
}
