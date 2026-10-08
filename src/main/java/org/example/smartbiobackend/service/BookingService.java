package org.example.smartbiobackend.service;

import org.example.smartbiobackend.dto.SeatOverviewDto;
import org.example.smartbiobackend.exception.ForbiddenException;
import org.example.smartbiobackend.exception.NotFoundException;
import org.example.smartbiobackend.model.*;
import org.example.smartbiobackend.model.dto.BookingDetails;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.BookingResponse;
import org.example.smartbiobackend.model.dto.SeatTicket;
import org.example.smartbiobackend.model.dto.TicketSeatDTO;
import org.example.smartbiobackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    // ---------- Se, annullér og tjek bookinger ind ----------

    @Transactional(readOnly = true)
    public BookingDetails getBooking(int bookingId) {
        return toDetails(findBooking(bookingId));
    }

    // Ekspedientens liste. showingId og email er null, hvis de ikke er sendt med
    @Transactional(readOnly = true)
    public List<BookingDetails> getBookings(Integer showingId, String email) {
        List<Booking> bookings;
        if (showingId != null) {
            bookings = bookingRepository.findByShowingIdOrderByIdDesc(showingId);
        } else if (email != null && !email.isBlank()) {
            bookings = bookingRepository.findByCustomerEmailIgnoreCaseOrderByIdDesc(email.trim());
        } else {
            bookings = bookingRepository.findAllByOrderByIdDesc();
        }
        return bookings.stream().map(this::toDetails).toList();
    }

    // En registreret kundes egne bookinger
    @Transactional(readOnly = true)
    public List<BookingDetails> getBookingsForUser(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Bruger findes ikke: " + userId);
        }
        return bookingRepository.findByUserIdOrderByIdDesc(userId).stream().map(this::toDetails).toList();
    }

    // Annullér: bookingen slettes, og sæderne bliver ledige igen (cascade = ALL sletter BookingSeat-rækkerne).
    // En medarbejder må altid. En kunde skal kunne oplyse den email, bookingen er lavet med
    @Transactional
    public void cancelBooking(int bookingId, String email, boolean isEmployee) {
        Booking booking = findBooking(bookingId);
        if (!isEmployee && !booking.getCustomerEmail().equalsIgnoreCase(email == null ? "" : email.trim())) {
            throw new ForbiddenException("Emailen passer ikke til bookingen");
        }
        if (booking.getShowing().getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Forestillingen er allerede startet, så bookingen kan ikke annulleres");
        }
        bookingRepository.delete(booking);
    }

    // Billetkontrol: markér at kunden er mødt op. Kan kun gøres én gang
    @Transactional
    public BookingDetails checkIn(int bookingId) {
        Booking booking = findBooking(bookingId);
        if (booking.getShowing().getStatus() == ShowingStatus.CANCELLED) {
            throw new IllegalStateException("Forestillingen er aflyst");
        }
        if (!booking.isPaid()) {
            throw new IllegalStateException("Bookingen er ikke betalt");
        }
        if (booking.isCheckedIn()) {
            throw new IllegalStateException("Billetten er allerede brugt");
        }
        booking.setCheckedIn(true);
        return toDetails(bookingRepository.save(booking));
    }

    // Enkel sædeoversigt (sædekode + optaget). Den fulde oversigt med rækker ligger i ShowingService.getSeatMap
    @Transactional(readOnly = true)
    public List<SeatOverviewDto> getSeatOverview(int showingId) {
        Showing showing = showingRepository.findById(showingId)
                .orElseThrow(() -> new NotFoundException("Forestilling findes ikke: " + showingId));

        Set<Integer> bookedSeatIds = bookingSeatRepository.findByShowingId(showingId).stream()
                .map(bookingSeat -> bookingSeat.getSeat().getId())
                .collect(Collectors.toSet());

        return seatRepository.findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(showing.getAuditorium().getId())
                .stream()
                .map(seat -> new SeatOverviewDto(seat.getSeatCode(), bookedSeatIds.contains(seat.getId())))
                .toList();
    }

    private Booking findBooking(int bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking findes ikke: " + bookingId));
    }

    private BookingDetails toDetails(Booking booking) {
        Showing showing = booking.getShowing();

        List<TicketSeatDTO> seats = booking.getBookingSeats().stream()
                .map(bookingSeat -> new TicketSeatDTO(bookingSeat.getSeat().getSeatCode(),
                        bookingSeat.getTicketType().getTicketName(), bookingSeat.getTicketType().getPrice()))
                .toList();
        int totalPrice = seats.stream().mapToInt(TicketSeatDTO::getPrice).sum();

        return new BookingDetails(booking.getId(), showing.getId(), showing.getMovie().getName(),
                showing.getAuditorium().getAuditoriumName(), showing.getStartTime(), showing.getStatus(),
                booking.getCustomerName(), booking.getCustomerEmail(), seats, totalPrice,
                booking.isPaid(), booking.isCheckedIn(), booking.getCreatedAt());
    }
}
