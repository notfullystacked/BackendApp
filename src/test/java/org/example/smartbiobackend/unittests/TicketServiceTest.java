package org.example.smartbiobackend.unittests;

import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.TicketDTO;
import org.example.smartbiobackend.repository.BookingRepository;
import org.example.smartbiobackend.service.TicketService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TicketService ticketService;

    private Booking bookingForInception() {
        Movie movie = new Movie("Inception");
        Auditorium auditorium = new Auditorium("Hall 1");
        Showing showing = new Showing(movie, auditorium, LocalDateTime.of(2026, 10, 1, 20, 0));
        return new Booking(showing, "Alice", "alice@example.com");
    }

    @Test
    void getTicket_WhenBookingDoesNotExist_ThrowsNotFound() {
        when(bookingRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicket(99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getTicket_WhenBookingExists_ReturnsMovieTitleFromShowing() {
        Booking booking = bookingForInception();
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        TicketDTO ticket = ticketService.getTicket(1);

        assertThat(ticket.getMovieTitle()).isEqualTo("Inception");
    }

    @Test
    void getTicket_WhenBookingExists_ReturnsTicketWithMatchingBookingId() {
        Booking booking = bookingForInception();
        ReflectionTestUtils.setField(booking, "id", 1);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        TicketDTO ticket = ticketService.getTicket(1);

        assertThat(ticket.getBookingId()).isEqualTo(1);
    }

    @Test
    void getTicket_WhenBookingExists_ReturnsShowingStartTime() {
        Booking booking = bookingForInception();
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        TicketDTO ticket = ticketService.getTicket(1);

        assertThat(ticket.getShowingStart()).isEqualTo(LocalDateTime.of(2026, 10, 1, 20, 0));
    }

    @Test
    void getTicket_WhenBookingExists_ReturnsAuditoriumName() {
        Booking booking = bookingForInception();
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        TicketDTO ticket = ticketService.getTicket(1);

        assertThat(ticket.getAuditoriumName()).isEqualTo("Hall 1");
    }

    @Test
    void getTicket_WhenBookingHasOneSeat_ReturnsSeatCodeInSeatsList() {
        Booking booking = bookingForInception();
        Seat seat = new Seat("A1");
        TicketType ticketType = new TicketType("Adult", 95);
        booking.getBookingSeats().add(new BookingSeat(booking, seat, ticketType));
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        TicketDTO ticket = ticketService.getTicket(1);

        assertThat(ticket.getSeats()).hasSize(1);
        assertThat(ticket.getSeats().get(0).getSeatCode()).isEqualTo("A1");
    }

    @Test
    void getTicket_WhenBookingHasOneSeat_ReturnsTicketTypeAndPrice() {
        Booking booking = bookingForInception();
        Seat seat = new Seat("A1");
        TicketType ticketType = new TicketType("Adult", 95);
        booking.getBookingSeats().add(new BookingSeat(booking, seat, ticketType));
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        TicketDTO ticket = ticketService.getTicket(1);

        assertThat(ticket.getSeats().getFirst().getTicketTypeName()).isEqualTo("Adult");
        assertThat(ticket.getSeats().getFirst().getPrice()).isEqualTo(95);
    }

    @Test
    void getTicket_WhenBookingExists_ReturnsCustomerNameAndEmail() {
        Booking booking = bookingForInception();
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        TicketDTO ticket = ticketService.getTicket(1);

        assertThat(ticket.getCustomerName()).isEqualTo("Alice");
        assertThat(ticket.getCustomerEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void getTicket_WhenBookingHasMultipleSeats_ReturnsSummedTotalPrice() {
        Booking booking = bookingForInception();
        Seat seatA = new Seat("A1");
        Seat seatB = new Seat("A2");
        TicketType adult = new TicketType("Adult", 95);
        TicketType child = new TicketType("Child", 60);
        booking.getBookingSeats().add(new BookingSeat(booking, seatA, adult));
        booking.getBookingSeats().add(new BookingSeat(booking, seatB, child));
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        TicketDTO ticket = ticketService.getTicket(1);

        assertThat(ticket.getTotalPrice()).isEqualTo(155);
    }

    @Test
    void getTicket_WhenBookingExists_ReturnsQrPlaceholderWithBookingId() {
        Booking booking = bookingForInception();
        ReflectionTestUtils.setField(booking, "id", 1);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        TicketDTO ticket = ticketService.getTicket(1);

        assertThat(ticket.getQrPlaceholder()).isEqualTo("TICKET-1");
    }

}