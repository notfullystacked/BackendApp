package org.example.smartbiobackend.model;

import jakarta.persistence.*;

@Entity
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String ticketName;

    @Column(nullable = false)
    private int price;

    public TicketType() {
    }

    public TicketType(String ticketName, int price) {
        this.ticketName = ticketName;
        this.price = price;
    }

    public String getTicketName() {
        return ticketName;
    }

    public int getPrice() {
        return price;
    }
}