package org.example.smartbiobackend.model;

import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    private Genre genre;

    // false = taget af programmet
    private boolean active = true;

    // Første dag filmen vises i Kino. Ligger den i fremtiden, er filmen "coming soon"
    private LocalDate premiereDate;

    // true = filmen fremhæves på forsiden (ISSUE-10)
    private boolean promoted;

    // Link til plakat, så frontenden kan vise et billede
    private String posterUrl;

    // Hvor mange dage efter premieren en film stadig tæller som premierefilm
    public static final int PREMIERE_DAYS = 14;

    // Constructor without imdb rating for now
    public Movie(int id, String name, int runTime, String description, String director, int releaseYear,
            LocalDate releaseDate, int ageRestriction) {
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

    public Movie(String name, int runTime, String description, String director, int releaseYear,
                 LocalDate releaseDate, int ageRestriction) {
        this.name = name;
        this.runTime = runTime;
        this.description = description;
        this.director = director;
        this.releaseYear = releaseYear;
        this.releaseDate = releaseDate;
        this.ageRestriction = ageRestriction;
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

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDate getPremiereDate() { return premiereDate; }
    public void setPremiereDate(LocalDate premiereDate) { this.premiereDate = premiereDate; }

    public boolean isPromoted() { return promoted; }
    public void setPromoted(boolean promoted) { this.promoted = promoted; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    // Kommer med i JSON som "status", fordi Jackson kalder alle getters.
    // Det er ikke en kolonne i databasen: JPA kigger kun på felterne, og der er intet felt, der hedder status
    public MovieStatus getStatus() {
        return getStatus(LocalDate.now());
    }

    // Samme beregning med en valgfri dato, så den kan unit-testes uden at afhænge af dagen i dag
    public MovieStatus getStatus(LocalDate today) {
        if (!active) {
            return MovieStatus.ARCHIVED;
        }
        if (premiereDate == null) {
            return MovieStatus.NOW_SHOWING;
        }
        if (premiereDate.isAfter(today)) {
            return MovieStatus.COMING_SOON;
        }
        if (today.isBefore(premiereDate.plusDays(PREMIERE_DAYS))) {
            return MovieStatus.PREMIERE;
        }
        return MovieStatus.NOW_SHOWING;
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
                ", genre=" + genre +
                ", active=" + active +
                ", premiereDate=" + premiereDate +
                ", promoted=" + promoted +
                '}';
    }
}