package org.example.smartbiobackend.controller;

import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.service.MovieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<Movie> getAll() {
        return movieService.getAll();
    }

    @PostMapping
    public Movie createMovie(@RequestBody Movie movie) {
        return movieService.createMovie(movie);
    }

    @PutMapping("/{movieId}")
    public Movie updateMovie(
            @PathVariable int movieId,
            @RequestBody Movie movie) {

        return movieService.updateMovie(movieId, movie);
    }

    @PutMapping("/{movieId}/deactivate")
    public void deactivateMovie(@PathVariable int movieId) {
        movieService.deactivateMovie(movieId);
    }

    @PutMapping("/{movieId}/promote")
    public void promoteMovie(@PathVariable int movieId) {
        movieService.promoteMovie(movieId);
    }

    @PutMapping("/{movieId}/remove-from-program")
    public void removeFromProgram(@PathVariable int movieId) {
        movieService.removeFromProgram(movieId);
    }

}