package org.example.smartbiobackend.service;

import org.example.smartbiobackend.exception.NotFoundException;
import org.example.smartbiobackend.model.Genre;
import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.ShowingStatus;
import org.example.smartbiobackend.model.dto.MovieRequest;
import org.example.smartbiobackend.repository.MovieRepository;
import org.example.smartbiobackend.repository.ShowingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    // ---------- Læs (ISSUE-12) ----------

    public List<Movie> getActiveMovies() {
        return movieRepository.findByActiveTrueOrderByName();
    }

    // Film på programmet, evt. filtreret på genre og/eller en søgetekst i titlen.
    // genre og search er null, hvis klienten ikke har sendt dem
    public List<Movie> getActiveMovies(Genre genre, String search) {
        return movieRepository.findByActiveTrueOrderByName().stream()
                .filter(movie -> genre == null || movie.getGenre() == genre)
                .filter(movie -> search == null || search.isBlank()
                        || movie.getName().toLowerCase().contains(search.trim().toLowerCase()))
                .toList();
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovie(int movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Film findes ikke: " + movieId));
    }

    // ---------- Opret, ret og fjern (ISSUE-8) ----------

    @Transactional
    public Movie createMovie(MovieRequest request) {
        validate(request);
        Movie movie = new Movie();
        applyRequest(movie, request);
        movie.setActive(true);
        return movieRepository.save(movie);
    }

    // PUT erstatter hele filmen: alle felter i requesten overskriver de gamle.
    // id kommer fra URL'en, ikke fra body'en, så man ikke kan komme til at rette en anden film
    @Transactional
    public Movie updateMovie(int movieId, MovieRequest request) {
        Movie movie = getMovie(movieId);
        validate(request);
        applyRequest(movie, request);
        return movieRepository.save(movie);
    }

    // Tag filmen af programmet før tid: skjul den og aflys dens fremtidige forestillinger.
    // Det er en "soft delete": rækken bliver i databasen, så gamle forestillinger og bookinger stadig peger på en film
    @Transactional
    public Movie deactivateMovie(int movieId) {
        Movie movie = getMovie(movieId);
        movie.setActive(false);
        movie.setPromoted(false);

        List<Showing> futureShowings = showingRepository
                .findByMovieIdAndStatusAndStartTimeAfter(movieId, ShowingStatus.ACTIVE, LocalDateTime.now());
        futureShowings.forEach(showing -> showing.setStatus(ShowingStatus.CANCELLED));
        showingRepository.saveAll(futureShowings);

        return movieRepository.save(movie);
    }

    // Sæt filmen på programmet igen. De aflyste forestillinger kommer ikke tilbage, de skal planlægges på ny
    @Transactional
    public Movie activateMovie(int movieId) {
        Movie movie = getMovie(movieId);
        movie.setActive(true);
        return movieRepository.save(movie);
    }

    // Rigtig sletning. Kun tilladt for film uden forestillinger (fx en film oprettet ved en fejl).
    // Ellers ville forestillinger og bookinger pege på en film, der ikke findes
    @Transactional
    public void deleteMovie(int movieId) {
        Movie movie = getMovie(movieId);
        if (showingRepository.existsByMovieId(movieId)) {
            throw new IllegalStateException("Filmen har forestillinger og kan ikke slettes. Tag den af programmet i stedet");
        }
        movieRepository.delete(movie);
    }

    // ---------- Promovering (ISSUE-10) ----------

    // Fremhævede film til forsiden
    public List<Movie> getPromotedMovies() {
        return movieRepository.findByActiveTrueAndPromotedTrueOrderByPremiereDateDesc();
    }

    // Film der har premiere senere end i dag
    public List<Movie> getComingSoon() {
        return movieRepository.findByActiveTrueAndPremiereDateAfterOrderByPremiereDate(LocalDate.now());
    }

    // Film der har haft premiere inden for de seneste 14 dage (Movie.PREMIERE_DAYS)
    public List<Movie> getPremieres() {
        LocalDate today = LocalDate.now();
        LocalDate firstPremiereDate = today.minusDays(Movie.PREMIERE_DAYS - 1);
        return movieRepository.findByActiveTrueAndPremiereDateBetweenOrderByPremiereDateDesc(firstPremiereDate, today);
    }

    @Transactional
    public Movie setPromoted(int movieId, boolean promoted) {
        Movie movie = getMovie(movieId);
        if (promoted && !movie.isActive()) {
            throw new IllegalStateException("En film, der er taget af programmet, kan ikke fremhæves");
        }
        movie.setPromoted(promoted);
        return movieRepository.save(movie);
    }

    // ---------- Hjælpemetoder ----------

    // name og runTime er påkrævede. De andre tal må gerne mangle (null)
    private void validate(MovieRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Filmen skal have et navn");
        }
        if (request.runTime() == null || request.runTime() <= 0) {
            throw new IllegalArgumentException("Filmen skal have en spilletid (runTime, i sekunder) over 0");
        }
        if (request.ageRestriction() != null && request.ageRestriction() < 0) {
            throw new IllegalArgumentException("Aldersgrænsen kan ikke være negativ");
        }
        if (request.imdbRating() != null && (request.imdbRating() < 0 || request.imdbRating() > 10)) {
            throw new IllegalArgumentException("IMDb-rating skal ligge mellem 0 og 10");
        }
    }

    private void applyRequest(Movie movie, MovieRequest request) {
        movie.setName(request.name().trim());
        movie.setRunTime(request.runTime());
        movie.setDescription(request.description());
        // Movie bruger int/double, som ikke kan være null. Mangler tallet, gemmes 0
        movie.setImdbRating(request.imdbRating() != null ? request.imdbRating() : 0);
        movie.setDirector(request.director());
        movie.setReleaseYear(request.releaseYear() != null ? request.releaseYear() : 0);
        movie.setReleaseDate(request.releaseDate());
        movie.setAgeRestriction(request.ageRestriction() != null ? request.ageRestriction() : 0);
        movie.setGenre(request.genre());
        movie.setPremiereDate(request.premiereDate());
        movie.setPosterUrl(request.posterUrl());
    }
}
