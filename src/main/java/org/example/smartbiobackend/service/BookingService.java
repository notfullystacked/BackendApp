package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.Booking;
import org.example.smartbiobackend.model.Seat;
import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.User;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.BookingResponse;
import org.example.smartbiobackend.repository.BookingRepository;
import org.example.smartbiobackend.repository.SeatRepository;
import org.example.smartbiobackend.repository.ShowingRepository;
import org.example.smartbiobackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ShowingRepository showingRepository;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          SeatRepository seatRepository, ShowingRepository showingRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.showingRepository = showingRepository;
    }
    @Transactional
    public BookingResponse processBooking(BookingRequest request) {
        // 1. Fetch referenced entities
        Showing showing = showingRepository.findById(request.showingId()).orElseThrow(() -> new IllegalArgumentException("Showing not found"));

        Seat seat = seatRepository.findBySeatCode(request.seatCode(), showing.getId())
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + request.seatCode()));

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
        } else {
            // Guest booking: do not attach a User entity
            if (request.guestMail() == null || request.guestMail().isBlank()) {
                throw new IllegalArgumentException("Guest email is required for unregistered bookings.");
            }
            recipientEmail = request.guestMail();
        }

        // 3. Save booking to DB (guest details are NOT stored)
        Booking savedBooking = bookingRepository.save(booking);

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
