package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.ShowingStatus;
import org.example.smartbiobackend.repository.MovieRepository;
import org.example.smartbiobackend.repository.ShowingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowingRepository showingRepository;

    public MovieService(MovieRepository movieRepository, ShowingRepository showingRepository) {
        this.movieRepository = movieRepository;
        this.showingRepository = showingRepository;
    }

    public List<Movie> getActiveMovies() {
        return movieRepository.findByActiveTrue();
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovie(int movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Film findes ikke: " + movieId));
    }

    @Transactional
    public Movie createMovie(Movie movie) {
        if (movie.getName() == null || movie.getName().isBlank()) {
            throw new IllegalArgumentException("Filmen skal have et navn");
        }
        movie.setId(0);
        movie.setActive(true);
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie updateMovie(int movieId, Movie changes) {
        Movie movie = getMovie(movieId);
        movie.setName(changes.getName());
        movie.setRunTime(changes.getRunTime());
        movie.setDescription(changes.getDescription());
        movie.setImdbRating(changes.getImdbRating());
        movie.setDirector(changes.getDirector());
        movie.setReleaseYear(changes.getReleaseYear());
        movie.setReleaseDate(changes.getReleaseDate());
        movie.setAgeRestriction(changes.getAgeRestriction());
        movie.setGenre(changes.getGenre());
        return movieRepository.save(movie);
    }

    // Tag filmen af programmet før tid: skjul den og aflys dens fremtidige forestillinger
    @Transactional
    public Movie deactivateMovie(int movieId) {
        Movie movie = getMovie(movieId);
        movie.setActive(false);

        List<Showing> futureShowings = showingRepository
                .findByMovieIdAndStatusAndStartTimeAfter(movieId, ShowingStatus.ACTIVE, LocalDateTime.now());
        futureShowings.forEach(showing -> showing.setStatus(ShowingStatus.CANCELLED));
        showingRepository.saveAll(futureShowings);

        return movieRepository.save(movie);
    }
}
