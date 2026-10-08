package org.example.smartbiobackend.servicetest;

import org.example.smartbiobackend.exception.NotFoundException;
import org.example.smartbiobackend.model.Auditorium;
import org.example.smartbiobackend.model.Genre;
import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.ShowingStatus;
import org.example.smartbiobackend.model.dto.MovieRequest;
import org.example.smartbiobackend.repository.MovieRepository;
import org.example.smartbiobackend.repository.ShowingRepository;
import org.example.smartbiobackend.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock private MovieRepository movieRepository;
    @Mock private ShowingRepository showingRepository;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(movieRepository, showingRepository);
        // save(...) returnerer bare den film, den får ind
        lenient().when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private MovieRequest request(String name, Integer runTime) {
        return new MovieRequest(name, runTime, "Beskrivelse", 8.1, "Instruktør", 1975,
                LocalDate.of(1975, 6, 20), 15, Genre.HORROR, LocalDate.of(2026, 11, 1), null);
    }

    // ---------- ISSUE-8: opret ----------

    @Test
    void createMovieSavesAllFieldsAndIsActive() {
        Movie saved = movieService.createMovie(request("Jaws", 7440));

        assertEquals("Jaws", saved.getName());
        assertEquals(7440, saved.getRunTime());
        assertEquals(Genre.HORROR, saved.getGenre());
        assertEquals(LocalDate.of(2026, 11, 1), saved.getPremiereDate());
        assertTrue(saved.isActive());
        assertFalse(saved.isPromoted());
    }

    @Test
    void createMovieWithoutName_IsRejected() {
        assertThrows(IllegalArgumentException.class, () -> movieService.createMovie(request("  ", 7440)));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void createMovieWithoutRunTime_IsRejected() {
        assertThrows(IllegalArgumentException.class, () -> movieService.createMovie(request("Jaws", 0)));
        verify(movieRepository, never()).save(any());
    }

    // ---------- ISSUE-8: ret ----------

    @Test
    void updateMovieOverwritesTheExistingMovie() {
        Movie existing = new Movie("Gammelt navn");
        existing.setId(1);
        when(movieRepository.findById(1)).thenReturn(Optional.of(existing));

        Movie updated = movieService.updateMovie(1, request("Nyt navn", 6000));

        assertSame(existing, updated);          // samme objekt, altså en UPDATE og ikke en ny film
        assertEquals(1, updated.getId());
        assertEquals("Nyt navn", updated.getName());
        assertEquals(6000, updated.getRunTime());
    }

    @Test
    void updateMovieThatDoesNotExist_GivesNotFound() {
        when(movieRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> movieService.updateMovie(99, request("Jaws", 7440)));
    }

    @Test
    void updateMovieWithoutName_IsRejectedAndNothingIsSaved() {
        when(movieRepository.findById(1)).thenReturn(Optional.of(new Movie("Jaws")));

        assertThrows(IllegalArgumentException.class, () -> movieService.updateMovie(1, request(null, 7440)));
        verify(movieRepository, never()).save(any());
    }

    // ---------- ISSUE-8: fjern ----------

    @Test
    void deactivateMovieHidesItAndCancelsFutureShowings() {
        Movie movie = new Movie("Jaws");
        movie.setPromoted(true);
        Showing future = new Showing(movie, new Auditorium("Sal 1", 20, 12), LocalDateTime.now().plusDays(1));
        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));
        when(showingRepository.findByMovieIdAndStatusAndStartTimeAfter(eq(1), eq(ShowingStatus.ACTIVE), any()))
                .thenReturn(List.of(future));

        Movie result = movieService.deactivateMovie(1);

        assertFalse(result.isActive());
        assertFalse(result.isPromoted());
        assertEquals(ShowingStatus.CANCELLED, future.getStatus());
    }

    @Test
    void deleteMovieWithShowings_IsRejected() {
        when(movieRepository.findById(1)).thenReturn(Optional.of(new Movie("Jaws")));
        when(showingRepository.existsByMovieId(1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> movieService.deleteMovie(1));
        verify(movieRepository, never()).delete(any());
    }

    @Test
    void deleteMovieWithoutShowings_DeletesIt() {
        Movie movie = new Movie("Fejloprettet");
        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));
        when(showingRepository.existsByMovieId(1)).thenReturn(false);

        movieService.deleteMovie(1);

        verify(movieRepository).delete(movie);
    }

    // ---------- ISSUE-12: se film ----------

    @Test
    void getMovieThatDoesNotExist_GivesNotFound() {
        when(movieRepository.findById(99)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> movieService.getMovie(99));
        assertEquals("Film findes ikke: 99", exception.getMessage());
    }

    @Test
    void activeMoviesCanBeFilteredByGenreAndSearchText() {
        Movie jaws = new Movie("Jaws");
        jaws.setGenre(Genre.HORROR);
        Movie notebook = new Movie("The Notebook");
        notebook.setGenre(Genre.ROMANCE);
        when(movieRepository.findByActiveTrueOrderByName()).thenReturn(List.of(jaws, notebook));

        assertEquals(List.of(jaws), movieService.getActiveMovies(Genre.HORROR, null));
        assertEquals(List.of(notebook), movieService.getActiveMovies(null, "note"));
        assertEquals(List.of(jaws, notebook), movieService.getActiveMovies(null, null));
    }

    // ---------- ISSUE-10: promovering ----------

    @Test
    void promoteMovieSetsPromoted() {
        when(movieRepository.findById(1)).thenReturn(Optional.of(new Movie("Jaws")));

        assertTrue(movieService.setPromoted(1, true).isPromoted());
    }

    @Test
    void promoteMovieThatIsOffTheProgram_IsRejected() {
        Movie movie = new Movie("Jaws");
        movie.setActive(false);
        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));

        assertThrows(IllegalStateException.class, () -> movieService.setPromoted(1, true));
    }
}
