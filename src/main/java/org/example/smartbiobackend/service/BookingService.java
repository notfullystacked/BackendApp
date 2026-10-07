package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.BookingResponse;
import org.example.smartbiobackend.model.dto.SeatTicket;
import org.example.smartbiobackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (request.seats() == null || request.seats().isEmpty()) {
            throw new IllegalArgumentException("Vælg mindst ét sæde");
        }

        Showing showing = showingRepository.findById(request.showingId())
                .orElseThrow(() -> new IllegalArgumentException("Forestilling findes ikke: " + request.showingId()));

        if (showing.getStatus() == ShowingStatus.CANCELLED) {
            throw new IllegalStateException("Forestillingen er aflyst");
        }
        if (showing.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Forestillingen er allerede startet");
        }

        Booking booking = new Booking();
        booking.setShowing(showing);
        setCustomer(booking, request);

        Set<Integer> chosenSeatIds = new HashSet<>();
        for (SeatTicket seatTicket : request.seats()) {
            if (!chosenSeatIds.add(seatTicket.seatId())) {
                throw new IllegalArgumentException("Samme sæde er valgt to gange: " + seatTicket.seatId());
            }

            Seat seat = seatRepository.findById(seatTicket.seatId())
                    .orElseThrow(() -> new IllegalArgumentException("Sæde findes ikke: " + seatTicket.seatId()));

            if (seat.getAuditorium().getId() != showing.getAuditorium().getId()) {
                throw new IllegalArgumentException("Sæde " + seat.getSeatCode() + " er ikke i salen for denne forestilling");
            }
            if (bookingSeatRepository.existsByShowingIdAndSeatId(showing.getId(), seat.getId())) {
                throw new IllegalStateException("Sæde " + seat.getSeatCode() + " er allerede booket");
            }

            TicketType ticketType = ticketTypeRepository.findById(seatTicket.ticketTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Billettype findes ikke: " + seatTicket.ticketTypeId()));

            booking.getBookingSeats().add(new BookingSeat(booking, seat, ticketType));
        }

        // cascade = ALL på bookingSeats, så sæderne gemmes sammen med bookingen
        Booking saved = bookingRepository.save(booking);

        List<String> seatCodes = saved.getBookingSeats().stream()
                .map(bookingSeat -> bookingSeat.getSeat().getSeatCode())
                .toList();

        return new BookingResponse(saved.getId(), seatCodes, saved.getCustomerEmail(), LocalDateTime.now());
    }

    // Registreret kunde: navn og email fra User. Gæst (eller ekspedient der booker over telefonen): fra requesten
    private void setCustomer(Booking booking, BookingRequest request) {
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("Bruger findes ikke: " + request.userId()));
            booking.setUser(user);
            booking.setCustomerName(user.getName());
            booking.setCustomerEmail(user.getEmail());
        } else {
            if (request.guestName() == null || request.guestName().isBlank()) {
                throw new IllegalArgumentException("Navn er påkrævet ved gæstebooking");
            }
            if (request.guestMail() == null || request.guestMail().isBlank()) {
                throw new IllegalArgumentException("Email er påkrævet ved gæstebooking");
            }
            booking.setCustomerName(request.guestName());
            booking.setCustomerEmail(request.guestMail());
        }
    }
}
