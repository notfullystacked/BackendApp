package org.example.smartbiobackend.servicetest;

import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.BookingResponse;
import org.example.smartbiobackend.repository.*;
import org.example.smartbiobackend.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        // Det processBooking slaar op i alle tre tests: forestilling 1, saede 1c, billettype 2
        when(showingRepository.findById(1)).thenReturn(Optional.of(new Showing()));
        when(seatRepository.findBySeatCode(eq("1c"), anyInt())).thenReturn(Optional.of(new Seat("1c")));
        when(ticketTypeRepository.findById(2)).thenReturn(Optional.of(new TicketType("Child", 80)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // Henter den Booking der blev sendt til bookingRepository.save(...)
    private Booking savedBooking() {
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void guestBookingSavesGuestNameAndEmail() {
        BookingRequest request = new BookingRequest("1c", null, "David", "david@email.dk", 1, 2);

        bookingService.processBooking(request);

        Booking saved = savedBooking();
        assertEquals("David", saved.getCustomerName());
        assertEquals("david@email.dk", saved.getCustomerEmail());
    }

    @Test
    void userBookingSavesUsersNameAndEmail() {
        User user = new User("Lucas", "lucas@mail.dk", LocalDate.of(2000, 1, 1));
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        BookingRequest request = new BookingRequest("1c", 5, null, null, 1, 2);

        bookingService.processBooking(request);

        Booking saved = savedBooking();
        assertEquals("Lucas", saved.getCustomerName());
        assertEquals("lucas@mail.dk", saved.getCustomerEmail());
    }

    @Test
    void userBookingResponseContainsUsersEmail() {
        User user = new User("Lucas", "lucas@mail.dk", LocalDate.of(2000, 1, 1));
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        BookingRequest request = new BookingRequest("1c", 5, null, null, 1, 2);

        BookingResponse response = bookingService.processBooking(request);

        assertEquals("lucas@mail.dk", response.recipientEmail());
    }
}
