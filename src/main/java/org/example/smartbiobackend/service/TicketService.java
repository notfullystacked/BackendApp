package org.example.smartbiobackend.service;

import org.example.smartbiobackend.dto.TicketDTO;
import org.example.smartbiobackend.dto.TicketSeatDTO;
import org.example.smartbiobackend.model.Booking;
import org.example.smartbiobackend.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final BookingRepository bookingRepository;

    public TicketService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public TicketDTO getTicket(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No booking with id " + bookingId));

        String movieTitle = booking.getShowing().getMovie().getName();
        LocalDateTime showingStart = booking.getShowing().getStartTime();
        String auditoriumName = booking.getShowing().getAuditorium().getName();

        List<TicketSeatDTO> seats = booking.getBookingSeats().stream()
                .map(bookingSeat -> new TicketSeatDTO(
                        bookingSeat.getSeat().getSeatCode(),
                        bookingSeat.getTicketType().getTicketName(),
                        bookingSeat.getTicketType().getPrice()))
                .toList();

        return new TicketDTO(booking.getId(), movieTitle, showingStart, auditoriumName,
                seats, booking.getCustomerName(), booking.getCustomerEmail());
    }
}