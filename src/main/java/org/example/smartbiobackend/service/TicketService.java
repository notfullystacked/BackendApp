package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.Booking;
import org.example.smartbiobackend.model.dto.TicketDTO;
import org.example.smartbiobackend.model.dto.TicketSeatDTO;
import org.example.smartbiobackend.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final BookingRepository bookingRepository;
    private final QrCodeService qrCodeService;

    public TicketService(BookingRepository bookingRepository,QrCodeService qrCodeService) {
        this.bookingRepository = bookingRepository;
        this.qrCodeService = qrCodeService;
    }

    public TicketDTO getTicket(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No booking with id " + bookingId));

        String movieTitle = booking.getShowing().getMovie().getName();
        LocalDateTime showingStart = booking.getShowing().getStartTime();
        String auditoriumName = booking.getShowing().getAuditorium().getAuditoriumName();
        String qrContent = "TICKET-" + booking.getId();

        BufferedImage qrImage = qrCodeService.generateQrCode(
                qrContent,
                300,
                300
        );

        String qrCode = convertQrToBase64(qrImage);

        List<TicketSeatDTO> seats = booking.getBookingSeats().stream()
                .map(bookingSeat -> new TicketSeatDTO(
                        bookingSeat.getSeat().getSeatCode(),
                        bookingSeat.getTicketType().getTicketName(),
                        bookingSeat.getTicketType().getPrice()))
                .toList();

        return new TicketDTO(
                booking.getId(),
                movieTitle,
                showingStart,
                auditoriumName,
                seats,
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                qrCode
        );
    }
    private String convertQrToBase64(BufferedImage qrImage) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ImageIO.write(qrImage, "PNG", outputStream);

            return Base64.getEncoder()
                    .encodeToString(outputStream.toByteArray());

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not convert QR code to Base64",
                    e
            );
        }
    }
}