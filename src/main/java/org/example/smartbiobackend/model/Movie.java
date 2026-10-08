package org.example.smartbiobackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public Movie(String name) {
        this.name = name;
    }

    @Column(nullable = false)
    private String name;

    // in seconds
    private int runTime;
@Column(length = 2000)
    private String description;

    private double imdbRating;

    private String director;

    private int releaseYear;

    private LocalDate releaseDate;

    private int ageRestriction;

    private String category;

    private boolean active = true;

    private boolean promoted = false;

    // Constructor without imdb rating for now
    public Movie(int id, String name, int runTime, String description, String director, int releaseYear,
            LocalDate releaseDate, int ageRestriction, String category) {
        this.id = id;
        this.name = name;
        this.runTime = runTime;
        this.description = description;
        this.director = director;
        this.releaseYear = releaseYear;
        this.releaseDate = releaseDate;
        this.ageRestriction = ageRestriction;
        this.category = category;

    }

    public Movie() {
    }

    public Movie(String name, int runTime, String description, String director, int releaseYear,
                 LocalDate releaseDate, int ageRestriction, String category) {
        this.name = name;
        this.runTime = runTime;
        this.description = description;
        this.director = director;
        this.releaseYear = releaseYear;
        this.releaseDate = releaseDate;
        this.ageRestriction = ageRestriction;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    public void setId(int movieId) {
        this.id = movieId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(double imdbRating) {
        this.imdbRating = imdbRating;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }

    public void setAgeRestriction(int ageRestriction) {
        this.ageRestriction = ageRestriction;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPromoted() {
        return promoted;
    }

    public void setPromoted(boolean promoted) {
        this.promoted = promoted;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", runTime=" + runTime +
                ", description='" + description + '\'' +
                ", imdbRating=" + imdbRating +
                ", director='" + director + '\'' +
                ", releaseYear=" + releaseYear +
                ", releaseDate=" + releaseDate +
                ", ageRestriction=" + ageRestriction +
                ", category=" + category + "/" +
                ", active=" + active +
                '}';
    }
}