package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;
    private final RoleGuard roleGuard;

    public MovieController(MovieService movieService, RoleGuard roleGuard) {
        this.movieService = movieService;
        this.roleGuard = roleGuard;
    }

    // Kunder: kun film på programmet
    @GetMapping
    public List<Movie> getActiveMovies() {
        return movieService.getActiveMovies();
    }

    // Medarbejdere: også film der er taget af programmet
    @GetMapping("/all")
    public List<Movie> getAllMovies(HttpServletRequest http) {
        roleGuard.require(http);
        return movieService.getAllMovies();
    }

    @GetMapping("/{movieId}")
    public Movie getMovie(@PathVariable("movieId") int movieId) {
        return movieService.getMovie(movieId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie createMovie(@RequestBody Movie movie, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return movieService.createMovie(movie);
    }

    @PutMapping("/{movieId}")
    public Movie updateMovie(@PathVariable("movieId") int movieId, @RequestBody Movie movie, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return movieService.updateMovie(movieId, movie);
    }

    // Tag filmen af programmet før tid
    @PutMapping("/{movieId}/deactivate")
    public Movie deactivateMovie(@PathVariable("movieId") int movieId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return movieService.deactivateMovie(movieId);
    }
}
