package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.SeatMapResponse;
import org.example.smartbiobackend.model.dto.SeatStatus;
import org.example.smartbiobackend.model.dto.ShowingRequest;
import org.example.smartbiobackend.model.dto.ShowingResponse;
import org.example.smartbiobackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShowingService {

    private static final int CLEANING_MINUTES = 15;   // pause mellem forestillinger i samme sal
    private static final int MONTHS_AHEAD = 3;

    private final ShowingRepository showingRepository;
    private final MovieRepository movieRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public ShowingService(ShowingRepository showingRepository, MovieRepository movieRepository,
                          AuditoriumRepository auditoriumRepository, SeatRepository seatRepository,
                          BookingSeatRepository bookingSeatRepository) {
        this.showingRepository = showingRepository;
        this.movieRepository = movieRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.seatRepository = seatRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    // Programmet: aktive forestillinger de næste 3 måneder
    @Transactional(readOnly = true)
    public List<ShowingResponse> getUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return showingRepository
                .findByStartTimeBetweenAndStatusOrderByStartTime(now, now.plusMonths(MONTHS_AHEAD), ShowingStatus.ACTIVE)
                .stream()
                .filter(showing -> showing.getMovie().isActive())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShowingResponse getShowing(int showingId) {
        return toResponse(findShowing(showingId));
    }

    @Transactional
    public ShowingResponse createShowing(ShowingRequest request) {
        Showing showing = new Showing();
        applyRequest(showing, request);
        return toResponse(showingRepository.save(showing));
    }

    @Transactional
    public ShowingResponse updateShowing(int showingId, ShowingRequest request) {
        Showing showing = findShowing(showingId);
        if (showing.getStatus() == ShowingStatus.CANCELLED) {
            throw new IllegalStateException("En aflyst forestilling kan ikke rettes");
        }
        applyRequest(showing, request);
        return toResponse(showingRepository.save(showing));
    }

    @Transactional
    public void cancelShowing(int showingId) {
        Showing showing = findShowing(showingId);
        showing.setStatus(ShowingStatus.CANCELLED);
        showingRepository.save(showing);
    }

    // Sædeoversigt: alle sæder i salen, og om de er booket til netop denne forestilling
    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(int showingId) {
        Showing showing = findShowing(showingId);
        Auditorium auditorium = showing.getAuditorium();

        Set<Integer> bookedSeatIds = bookingSeatRepository.findByShowingId(showingId).stream()
                .map(bookingSeat -> bookingSeat.getSeat().getId())
                .collect(Collectors.toSet());

        List<SeatStatus> seats = seatRepository.findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(auditorium.getId())
                .stream()
                .map(seat -> new SeatStatus(seat.getId(), seat.getSeatRow(), seat.getSeatNumber(),
                        bookedSeatIds.contains(seat.getId())))
                .toList();

        return new SeatMapResponse(showingId, auditorium.getAuditoriumName(),
                auditorium.getRowCount(), auditorium.getSeatsPerRow(), seats);
    }

    private void applyRequest(Showing showing, ShowingRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new IllegalArgumentException("Film findes ikke: " + request.movieId()));
        if (!movie.isActive()) {
            throw new IllegalStateException("Filmen er taget af programmet");
        }

        Auditorium auditorium = auditoriumRepository.findById(request.auditoriumId())
                .orElseThrow(() -> new IllegalArgumentException("Sal findes ikke: " + request.auditoriumId()));

        if (request.startTime() == null || request.startTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Starttidspunktet skal ligge i fremtiden");
        }

        checkAuditoriumIsFree(showing.getId(), auditorium.getId(), request.startTime(), movie);

        showing.setMovie(movie);
        showing.setAuditorium(auditorium);
        showing.setStartTime(request.startTime());
        showing.setDate(request.startTime().toLocalDate());
        showing.setStatus(ShowingStatus.ACTIVE);
    }

    // To forestillinger må ikke overlappe i samme sal (inkl. rengøring)
    private void checkAuditoriumIsFree(int showingId, int auditoriumId, LocalDateTime start, Movie movie) {
        LocalDateTime end = endTime(start, movie);

        for (Showing other : showingRepository.findByAuditoriumIdAndStatus(auditoriumId, ShowingStatus.ACTIVE)) {
            if (other.getId() == showingId) {
                continue; // det er den forestilling vi er ved at rette
            }
            LocalDateTime otherStart = other.getStartTime();
            LocalDateTime otherEnd = endTime(otherStart, other.getMovie());

            if (start.isBefore(otherEnd) && otherStart.isBefore(end)) {
                throw new IllegalStateException("Salen er optaget af " + other.getMovie().getName()
                        + " fra " + otherStart + " til " + otherEnd);
            }
        }
    }

    // runTime er i sekunder (se Movie)
    private LocalDateTime endTime(LocalDateTime start, Movie movie) {
        return start.plusSeconds(movie.getRunTime()).plusMinutes(CLEANING_MINUTES);
    }

    private Showing findShowing(int showingId) {
        return showingRepository.findById(showingId)
                .orElseThrow(() -> new IllegalArgumentException("Forestilling findes ikke: " + showingId));
    }

    private ShowingResponse toResponse(Showing showing) {
        Movie movie = showing.getMovie();
        Auditorium auditorium = showing.getAuditorium();
        return new ShowingResponse(showing.getId(), movie.getId(), movie.getName(), movie.getGenre(),
                movie.getAgeRestriction(), movie.getRunTime(), auditorium.getId(),
                auditorium.getAuditoriumName(), showing.getStartTime(), showing.getStatus());
    }
}
