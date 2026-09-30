package org.example.smartbiobackend.model;


import jakarta.persistence.*;

@Entity
public class Seat {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int seatId;

   @ManyToOne
   @JoinColumn(name = "auditorium_id")
   private Auditorium auditorium;

   private String seatCode;

}
