package org.example.smartbiobackend.config;

import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.SeatTicket;
import org.example.smartbiobackend.repository.*;
import org.example.smartbiobackend.service.AuditoriumService;
import org.example.smartbiobackend.service.BookingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Testdata. Kører én gang, når applikationen starter (CommandLineRunner).
// Alt ligger her i stedet for i data.sql, så det virker ens på H2 og MySQL, og så adgangskoder kan hashes i koden.
//
// Id'er (bruges i config/httprequests.http):
//   Sale:           1 = Sal 1 (20 x 12, sæde-id 1-240), 2 = Sal 2 (25 x 16, sæde-id 241-640)
//   Film:           1 = Jaws, 2 = The Notebook, 3 = Mad Max (premierefilm), 4 = Spirited Away og 5 = Alien (coming soon)
//   Forestillinger: 1 = Jaws kl. 17, 2 = Jaws kl. 20, 3 = The Notebook, 4 = Mad Max
//   Billettyper:    1 = Voksen 120, 2 = Barn 80, 3 = Pensionist 95
//   Booking 1:      David, række 1 sæde 1, forestilling 2
//   Medarbejdere:   mads (Admin + Clerk), sofie (Clerk), oliver (Operator + MovieEditor), emma (Inspector). Kode: kode123
@Component
public class InitData implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "kode123";

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ShowingRepository showingRepository;
    private final SeatRepository seatRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditoriumService auditoriumService;
    private final BookingService bookingService;
    private final PasswordEncoder passwordEncoder;

    public InitData(UserRepository userRepository, MovieRepository movieRepository,
                    ShowingRepository showingRepository, SeatRepository seatRepository,
                    TicketTypeRepository ticketTypeRepository, RoleRepository roleRepository,
                    EmployeeRepository employeeRepository, AuditoriumService auditoriumService,
                    BookingService bookingService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.showingRepository = showingRepository;
        this.seatRepository = seatRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.auditoriumService = auditoriumService;
        this.bookingService = bookingService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Er der allerede data (fx en MySQL-database der ikke bliver nulstillet), opretter vi ikke det hele én gang til
        if (movieRepository.count() > 0 || employeeRepository.count() > 0) {
            return;
        }

        createEmployees();

        User david = userRepository.save(new User("David", "mail@mail.dk", LocalDate.of(1995, 5, 1)));

        // Sæderne oprettes automatisk ud fra rækker × sæder pr. række
        Auditorium sal1 = auditoriumService.createAuditorium("Sal 1", 20, 12);
        Auditorium sal2 = auditoriumService.createAuditorium("Sal 2", 25, 16);

        LocalDate today = LocalDate.now();

        // runTime er i sekunder. premiereDate afgør status: gammel = NOW_SHOWING, ny = PREMIERE, fremtid = COMING_SOON
        Movie jaws = movie("Jaws", 124, "En kæmpe hvidhaj spreder skræk i en lille badeby, og tre mænd sejler ud for at fange den.",
                "Steven Spielberg", 1975, LocalDate.of(1975, 6, 20), 15, Genre.HORROR, 8.1, today.minusDays(60));
        Movie notebook = movie("The Notebook", 123, "En ældre mand læser en kærlighedshistorie højt fra en gammel notesbog.",
                "Nick Cassavetes", 2004, LocalDate.of(2004, 6, 25), 11, Genre.ROMANCE, 7.8, today.minusDays(40));
        Movie madMax = movie("Mad Max: Fury Road", 120, "En vild flugt gennem ørkenen i en verden, hvor vand og benzin er det mest værdifulde.",
                "George Miller", 2015, LocalDate.of(2015, 5, 15), 15, Genre.ACTION, 8.1, today.minusDays(3));
        madMax.setPromoted(true);
        Movie spirited = movie("Spirited Away", 125, "En pige farer vild i en åndeverden og må arbejde i et badehus for at redde sine forældre.",
                "Hayao Miyazaki", 2001, LocalDate.of(2001, 7, 20), 7, Genre.ANIMATION, 8.6, today.plusDays(10));
        spirited.setPromoted(true);
        Movie alien = movie("Alien", 117, "Besætningen på et rumskib opdager, at de har fået en ubuden gæst om bord.",
                "Ridley Scott", 1979, LocalDate.of(1979, 5, 25), 15, Genre.SCIFI, 8.5, today.plusDays(25));
        movieRepository.saveAll(List.of(jaws, notebook, madMax, spirited, alien));

        // De fire første har faste id'er, som httprequests.http bruger
        LocalDate tomorrow = today.plusDays(1);
        showingRepository.save(new Showing(jaws, sal1, tomorrow.atTime(17, 0)));
        Showing jawsEvening = showingRepository.save(new Showing(jaws, sal1, tomorrow.atTime(20, 0)));
        showingRepository.save(new Showing(notebook, sal2, tomorrow.atTime(19, 0)));
        showingRepository.save(new Showing(madMax, sal2, tomorrow.plusDays(1).atTime(21, 0)));

        // Resten af ugen. Tiderne er valgt, så der ikke er overlap i samme sal (spilletid + 15 min. rengøring)
        for (int day = 2; day <= 7; day++) {
            LocalDate date = today.plusDays(day);
            showingRepository.save(new Showing(jaws, sal1, date.atTime(17, 0)));
            showingRepository.save(new Showing(madMax, sal1, date.atTime(20, 0)));
            showingRepository.save(new Showing(notebook, sal2, date.atTime(18, 0)));
        }

        // Forsalg til de kommende film: første forestilling ligger på premieredagen
        showingRepository.save(new Showing(spirited, sal2, spirited.getPremiereDate().atTime(19, 0)));
        showingRepository.save(new Showing(alien, sal1, alien.getPremiereDate().atTime(20, 0)));

        TicketType adult = ticketTypeRepository.save(new TicketType("Voksen", 120));
        ticketTypeRepository.save(new TicketType("Barn", 80));
        ticketTypeRepository.save(new TicketType("Pensionist", 95));

        // Én testbooking: række 1, sæde 1 til Jaws kl. 20
        Seat firstSeat = seatRepository.findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(sal1.getId()).get(0);
        bookingService.processBooking(new BookingRequest(david.getId(), null, null, jawsEvening.getId(),
                List.of(new SeatTicket(firstSeat.getId(), adult.getId()))));
    }

    // Roller og medarbejdere. Rækkefølgen giver rollerne id 1-6
    private void createEmployees() {
        Role movieEditor = roleRepository.save(new Role("MovieEditor"));
        Role admin = roleRepository.save(new Role("Admin"));
        Role clerk = roleRepository.save(new Role("Clerk"));
        roleRepository.save(new Role("Guest"));
        Role operator = roleRepository.save(new Role("Operator"));
        Role inspector = roleRepository.save(new Role("Inspector"));

        employeeRepository.save(employee("Mads", "mads", "mads@kino.dk", Set.of(admin, clerk)));
        employeeRepository.save(employee("Sofie", "sofie", "sofie@kino.dk", Set.of(clerk)));
        employeeRepository.save(employee("Oliver", "oliver", "oliver@kino.dk", Set.of(operator, movieEditor)));
        employeeRepository.save(employee("Emma", "emma", "emma@kino.dk", Set.of(inspector)));
    }

    private Employee employee(String name, String username, String email, Set<Role> roles) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setUsername(username);
        employee.setEmail(email);
        employee.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        employee.setRoles(new HashSet<>(roles));
        return employee;
    }

    private Movie movie(String name, int minutes, String description, String director, int releaseYear,
                        LocalDate releaseDate, int ageRestriction, Genre genre, double imdbRating,
                        LocalDate premiereDate) {
        Movie movie = new Movie(name, minutes * 60, description, director, releaseYear, releaseDate, ageRestriction);
        movie.setGenre(genre);
        movie.setImdbRating(imdbRating);
        movie.setPremiereDate(premiereDate);
        return movie;
    }
}
