package org.example.smartbiobackend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Showing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn
    private Auditorium auditorium;

    @ManyToOne
    @JoinColumn
    private Movie movie;

    private LocalDate date;

    private LocalDateTime startTime;

    public Showing(int id, Auditorium auditorium, Movie movie, LocalDate date, LocalDateTime startTime) {
        this.id = id;
        this.auditorium = auditorium;
        this.movie = movie;
        this.date = date;
        this.startTime = startTime;
    }

    public Showing() {

    }


    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int showingId) {
        this.id = showingId;
    }


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Showing(Auditorium auditorium, Movie movie, LocalDate date, LocalDateTime startTime) {
        this.auditorium = auditorium;
        this.movie = movie;
        this.date = date;
        this.startTime = startTime;
    }
    public Showing(Movie movie2, Auditorium auditorium2, LocalDateTime of) {
        //TODO Auto-generated constructor stub
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Auditorium getAuditorium() {
        return auditorium;
    }

    public void setAuditorium(Auditorium auditorium) {
        this.auditorium = auditorium;
    }
    @Override
    public String toString() {
        return "Showing{" +
                "id=" + id +
                ", auditorium=" + auditorium +
                ", movie=" + movie +
                ", date=" + date +
                ", startTime=" + startTime +
                '}';
    }
}
