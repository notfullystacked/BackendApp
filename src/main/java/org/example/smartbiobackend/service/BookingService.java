package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.BookingResponse;
import org.example.smartbiobackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ShowingRepository showingRepository;
    private final TicketTypeRepository ticketTypeRepository; //Lucas added
    private final BookingSeatRepository bookingSeatRepository; //Lucas added

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
        // 1. Fetch referenced entities
        Showing showing = showingRepository.findById(request.showingId()).orElseThrow(() -> new IllegalArgumentException("Showing not found"));

        Seat seat = seatRepository.findBySeatCode(request.seatCode(), showing.getId())
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + request.seatCode()));

        //New: Fetch the chosen ticket
        TicketType ticketType = ticketTypeRepository.findById(request.ticketTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket Type not found: " + request.ticketTypeId()));

        // Fill out the booking information
        Booking booking = new Booking();
        booking.setSeat(seat);
        booking.setShowing(showing);

        // 2. Initialize email variable here. This is so that we can reassign it with the guest email
        // If the user is registered we instead just grab their info from the repo and set that user on the booking.
        String recipientEmail = "";

        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));
            booking.setUser(user);
            booking.setCustomerName(user.getName());   // Lucas: Booking kraever navn og email
            booking.setCustomerEmail(user.getEmail());
            recipientEmail = user.getEmail();
        } else {
            // Guest booking: do not attach a User entity
            if (request.guestMail() == null || request.guestMail().isBlank()) {
                throw new IllegalArgumentException("Guest email is required for unregistered bookings.");
            }
            booking.setCustomerName(request.guestName());   // Lucas: gem gaestens navn og email
            booking.setCustomerEmail(request.guestMail());
            recipientEmail = request.guestMail();
        }

        // 3. Save booking to DB (with the customer's name and email)
        Booking savedBooking = bookingRepository.save(booking);

        //  4. Save the seat with its ticket type, so the price can be calculated
        bookingSeatRepository.save(new BookingSeat(savedBooking, seat, ticketType));

          // 5. Construct JSON response
        return new BookingResponse(
                savedBooking.getId(),
                seat.getSeatCode(),
                recipientEmail,
                // Set the booking time here
                LocalDateTime.now()
        );
    }
}
