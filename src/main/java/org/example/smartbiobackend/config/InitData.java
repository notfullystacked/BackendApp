package org.example.smartbiobackend.config;


import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class InitData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;
    private final ShowingRepository showingRepository;
    private final RoleRepository roleRepository;
    private final BookingRepository bookingRepository;

    public InitData(UserRepository userRepository, MovieRepository movieRepository, AuditoriumRepository auditoriumRepository, SeatRepository seatRepository, ShowingRepository showingRepository, RoleRepository roleRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.seatRepository = seatRepository;
        this.showingRepository = showingRepository;
        this.roleRepository = roleRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Steps:
        // Create x Users
        // Pick a seat in a booking
        // Verify that the seat is reserved by user
        setupABooking();
    }

    private void setupABooking() {
        User user = new User("David", "mail@mail.dk", LocalDate.now());
        userRepository.save(user);

        Movie movie = new Movie("Jaws", 2000,
                "Shark movie"
                , "Steven Spielberg", 1975,
                LocalDate.of(1975,6,20), 18);

        movieRepository.save(movie);
        Auditorium auditorium = new Auditorium("Thriller Scene");
        auditoriumRepository.save(auditorium);
        Showing showing = new Showing();
        showing.setAuditorium(auditorium);
        showing.setMovie(movie);
        showing.setStartTime(LocalTime.now());
        showing.setDate(LocalDate.now());
        showingRepository.save(showing);

        Seat seat = new Seat(auditorium, "1b");
        seatRepository.save(seat);
        Booking booking = new Booking(showing, user, seat);
        bookingRepository.save(booking);

        System.out.println(booking);
    }
}
