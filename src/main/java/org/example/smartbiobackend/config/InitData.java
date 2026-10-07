package org.example.smartbiobackend.config;

import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.SeatTicket;
import org.example.smartbiobackend.repository.*;
import org.example.smartbiobackend.service.AuditoriumService;
import org.example.smartbiobackend.service.BookingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class InitData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ShowingRepository showingRepository;
    private final SeatRepository seatRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final AuditoriumService auditoriumService;
    private final BookingService bookingService;

    public InitData(UserRepository userRepository, MovieRepository movieRepository,
                    ShowingRepository showingRepository, SeatRepository seatRepository,
                    TicketTypeRepository ticketTypeRepository, AuditoriumService auditoriumService,
                    BookingService bookingService) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.showingRepository = showingRepository;
        this.seatRepository = seatRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.auditoriumService = auditoriumService;
        this.bookingService = bookingService;
    }

    @Override
    public void run(String... args) {
        User david = userRepository.save(new User("David", "mail@mail.dk", LocalDate.of(1995, 5, 1)));

        // Sæderne oprettes automatisk ud fra rækker × sæder pr. række
        Auditorium sal1 = auditoriumService.createAuditorium("Sal 1", 20, 12);
        Auditorium sal2 = auditoriumService.createAuditorium("Sal 2", 25, 16);

        // runTime er i sekunder
        Movie jaws = new Movie("Jaws", 124 * 60, "Shark movie", "Steven Spielberg", 1975,
                LocalDate.of(1975, 6, 20), 15);
        jaws.setGenre(Genre.HORROR);

        Movie notebook = new Movie("The Notebook", 123 * 60, "Love story", "Nick Cassavetes", 2004,
                LocalDate.of(2004, 6, 25), 11);
        notebook.setGenre(Genre.ROMANCE);

        Movie madMax = new Movie("Mad Max: Fury Road", 120 * 60, "Desert chase", "George Miller", 2015,
                LocalDate.of(2015, 5, 15), 15);
        madMax.setGenre(Genre.ACTION);

        movieRepository.saveAll(List.of(jaws, notebook, madMax));

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        showingRepository.save(new Showing(jaws, sal1, tomorrow.atTime(17, 0)));
        Showing jawsEvening = showingRepository.save(new Showing(jaws, sal1, tomorrow.atTime(20, 0)));
        showingRepository.save(new Showing(notebook, sal2, tomorrow.atTime(19, 0)));
        showingRepository.save(new Showing(madMax, sal2, tomorrow.plusDays(1).atTime(21, 0)));

        TicketType adult = ticketTypeRepository.save(new TicketType("Voksen", 120));
        ticketTypeRepository.save(new TicketType("Barn", 80));
        ticketTypeRepository.save(new TicketType("Pensionist", 95));

        // Én testbooking: række 1, sæde 1 til Jaws kl. 20
        Seat firstSeat = seatRepository.findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(sal1.getId()).get(0);
        bookingService.processBooking(new BookingRequest(david.getId(), null, null, jawsEvening.getId(),
                List.of(new SeatTicket(firstSeat.getId(), adult.getId()))));
    }
}
