package org.example.smartbiobackend.config;


import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.example.smartbiobackend.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Configuration
public class InitData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final MovieRepository movieRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;
    private final ShowingRepository showingRepository;
    private final RoleRepository roleRepository;
    private final BookingRepository bookingRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public InitData(UserRepository userRepository, MovieRepository movieRepository, AuditoriumRepository auditoriumRepository, SeatRepository seatRepository, ShowingRepository showingRepository, RoleRepository roleRepository, BookingRepository bookingRepository, TicketTypeRepository ticketTypeRepository, BookingSeatRepository bookingSeatRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.seatRepository = seatRepository;
        this.showingRepository = showingRepository;
        this.roleRepository = roleRepository;
        this.bookingRepository = bookingRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
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
        // Create employee role
        Role employeeRole = new Role("EMPLOYEE");
        roleRepository.save(employeeRole);

        Employee employee = new Employee();

        employee.setName("Test Employee");
        employee.setUsername("employee");
        employee.setEmail("employee@kino.dk");
        employee.setBirthday(LocalDate.of(1995, 1, 1));
        employee.setPassword(
                passwordEncoder.encode("password123")
        );

        employee.getRoles().add(employeeRole);

        employeeRepository.save(employee);

        User user = new User("David", "mail@mail.dk", LocalDate.now());
        userRepository.save(user);

        Movie movie = new Movie("Jaws", 2000,
                "Shark movie",
                "Steven Spielberg", 1975,
                LocalDate.of(1975, 6, 20), 18,
                "Horror");

        movieRepository.save(movie);
        Auditorium auditorium = new Auditorium(
                "Horror Auditorium",
                5,
                10
        );

        auditoriumRepository.save(auditorium);
        Showing showing = new Showing();
        showing.setAuditorium(auditorium);
        showing.setMovie(movie);
        showing.setStartTime(LocalDateTime.now());
        showing.setDate(LocalDate.now());
        showingRepository.save(showing);

        Seat seat = new Seat(auditorium, 1, 2);
        seatRepository.save(seat);

        Seat freeSeat = new Seat(auditorium, 1, 3);
        seatRepository.save(freeSeat);

        TicketType adult = new TicketType("Adult", 120);   // NY
        TicketType child = new TicketType("Child", 80);    // NY
        ticketTypeRepository.save(adult);                  // NY
        ticketTypeRepository.save(child);

        Booking booking = new Booking(showing, user, seat);
        booking.setCustomerName(user.getName());
        booking.setCustomerEmail(user.getEmail());
        bookingRepository.save(booking);

        bookingSeatRepository.save(new BookingSeat(booking, seat, adult));

        System.out.println(booking);
    }
}
