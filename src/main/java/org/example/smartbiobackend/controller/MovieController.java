package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.smartbiobackend.model.Genre;
import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.model.dto.MovieRequest;
import org.example.smartbiobackend.model.dto.ShowingResponse;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.service.MovieService;
import org.example.smartbiobackend.service.ShowingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;
    private final ShowingService showingService;
    private final RoleGuard roleGuard;

    public MovieController(MovieService movieService, ShowingService showingService, RoleGuard roleGuard) {
        this.movieService = movieService;
        this.showingService = showingService;
        this.roleGuard = roleGuard;
    }

    // Kunder: kun film på programmet. Kan filtreres: GET /api/movies?genre=HORROR&search=jaws
    @GetMapping
    public List<Movie> getActiveMovies(@RequestParam(name = "genre", required = false) Genre genre,
                                       @RequestParam(name = "search", required = false) String search) {
        return movieService.getActiveMovies(genre, search);
    }

    // Medarbejdere: også film der er taget af programmet
    @GetMapping("/all")
    public List<Movie> getAllMovies(HttpServletRequest http) {
        roleGuard.require(http);
        return movieService.getAllMovies();
    }

    // ISSUE-10: fremhævede film, kommende film og premierefilm
    @GetMapping("/promoted")
    public List<Movie> getPromotedMovies() {
        return movieService.getPromotedMovies();
    }

    @GetMapping("/coming-soon")
    public List<Movie> getComingSoon() {
        return movieService.getComingSoon();
    }

    @GetMapping("/premieres")
    public List<Movie> getPremieres() {
        return movieService.getPremieres();
    }

    // Genrerne ligger i en enum. Frontenden henter dem her til sin dropdown i stedet for at hardcode dem
    @GetMapping("/genres")
    public Genre[] getGenres() {
        return Genre.values();
    }

    @GetMapping("/{movieId}")
    public Movie getMovie(@PathVariable("movieId") int movieId) {
        return movieService.getMovie(movieId);
    }

    // Kommende forestillinger for én film
    @GetMapping("/{movieId}/showings")
    public List<ShowingResponse> getShowingsForMovie(@PathVariable("movieId") int movieId) {
        return showingService.getUpcomingForMovie(movieId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie createMovie(@RequestBody MovieRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return movieService.createMovie(request);
    }

    // Ingen @ResponseStatus her: der oprettes ikke noget nyt, så standardkoden 200 er den rigtige
    @PutMapping("/{movieId}")
    public Movie updateMovie(@PathVariable("movieId") int movieId, @RequestBody MovieRequest request,
                             HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return movieService.updateMovie(movieId, request);
    }

    // Tag filmen af programmet før tid
    @PutMapping("/{movieId}/deactivate")
    public Movie deactivateMovie(@PathVariable("movieId") int movieId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return movieService.deactivateMovie(movieId);
    }

    @PutMapping("/{movieId}/activate")
    public Movie activateMovie(@PathVariable("movieId") int movieId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return movieService.activateMovie(movieId);
    }

    @PutMapping("/{movieId}/promote")
    public Movie promoteMovie(@PathVariable("movieId") int movieId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return movieService.setPromoted(movieId, true);
    }

    @PutMapping("/{movieId}/unpromote")
    public Movie unpromoteMovie(@PathVariable("movieId") int movieId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return movieService.setPromoted(movieId, false);
    }

    // Slet helt. Virker kun for film uden forestillinger (ellers 409)
    @DeleteMapping("/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable("movieId") int movieId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        movieService.deleteMovie(movieId);
    }
}
