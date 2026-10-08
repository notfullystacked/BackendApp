package org.example.smartbiobackend.service;

import org.example.smartbiobackend.exception.NotFoundException;
import org.example.smartbiobackend.model.Auditorium;
import org.example.smartbiobackend.model.CleaningStatus;
import org.example.smartbiobackend.model.Seat;
import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.ShowingStatus;
import org.example.smartbiobackend.repository.AuditoriumRepository;
import org.example.smartbiobackend.repository.BookingSeatRepository;
import org.example.smartbiobackend.repository.SeatRepository;
import org.example.smartbiobackend.repository.ShowingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriumService {

    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;
    private final ShowingRepository showingRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public AuditoriumService(AuditoriumRepository auditoriumRepository, SeatRepository seatRepository,
                             ShowingRepository showingRepository, BookingSeatRepository bookingSeatRepository) {
        this.auditoriumRepository = auditoriumRepository;
        this.seatRepository = seatRepository;
        this.showingRepository = showingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    // ISSUE-9: hver sal kommer ud med "capacity" i JSON (se Auditorium.getCapacity)
    public List<Auditorium> getAll() {
        return auditoriumRepository.findAll();
    }

    public Auditorium getAuditorium(int auditoriumId) {
        return auditoriumRepository.findById(auditoriumId)
                .orElseThrow(() -> new NotFoundException("Sal findes ikke: " + auditoriumId));
    }

    public List<Seat> getSeats(int auditoriumId) {
        getAuditorium(auditoriumId); // giver 404, hvis salen ikke findes
        return seatRepository.findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(auditoriumId);
    }

    // Ny sal = nye sæder automatisk. Ingen kodeændringer nødvendige for flere/større sale
    @Transactional
    public Auditorium createAuditorium(String name, int rowCount, int seatsPerRow) {
        validate(name, rowCount, seatsPerRow);

        Auditorium auditorium = auditoriumRepository.save(new Auditorium(name.trim(), rowCount, seatsPerRow));

        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= rowCount; row++) {
            for (int number = 1; number <= seatsPerRow; number++) {
                seats.add(new Seat(auditorium, row, number));
            }
        }
        seatRepository.saveAll(seats);
        return auditorium;
    }

    // Ret navn og/eller størrelse. Nye sæder oprettes, og sæder uden for den nye størrelse fjernes.
    // Et sæde, der har været booket, kan ikke fjernes, for så ville bookingen pege på et sæde, der ikke findes
    @Transactional
    public Auditorium updateAuditorium(int auditoriumId, String name, int rowCount, int seatsPerRow) {
        validate(name, rowCount, seatsPerRow);
        Auditorium auditorium = getAuditorium(auditoriumId);

        List<Seat> existingSeats = seatRepository.findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(auditoriumId);

        List<Seat> seatsToRemove = new ArrayList<>();
        for (Seat seat : existingSeats) {
            if (seat.getSeatRow() > rowCount || seat.getSeatNumber() > seatsPerRow) {
                if (bookingSeatRepository.existsBySeatId(seat.getId())) {
                    throw new IllegalStateException("Sæde " + seat.getSeatCode()
                            + " har bookinger, så salen kan ikke gøres så lille");
                }
                seatsToRemove.add(seat);
            }
        }
        seatRepository.deleteAll(seatsToRemove);

        // De sæder, der mangler: alt uden for den gamle størrelse
        List<Seat> newSeats = new ArrayList<>();
        for (int row = 1; row <= rowCount; row++) {
            for (int number = 1; number <= seatsPerRow; number++) {
                if (row > auditorium.getRowCount() || number > auditorium.getSeatsPerRow()) {
                    newSeats.add(new Seat(auditorium, row, number));
                }
            }
        }
        seatRepository.saveAll(newSeats);

        auditorium.setAuditoriumName(name.trim());
        auditorium.setRowCount(rowCount);
        auditorium.setSeatsPerRow(seatsPerRow);
        return auditoriumRepository.save(auditorium);
    }

    // Luk en sal (fx ved ombygning). Kræver at der ikke er planlagt forestillinger i den
    @Transactional
    public Auditorium closeAuditorium(int auditoriumId) {
        Auditorium auditorium = getAuditorium(auditoriumId);

        List<Showing> futureShowings = showingRepository
                .findByAuditoriumIdAndStatusAndStartTimeAfter(auditoriumId, ShowingStatus.ACTIVE, LocalDateTime.now());
        if (!futureShowings.isEmpty()) {
            throw new IllegalStateException("Salen har " + futureShowings.size()
                    + " kommende forestillinger. Aflys eller flyt dem først");
        }
        auditorium.setActive(false);
        return auditoriumRepository.save(auditorium);
    }

    @Transactional
    public Auditorium openAuditorium(int auditoriumId) {
        Auditorium auditorium = getAuditorium(auditoriumId);
        auditorium.setActive(true);
        return auditoriumRepository.save(auditorium);
    }

    // Rengøring: personalet markerer, at salen skal gøres ren, og bagefter at den er klar igen
    @Transactional
    public Auditorium markNeedsCleaning(int auditoriumId) {
        Auditorium auditorium = getAuditorium(auditoriumId);
        auditorium.setCleaningStatus(CleaningStatus.NEEDS_CLEANING);
        return auditoriumRepository.save(auditorium);
    }

    @Transactional
    public Auditorium markClean(int auditoriumId) {
        Auditorium auditorium = getAuditorium(auditoriumId);
        auditorium.setCleaningStatus(CleaningStatus.CLEAN);
        return auditoriumRepository.save(auditorium);
    }

    private void validate(String name, int rowCount, int seatsPerRow) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Salen skal have et navn");
        }
        if (rowCount <= 0 || seatsPerRow <= 0) {
            throw new IllegalArgumentException("Antal rækker og sæder skal være over 0");
        }
        if (rowCount > 100 || seatsPerRow > 100) {
            throw new IllegalArgumentException("En sal kan højst have 100 rækker og 100 sæder pr. række");
        }
    }
}
