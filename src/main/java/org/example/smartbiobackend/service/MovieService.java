package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    public void deactivateMovie(int movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        movie.setActive(false);
        movieRepository.save(movie);
    }

    public void promoteMovie(int movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        movie.setPromoted(true);
        movieRepository.save(movie);
    }

    public void removeFromProgram(int movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        movie.setActive(false);
        movieRepository.save(movie);
    }

    public Movie createMovie(Movie movie) {
        movie.setActive(true);
        return movieRepository.save(movie);
    }

    public Movie updateMovie(int movieId, Movie updatedMovie) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        movie.setName(updatedMovie.getName());
        movie.setRunTime(updatedMovie.getRunTime());
        movie.setDescription(updatedMovie.getDescription());
        movie.setImdbRating(updatedMovie.getImdbRating());
        movie.setDirector(updatedMovie.getDirector());
        movie.setReleaseYear(updatedMovie.getReleaseYear());
        movie.setReleaseDate(updatedMovie.getReleaseDate());
        movie.setAgeRestriction(updatedMovie.getAgeRestriction());
        movie.setCategory(updatedMovie.getCategory());

        return movieRepository.save(movie);
    }
}