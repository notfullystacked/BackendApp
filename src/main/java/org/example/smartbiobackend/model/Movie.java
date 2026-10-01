package org.example.smartbiobackend.model;


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

    private String name;

    // In seconds
    private int runTime;

    private String description;

    private int imdb_rating;

    private String director;

    private int releaseYear;

    private LocalDate releaseDate;

    private int ageRestriction;


    // Constructor without imdb rating for now
    public Movie(int id, String name, int runTime, String description, String director, int releaseYear, LocalDate releaseDate, int ageRestriction) {
        this.id = id;
        this.name = name;
        this.runTime = runTime;
        this.description = description;
        this.director = director;
        this.releaseYear = releaseYear;
        this.releaseDate = releaseDate;
        this.ageRestriction = ageRestriction;
    }

    public Movie() {

    }

    public Movie(String movieName, int runtime, String description, String director, int releaseYear, LocalDate releaseDate, int ageRestriction) {
        this.name = movieName;
        this.runTime = runtime;
        this.description = description;
        this.director = director;
        this.releaseYear = releaseYear;
        this.releaseDate = releaseDate;
        this.ageRestriction = ageRestriction;
    }

    public int getId() {
        return id;
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

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImdb_rating() {
        return imdb_rating;
    }

    public void setImdb_rating(int imdb_rating) {
        this.imdb_rating = imdb_rating;
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

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", runTime=" + runTime +
                ", description='" + description + '\'' +
                ", imdb_rating=" + imdb_rating +
                ", director='" + director + '\'' +
                ", releaseYear=" + releaseYear +
                ", releaseDate=" + releaseDate +
                ", ageRestriction=" + ageRestriction +
                '}';
    }
}

