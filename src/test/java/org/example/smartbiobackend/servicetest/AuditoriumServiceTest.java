package org.example.smartbiobackend.servicetest;

import org.example.smartbiobackend.exception.NotFoundException;
import org.example.smartbiobackend.model.Auditorium;
import org.example.smartbiobackend.model.CleaningStatus;
import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.model.Seat;
import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.ShowingStatus;
import org.example.smartbiobackend.repository.AuditoriumRepository;
import org.example.smartbiobackend.repository.BookingSeatRepository;
import org.example.smartbiobackend.repository.SeatRepository;
import org.example.smartbiobackend.repository.ShowingRepository;
import org.example.smartbiobackend.service.AuditoriumService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriumServiceTest {

    @Mock private AuditoriumRepository auditoriumRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private ShowingRepository showingRepository;
    @Mock private BookingSeatRepository bookingSeatRepository;

    private AuditoriumService auditoriumService;

    @BeforeEach
    void setUp() {
        auditoriumService = new AuditoriumService(auditoriumRepository, seatRepository,
                showingRepository, bookingSeatRepository);
        lenient().when(auditoriumRepository.save(any(Auditorium.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // Henter den liste af sæder, der blev sendt til seatRepository.saveAll(...)
    @SuppressWarnings("unchecked")
    private List<Seat> savedSeats() {
        ArgumentCaptor<List<Seat>> captor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    // Alle sæder i en sal på rows x seatsPerRow, med id 1, 2, 3 ...
    private List<Seat> seatsFor(Auditorium auditorium) {
        List<Seat> seats = new ArrayList<>();
        int id = 1;
        for (int row = 1; row <= auditorium.getRowCount(); row++) {
            for (int number = 1; number <= auditorium.getSeatsPerRow(); number++) {
                Seat seat = new Seat(auditorium, row, number);
                seat.setId(id++);
                seats.add(seat);
            }
        }
        return seats;
    }

    // ---------- ISSUE-9: kapacitet ----------

    @Test
    void capacityIsRowsTimesSeatsPerRow() {
        assertEquals(240, new Auditorium("Lille sal", 20, 12).getCapacity());
        assertEquals(400, new Auditorium("Stor sal", 25, 16).getCapacity());
    }

    @Test
    void createAuditoriumCreatesOneSeatForEachPlace() {
        Auditorium auditorium = auditoriumService.createAuditorium("Sal 3", 3, 4);

        List<Seat> seats = savedSeats();
        assertEquals(12, seats.size());
        assertEquals(auditorium.getCapacity(), seats.size());
        assertEquals("1-1", seats.get(0).getSeatCode());
        assertEquals("3-4", seats.get(11).getSeatCode());
    }

    @Test
    void createAuditoriumWithoutName_IsRejected() {
        assertThrows(IllegalArgumentException.class, () -> auditoriumService.createAuditorium("", 3, 4));
    }

    @Test
    void createAuditoriumWithZeroRows_IsRejected() {
        assertThrows(IllegalArgumentException.class, () -> auditoriumService.createAuditorium("Sal 3", 0, 4));
    }

    // ---------- Ret størrelse ----------

    @Test
    void makingAuditoriumBiggerAddsOnlyTheMissingSeats() {
        Auditorium auditorium = new Auditorium("Sal 3", 2, 2);
        when(auditoriumRepository.findById(1)).thenReturn(Optional.of(auditorium));
        when(seatRepository.findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(1)).thenReturn(seatsFor(auditorium));

        Auditorium updated = auditoriumService.updateAuditorium(1, "Sal 3", 3, 2);

        assertEquals(6, updated.getCapacity());
        assertEquals(List.of("3-1", "3-2"), savedSeats().stream().map(Seat::getSeatCode).toList());
    }

    @Test
    void makingAuditoriumSmallerRemovesSeatsOutsideTheNewSize() {
        Auditorium auditorium = new Auditorium("Sal 3", 2, 2);
        when(auditoriumRepository.findById(1)).thenReturn(Optional.of(auditorium));
        when(seatRepository.findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(1)).thenReturn(seatsFor(auditorium));
        when(bookingSeatRepository.existsBySeatId(anyInt())).thenReturn(false);

        Auditorium updated = auditoriumService.updateAuditorium(1, "Sal 3", 1, 2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Seat>> removed = ArgumentCaptor.forClass(List.class);
        verify(seatRepository).deleteAll(removed.capture());
        assertEquals(List.of("2-1", "2-2"), removed.getValue().stream().map(Seat::getSeatCode).toList());
        assertEquals(2, updated.getCapacity());
    }

    @Test
    void makingAuditoriumSmaller_IsRejectedWhenARemovedSeatHasBookings() {
        Auditorium auditorium = new Auditorium("Sal 3", 2, 2);
        when(auditoriumRepository.findById(1)).thenReturn(Optional.of(auditorium));
        when(seatRepository.findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(1)).thenReturn(seatsFor(auditorium));
        when(bookingSeatRepository.existsBySeatId(anyInt())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> auditoriumService.updateAuditorium(1, "Sal 3", 1, 2));
        verify(auditoriumRepository, never()).save(any());
    }

    // ---------- Luk sal ----------

    @Test
    void closeAuditoriumWithFutureShowings_IsRejected() {
        Auditorium auditorium = new Auditorium("Sal 1", 20, 12);
        Showing future = new Showing(new Movie("Jaws"), auditorium, LocalDateTime.now().plusDays(1));
        when(auditoriumRepository.findById(1)).thenReturn(Optional.of(auditorium));
        when(showingRepository.findByAuditoriumIdAndStatusAndStartTimeAfter(eq(1), eq(ShowingStatus.ACTIVE), any()))
                .thenReturn(List.of(future));

        assertThrows(IllegalStateException.class, () -> auditoriumService.closeAuditorium(1));
        assertTrue(auditorium.isActive());
    }

    @Test
    void closeAuditoriumWithoutShowings_ClosesIt() {
        Auditorium auditorium = new Auditorium("Sal 1", 20, 12);
        when(auditoriumRepository.findById(1)).thenReturn(Optional.of(auditorium));
        when(showingRepository.findByAuditoriumIdAndStatusAndStartTimeAfter(eq(1), eq(ShowingStatus.ACTIVE), any()))
                .thenReturn(List.of());

        assertFalse(auditoriumService.closeAuditorium(1).isActive());
    }

    // ---------- Rengøring ----------

    @Test
    void newAuditoriumIsClean() {
        assertEquals(CleaningStatus.CLEAN, new Auditorium("Sal 1", 20, 12).getCleaningStatus());
    }

    @Test
    void auditoriumCanBeMarkedAsNeedingCleaningAndAsCleanAgain() {
        Auditorium auditorium = new Auditorium("Sal 1", 20, 12);
        when(auditoriumRepository.findById(1)).thenReturn(Optional.of(auditorium));

        assertEquals(CleaningStatus.NEEDS_CLEANING, auditoriumService.markNeedsCleaning(1).getCleaningStatus());
        assertEquals(CleaningStatus.CLEAN, auditoriumService.markClean(1).getCleaningStatus());
    }

    @Test
    void markCleaningOnAuditoriumThatDoesNotExist_GivesNotFound() {
        when(auditoriumRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> auditoriumService.markNeedsCleaning(99));
    }
}
