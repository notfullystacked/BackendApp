package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.smartbiobackend.dto.SeatOverviewDto;
import org.example.smartbiobackend.model.dto.BookingDetails;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.BookingResponse;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final RoleGuard roleGuard;

    public BookingController(BookingService bookingService, RoleGuard roleGuard) {
        this.bookingService = bookingService;
        this.roleGuard = roleGuard;
    }

    // Åben for alle: kunder booker selv, og ekspedienten booker for kunder i telefonen (med guestName/guestMail)
    @PostMapping("/reserve")
    public ResponseEntity<BookingResponse> reserveSeat(@RequestBody BookingRequest request)  {
        BookingResponse response = bookingService.processBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Medarbejdere: alle bookinger, evt. GET /api/bookings?showingId=2 eller ?email=anna@mail.dk
    @GetMapping
    public List<BookingDetails> getBookings(@RequestParam(name = "showingId", required = false) Integer showingId,
                                            @RequestParam(name = "email", required = false) String email,
                                            HttpServletRequest http) {
        roleGuard.require(http);
        return bookingService.getBookings(showingId, email);
    }

    @GetMapping("/{bookingId}")
    public BookingDetails getBooking(@PathVariable("bookingId") int bookingId) {
        return bookingService.getBooking(bookingId);
    }

    // Annullér. Medarbejdere må altid. Kunder sender deres email med: DELETE /api/bookings/5?email=anna@mail.dk
    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(@PathVariable("bookingId") int bookingId,
                              @RequestParam(name = "email", required = false) String email,
                              HttpServletRequest http) {
        bookingService.cancelBooking(bookingId, email, roleGuard.isLoggedIn(http));
    }

    // Billetkontrol ved indgangen
    @PutMapping("/{bookingId}/check-in")
    public BookingDetails checkIn(@PathVariable("bookingId") int bookingId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.INSPECTOR, RoleGuard.CLERK);
        return bookingService.checkIn(bookingId);
    }

    // Enkel sædeoversigt. Samme oplysninger findes med rækker og sæde-id på GET /api/showings/{id}/seats
    @GetMapping("/showing/{showingId}/seats")
    public ResponseEntity<List<SeatOverviewDto>> getSeatOverview(@PathVariable("showingId") int showingId) {
        return ResponseEntity.ok(bookingService.getSeatOverview(showingId));
    }
}
