package org.example.smartbiobackend.model;

import jakarta.persistence.*;

@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String seatCode;

    public Seat() {
    }

    public Seat(String seatCode) {
        this.seatCode = seatCode;
    }

    public String getSeatCode() {
        return seatCode;
    }
}