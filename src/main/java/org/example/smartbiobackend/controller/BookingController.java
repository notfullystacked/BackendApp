package org.example.smartbiobackend.controller;

import java.util.List;
import org.example.smartbiobackend.dto.SeatOverviewDto;
import org.example.smartbiobackend.model.Booking;
import org.example.smartbiobackend.model.dto.BookingRequest;
import org.example.smartbiobackend.model.dto.BookingResponse;
import org.example.smartbiobackend.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.dto.BookingDetailsDto;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final RoleGuard roleGuard;

    public BookingController(
            BookingService bookingService,
            RoleGuard roleGuard) {
        this.bookingService = bookingService;
        this.roleGuard = roleGuard;
    }

    @PostMapping("/reserve")
    public ResponseEntity<BookingResponse> reserveSeat(
            @RequestBody BookingRequest request,
            HttpSession session) {

        roleGuard.requireEmployee(session);

        BookingResponse response =
                bookingService.processBooking(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/showing/{showingId}/seats")
    public ResponseEntity<List<SeatOverviewDto>> getSeatOverview(
            @PathVariable int showingId) {
        List<SeatOverviewDto> seats =
                bookingService.getSeatOverview(showingId);

        return ResponseEntity.ok(seats);
    }
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDetailsDto> getBookingById(
            @PathVariable int bookingId,
            HttpSession session) {

        roleGuard.requireEmployee(session);

        BookingDetailsDto booking =
                bookingService.findBookingById(bookingId);

        return ResponseEntity.ok(booking);
    }
    @GetMapping("/showing/{showingId}")
    public ResponseEntity<List<BookingDetailsDto>> getBookingsByShowing(
            @PathVariable int showingId,
            HttpSession session) {

        roleGuard.requireEmployee(session);

        List<BookingDetailsDto> bookings =
                bookingService.getBookingsByShowing(showingId);

        return ResponseEntity.ok(bookings);
    }
}
