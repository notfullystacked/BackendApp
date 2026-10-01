package org.example.smartbiobackend.model;

import jakarta.persistence.*;

@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String seatCode;

   @ManyToOne
   @JoinColumn
   private Auditorium auditorium;

    public Seat() {
    }

    public Seat(String seatCode) {
        this.seatCode = seatCode;
    }

    public String getSeatCode() {
        return seatCode;
    }

   public Seat(int id, Auditorium auditorium, String seatCode) {
      this.id = id;
      this.auditorium = auditorium;
      this.seatCode = seatCode;
   }


   public Seat(Auditorium auditorium, String seatCode) {
      this.auditorium = auditorium;
      this.seatCode = seatCode;
   }


   public int getId() {
      return id;
   }

   public void setId(int seatId) {
      this.id = seatId;
   }

   public Auditorium getAuditorium() {
      return auditorium;
   }

   public void setAuditorium(Auditorium auditorium) {
      this.auditorium = auditorium;
   }



   public void setSeatCode(String seatCode) {
      this.seatCode = seatCode;
   }

   @Override
   public String toString() {
      return "Seat{" +
              "id=" + id +
              ", auditorium=" + auditorium +
              ", seatCode='" + seatCode + '\'' +
              '}';
   }

}
   