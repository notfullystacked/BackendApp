package org.example.smartbiobackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    public Movie getMovie() {
        return movie;
    }

    public Auditorium getAuditorium() {
        return auditorium;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}