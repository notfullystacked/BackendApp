package org.example.smartbiobackend.service;

import org.example.smartbiobackend.dto.SeatOverviewDto;
import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.BookingResponse;
import org.example.smartbiobackend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ShowingRepository showingRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          SeatRepository seatRepository,
                          ShowingRepository showingRepository,
                          TicketTypeRepository ticketTypeRepository,
                          BookingSeatRepository bookingSeatRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.showingRepository = showingRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    @Transactional
    public BookingResponse processBooking(BookingRequest request) {

        Showing showing = showingRepository.findById(request.showingId())
                .orElseThrow(() -> new IllegalArgumentException("Showing not found"));

        Seat seat = seatRepository.findBySeatCode(request.seatCode(), showing.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seat not found: " + request.seatCode()));

        TicketType ticketType = ticketTypeRepository.findById(request.ticketTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ticket Type not found: " + request.ticketTypeId()));

        // Check if the seat is already booked for this showing
        if (bookingSeatRepository.existsBySeatIdAndBookingShowingId(
                seat.getId(), showing.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat " + seat.getSeatCode()
                            + " is already booked for this showing."
            );
        }

        // Fill out the booking information
        Booking booking = new Booking();
        booking.setSeat(seat);
        booking.setShowing(showing);

        String recipientEmail = "";

        if (request.userId() != null) {

            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User not found: " + request.userId()));

            booking.setUser(user);
            booking.setCustomerName(user.getName());
            booking.setCustomerEmail(user.getEmail());
            recipientEmail = user.getEmail();

        } else {

            if (request.guestMail() == null || request.guestMail().isBlank()) {
                throw new IllegalArgumentException(
                        "Guest email is required for unregistered bookings.");
            }

            booking.setCustomerName(request.guestName());
            booking.setCustomerEmail(request.guestMail());
            recipientEmail = request.guestMail();
        }

        Booking savedBooking = bookingRepository.save(booking);

        bookingSeatRepository.save(
                new BookingSeat(savedBooking, seat, ticketType)
        );

        return new BookingResponse(
                savedBooking.getId(),
                seat.getSeatCode(),
                recipientEmail,
                LocalDateTime.now()
        );
    }

    // Seat overview for a specific showing
    public List<SeatOverviewDto> getSeatOverview(int showingId) {

        Showing showing = showingRepository.findById(showingId)
                .orElseThrow(() -> new IllegalArgumentException("Showing not found"));

        int auditoriumId = showing.getAuditorium().getId();

        List<Seat> allSeats = seatRepository.findByAuditoriumId(auditoriumId);

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingShowingId(showingId);

        return allSeats.stream()
                .map(seat -> new SeatOverviewDto(
                        seat.getSeatCode(),
                        bookingSeats.stream()
                                .anyMatch(bookingSeat ->
                                        bookingSeat.getSeat().getId() == seat.getId())
                ))
                .toList();
    }
}