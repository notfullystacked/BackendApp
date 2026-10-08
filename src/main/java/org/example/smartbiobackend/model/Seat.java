package org.example.smartbiobackend.model;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"auditorium_id", "seat_row", "seat_number"}))
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "auditorium_id")
    private Auditorium auditorium;

    @Column(name = "seat_row")
    private int seatRow;

    @Column(name = "seat_number")
    private int seatNumber;

    // Fx "3-12" = række 3, sæde 12. Udfyldes automatisk
    @Column(nullable = false)
    private String seatCode;

    public Seat() {
    }

    public Seat(Auditorium auditorium, int seatRow, int seatNumber) {
        this.auditorium = auditorium;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.seatCode = seatRow + "-" + seatNumber;
    }

    // Bruges af de eksisterende TicketService-tests
    public Seat(String seatCode) {
        this.seatCode = seatCode;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Auditorium getAuditorium() { return auditorium; }
    public void setAuditorium(Auditorium auditorium) { this.auditorium = auditorium; }

    public int getSeatRow() { return seatRow; }
    public int getSeatNumber() { return seatNumber; }
    public String getSeatCode() { return seatCode; }

    @Override
    public String toString() {
        return "Seat{id=" + id + ", seatCode='" + seatCode + "'}";
    }
}
