package org.example.smartbiobackend.servicetest;

import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.BookingResponse;
import org.example.smartbiobackend.model.dto.SeatTicket;
import org.example.smartbiobackend.repository.*;
import org.example.smartbiobackend.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private ShowingRepository showingRepository;
    @Mock private TicketTypeRepository ticketTypeRepository;
    @Mock private BookingSeatRepository bookingSeatRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository, userRepository, seatRepository,
                showingRepository, ticketTypeRepository, bookingSeatRepository);

        Auditorium sal = new Auditorium("Sal 1", 20, 12);
        Showing showing = new Showing(new Movie("Jaws"), sal, LocalDateTime.now().plusDays(1));

        // Forestilling 1, sæde 7 (række 1, sæde 3), billettype 2
        lenient().when(showingRepository.findById(1)).thenReturn(Optional.of(showing));
        lenient().when(seatRepository.findById(7)).thenReturn(Optional.of(new Seat(sal, 1, 3)));
        lenient().when(ticketTypeRepository.findById(2)).thenReturn(Optional.of(new TicketType("Child", 80)));
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private BookingRequest guestRequest() {
        return new BookingRequest(null, "David", "david@email.dk", 1, List.of(new SeatTicket(7, 2)));
    }

    private BookingRequest userRequest() {
        return new BookingRequest(5, null, null, 1, List.of(new SeatTicket(7, 2)));
    }

    // Henter den Booking der blev sendt til bookingRepository.save(...)
    private Booking savedBooking() {
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void guestBookingSavesGuestNameAndEmail() {
        bookingService.processBooking(guestRequest());

        Booking saved = savedBooking();
        assertEquals("David", saved.getCustomerName());
        assertEquals("david@email.dk", saved.getCustomerEmail());
    }

    @Test
    void userBookingSavesUsersNameAndEmail() {
        when(userRepository.findById(5)).thenReturn(Optional.of(new User("Lucas", "lucas@mail.dk", LocalDate.of(2000, 1, 1))));

        bookingService.processBooking(userRequest());

        Booking saved = savedBooking();
        assertEquals("Lucas", saved.getCustomerName());
        assertEquals("lucas@mail.dk", saved.getCustomerEmail());
    }

    @Test
    void userBookingResponseContainsUsersEmailAndSeatCode() {
        when(userRepository.findById(5)).thenReturn(Optional.of(new User("Lucas", "lucas@mail.dk", LocalDate.of(2000, 1, 1))));

        BookingResponse response = bookingService.processBooking(userRequest());

        assertEquals("lucas@mail.dk", response.recipientEmail());
        assertEquals(List.of("1-3"), response.seatCodes());
    }

    @Test
    void alreadyBookedSeatIsRejected() {
        when(bookingSeatRepository.existsByShowingIdAndSeatId(anyInt(), anyInt())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> bookingService.processBooking(guestRequest()));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void sameSeatTwiceInOneBookingIsRejected() {
        BookingRequest request = new BookingRequest(null, "David", "david@email.dk", 1,
                List.of(new SeatTicket(7, 2), new SeatTicket(7, 2)));

        assertThrows(IllegalArgumentException.class, () -> bookingService.processBooking(request));
    }
}
