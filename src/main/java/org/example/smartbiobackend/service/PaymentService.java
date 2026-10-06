package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.Booking;
import org.example.smartbiobackend.repository.BookingRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;

    public PaymentService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public void payForBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (booking.isPaid()) {
            throw new IllegalStateException("Booking is already paid: " + bookingId);
        }

        booking.setPaid(true);
        bookingRepository.save(booking);
    }

}
