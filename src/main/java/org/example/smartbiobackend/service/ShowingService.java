package org.example.smartbiobackend.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.example.smartbiobackend.model.Auditorium;
import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.ShowingStatus;
import org.example.smartbiobackend.repository.AuditoriumRepository;
import org.example.smartbiobackend.repository.MovieRepository;
import org.example.smartbiobackend.repository.ShowingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowingService {

    private final ShowingRepository showingRepository;
    private final MovieRepository movieRepository;
    private final AuditoriumRepository auditoriumRepository;

    public ShowingService(
            ShowingRepository showingRepository,
            MovieRepository movieRepository,
            AuditoriumRepository auditoriumRepository) {

        this.showingRepository = showingRepository;
        this.movieRepository = movieRepository;
        this.auditoriumRepository = auditoriumRepository;
    }

    public void cancelShowing(int showingId) {
        Showing showing = showingRepository.findById(showingId)
                .orElseThrow(() -> new IllegalArgumentException("Showing not found"));

        showing.setStatus(ShowingStatus.CANCELLED);
        showingRepository.save(showing);
    }

    public Showing createShowing(
            int movieId,
            int auditoriumId,
            LocalDate date,
            LocalDateTime startTime) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        Auditorium auditorium = auditoriumRepository.findById(auditoriumId)
                .orElseThrow(() -> new IllegalArgumentException("Auditorium not found"));

        List<Showing> existingShowings =
                showingRepository.findByAuditorium_IdAndDate(auditoriumId, date);

        for (Showing existingShowing : existingShowings) {

            LocalDateTime existingStart = existingShowing.getStartTime();

            int existingRuntime = existingShowing.getMovie().getRunTime();

            LocalDateTime existingEnd =
                    existingStart.plusSeconds(existingRuntime);

            LocalDateTime newEnd =
                    startTime.plusSeconds(movie.getRunTime());

            boolean overlaps =
                    startTime.isBefore(existingEnd)
                            && newEnd.isAfter(existingStart);

            if (overlaps) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "There is already a showing in this auditorium at this time"
                );
            }
        }

        Showing showing = new Showing(
                auditorium,
                movie,
                date,
                startTime
        );

        return showingRepository.save(showing);
    }

    public Showing updateShowing(
            int showingId,
            int movieId,
            int auditoriumId,
            LocalDate date,
            LocalDateTime startTime) {

        Showing showing = showingRepository.findById(showingId)
                .orElseThrow(() -> new IllegalArgumentException("Showing not found"));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        Auditorium auditorium = auditoriumRepository.findById(auditoriumId)
                .orElseThrow(() -> new IllegalArgumentException("Auditorium not found"));

        List<Showing> existingShowings =
                showingRepository.findByAuditorium_IdAndDate(auditoriumId, date);

        for (Showing existingShowing : existingShowings) {

            // Don't compare the showing against itself
            if (existingShowing.getId() == showingId) {
                continue;
            }

            LocalDateTime existingStart = existingShowing.getStartTime();

            int existingRuntime = existingShowing.getMovie().getRunTime();

            LocalDateTime existingEnd =
                    existingStart.plusSeconds(existingRuntime);

            LocalDateTime newEnd =
                    startTime.plusSeconds(movie.getRunTime());

            boolean overlaps =
                    startTime.isBefore(existingEnd)
                            && newEnd.isAfter(existingStart);

            if (overlaps) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "There is already a showing in this auditorium at this time"
                );
            }
        }

        showing.setMovie(movie);
        showing.setAuditorium(auditorium);
        showing.setDate(date);
        showing.setStartTime(startTime);

        return showingRepository.save(showing);
    }

    public List<Showing> getThreeMonthPlan() {
        LocalDate today = LocalDate.now();
        LocalDate threeMonthsFromNow = today.plusMonths(3);

        return showingRepository.findByDateBetween(
                today,
                threeMonthsFromNow
        );
    }
}