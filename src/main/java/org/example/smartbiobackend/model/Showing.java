package org.example.smartbiobackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import java.time.LocalDate;

@Entity
public class Showing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    private LocalDate date;

    public Showing() {
    }

    public Showing(Movie movie) {
        this.movie = movie;
    }

    public Showing(Movie movie, LocalDateTime startTime) {
        this.movie = movie;
        this.startTime = startTime;
    }

    public Showing(Movie movie, Auditorium auditorium, LocalDateTime startTime) {
        this.movie = movie;
        this.auditorium = auditorium;
        this.startTime = startTime;
    }

    public Showing(int id, Auditorium auditorium, Movie movie, LocalDate date, LocalDateTime startTime) {
        this.id = id;
        this.auditorium = auditorium;
        this.movie = movie;
        this.date = date;
        this.startTime = startTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Auditorium getAuditorium() {
        return auditorium;
    }

    public LocalDateTime getStartTime() {
        return startTime;
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