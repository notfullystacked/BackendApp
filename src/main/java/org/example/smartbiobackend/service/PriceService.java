package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.BookingSeat;
import org.example.smartbiobackend.model.TicketType;
import org.example.smartbiobackend.repository.BookingSeatRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PriceService {

    private final BookingSeatRepository bookingSeatRepository;

    public PriceService(BookingSeatRepository bookingSeatRepository) {
        this.bookingSeatRepository = bookingSeatRepository;
    }

    public int calculateTotal(List<TicketType> tickets) {
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("Der skal være mindst én billet");
        }
        int total = 0;
        for (TicketType ticket : tickets) {
        if (ticket.getPrice() < 0) {
            throw new IllegalArgumentException("Prisen kan ikke være negativ");
        }
        total += ticket.getPrice();
        }
        return total;
    }

    public int calculateTotalForBooking(int bookingId) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);

        if (bookingSeats.isEmpty()) {
            throw new IllegalArgumentException("Booking findes ikke: " + bookingId);
        }

        List<TicketType> tickets = new ArrayList<>();
        for (BookingSeat bookingSeat : bookingSeats) {
            tickets.add(bookingSeat.getTicketType());

        }
        return calculateTotal(tickets);
    }
}
