package org.example.smartbiobackend.servicetest;

import org.example.smartbiobackend.model.Booking;
import org.example.smartbiobackend.repository.BookingRepository;
import org.example.smartbiobackend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(bookingRepository);
    }

    @Test
    void payingBookingMarkItsPaid() {
        Booking booking = new Booking();
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        paymentService.payForBooking(1);

        assertTrue(booking.isPaid());
        verify(bookingRepository).save(booking);
    }

    @Test
    void payingBookingDoesNotExist_IsRejected() {
        when(bookingRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.payForBooking(99));

        assertEquals("Booking not found: 99", exception.getMessage());
    }

    @Test
    void payingBookingAlready_IsPaid() {
        Booking booking = new Booking();
        booking.setPaid(true);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> paymentService.payForBooking(1));

        assertEquals("Booking is already paid: 1", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }
}
