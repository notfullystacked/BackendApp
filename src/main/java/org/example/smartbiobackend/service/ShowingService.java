package org.example.smartbiobackend.service;

import org.example.smartbiobackend.exception.NotFoundException;
import org.example.smartbiobackend.model.Auditorium;
import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.ShowingStatus;
import org.example.smartbiobackend.model.dto.SeatMapResponse;
import org.example.smartbiobackend.model.dto.SeatStatus;
import org.example.smartbiobackend.model.dto.ShowingRequest;
import org.example.smartbiobackend.model.dto.ShowingResponse;
import org.example.smartbiobackend.repository.AuditoriumRepository;
import org.example.smartbiobackend.repository.BookingSeatRepository;
import org.example.smartbiobackend.repository.MovieRepository;
import org.example.smartbiobackend.repository.SeatRepository;
import org.example.smartbiobackend.repository.ShowingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShowingService {

    private static final int CLEANING_MINUTES = 15;   // pause mellem forestillinger i samme sal
    private static final int MONTHS_AHEAD = 3;        // programmet planlægges 3 måneder frem

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
        return getUpcoming(null, null);
    }

    // Samme, men evt. kun for én film og/eller én dato. movieId og date er null, hvis de ikke er sendt med
    @Transactional(readOnly = true)
    public List<ShowingResponse> getUpcoming(Integer movieId, LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        return showingRepository
                .findByStartTimeBetweenAndStatusOrderByStartTime(now, now.plusMonths(MONTHS_AHEAD), ShowingStatus.ACTIVE)
                .stream()
                .filter(showing -> showing.getMovie().isActive())
                .filter(showing -> movieId == null || showing.getMovie().getId() == movieId)
                .filter(showing -> date == null || showing.getStartTime().toLocalDate().equals(date))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShowingResponse> getUpcomingForMovie(int movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new NotFoundException("Film findes ikke: " + movieId);
        }
        return showingRepository
                .findByMovieIdAndStatusAndStartTimeAfterOrderByStartTime(movieId, ShowingStatus.ACTIVE, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Medarbejdere: alle forestillinger, også gamle og aflyste
    @Transactional(readOnly = true)
    public List<ShowingResponse> getAll() {
        return showingRepository.findAllByOrderByStartTime().stream()
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
        // Sæderne i en booking hører til en bestemt sal, så salen må ikke skiftes, når der er solgt billetter
        boolean changesAuditorium = showing.getAuditorium().getId() != request.auditoriumId();
        if (changesAuditorium && bookingSeatRepository.countByShowingId(showingId) > 0) {
            throw new IllegalStateException("Forestillingen har bookinger og kan ikke flyttes til en anden sal");
        }
        applyRequest(showing, request);
        return toResponse(showingRepository.save(showing));
    }

    // Aflys: forestillingen bliver i databasen (bookingerne peger på den), men kan ikke længere bookes
    @Transactional
    public ShowingResponse cancelShowing(int showingId) {
        Showing showing = findShowing(showingId);
        showing.setStatus(ShowingStatus.CANCELLED);
        return toResponse(showingRepository.save(showing));
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
        if (!auditorium.isActive()) {
            throw new IllegalStateException("Salen er lukket");
        }
        if (request.startTime() == null || request.startTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Starttidspunktet skal ligge i fremtiden");
        }
        if (request.startTime().isAfter(LocalDateTime.now().plusMonths(MONTHS_AHEAD))) {
            throw new IllegalArgumentException("Programmet kan højst planlægges " + MONTHS_AHEAD + " måneder frem");
        }
        // En film kan ikke vises før sin premiere. Billetterne må gerne sælges før (forsalg)
        if (movie.getPremiereDate() != null && request.startTime().toLocalDate().isBefore(movie.getPremiereDate())) {
            throw new IllegalArgumentException("Filmen har først premiere " + movie.getPremiereDate());
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
        LocalDateTime end = endTime(start, movie).plusMinutes(CLEANING_MINUTES);

        for (Showing other : showingRepository.findByAuditoriumIdAndStatus(auditoriumId, ShowingStatus.ACTIVE)) {
            if (other.getId() == showingId) {
                continue; // det er den forestilling vi er ved at rette
            }
            LocalDateTime otherStart = other.getStartTime();
            LocalDateTime otherEnd = endTime(otherStart, other.getMovie()).plusMinutes(CLEANING_MINUTES);

            // To perioder overlapper, hvis hver af dem starter, før den anden slutter
            if (start.isBefore(otherEnd) && otherStart.isBefore(end)) {
                throw new IllegalStateException("Salen er optaget af " + other.getMovie().getName()
                        + " fra " + otherStart + " til " + otherEnd + " (inkl. rengøring)");
            }
        }
    }

    // runTime er i sekunder (se Movie)
    private LocalDateTime endTime(LocalDateTime start, Movie movie) {
        return start.plusSeconds(movie.getRunTime());
    }

    private Showing findShowing(int showingId) {
        return showingRepository.findById(showingId)
                .orElseThrow(() -> new NotFoundException("Forestilling findes ikke: " + showingId));
    }

    private ShowingResponse toResponse(Showing showing) {
        Movie movie = showing.getMovie();
        Auditorium auditorium = showing.getAuditorium();
        int capacity = auditorium.getCapacity();
        int booked = (int) bookingSeatRepository.countByShowingId(showing.getId());

        return new ShowingResponse(showing.getId(), movie.getId(), movie.getName(), movie.getGenre(),
                movie.getAgeRestriction(), movie.getRunTime(), auditorium.getId(),
                auditorium.getAuditoriumName(), showing.getStartTime(), endTime(showing.getStartTime(), movie),
                showing.getStatus(), capacity, capacity - booked);
    }
}
