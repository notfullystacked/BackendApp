package org.example.smartbiobackend.servicetest;

import org.example.smartbiobackend.model.BookingSeat;
import org.example.smartbiobackend.model.TicketType;
import org.example.smartbiobackend.repository.BookingSeatRepository;
import org.example.smartbiobackend.service.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriceServiceTest {

    @Mock
    private BookingSeatRepository bookingSeatRepository;

    private PriceService priceService = new PriceService(bookingSeatRepository);

    @BeforeEach
    void setUp() {
        priceService = new PriceService(bookingSeatRepository);
    }

    @Test
    public void totalForOneTicketIsTheTicketPrice() {
        TicketType adult = new TicketType("Voksen", 120);

        int total = priceService.calculateTotal(List.of(adult));

        assertEquals(120, total);
    }

    @Test
    void totalForSeveralTicketsIsTheTotalPrice() {
        TicketType adult  = new TicketType("Voksen", 120);
        TicketType child = new TicketType("Barn", 80);

        int total = priceService.calculateTotal(List.of(adult,adult,child));

        assertEquals(320, total);
    }

    @Test
    void calculatingTotalWithNoTickets_IsRejected() {

        assertThrows(IllegalArgumentException.class, () -> priceService.calculateTotal(List.of()));
    }

    @Test
    void calculatingTotalWithNegativeTickets_IsRejected() {
        TicketType negative = new TicketType("Fejl", -10);

        assertThrows(IllegalArgumentException.class, () -> priceService.calculateTotal(List.of(negative)));
    }

    @Test
    void totalForBookingWithOneSeatIsTheTotalPrice() {
        TicketType adult = new TicketType("Voksen", 120);
        BookingSeat bookingSeat = new BookingSeat(null, null, adult);
        when(bookingSeatRepository.findByBookingId(1)).thenReturn(List.of(bookingSeat));

        int total = priceService.calculateTotalForBooking(1);

        assertEquals(120, total);
    }

    @Test
    void totalForBookingWithSeveralSeatsIsTheTotalPrice() {
        TicketType adult = new TicketType("Voksen", 120);
        TicketType child = new TicketType("Barn", 80);
        BookingSeat bookingSeat1 = new BookingSeat(null, null, adult);
        BookingSeat bookingSeat2 = new BookingSeat(null, null, child);
        when(bookingSeatRepository.findByBookingId(1)).thenReturn(List.of(bookingSeat1, bookingSeat2));

        int total = priceService.calculateTotalForBooking(1);

        assertEquals(200, total);
    }

    @Test
    void totalForBookingThatDoesNotExist_IsRejected() {
        when(bookingSeatRepository.findByBookingId(99)).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> priceService.calculateTotalForBooking(99));

        assertEquals("Booking not found: 99", exception.getMessage());
    }
}
