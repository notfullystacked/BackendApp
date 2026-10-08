package org.example.smartbiobackend.dto;

import java.time.LocalDateTime;

public record BookingDetailsDto(
        int bookingId,
        String customerName,
        String customerEmail,
        String seatCode,
        int showingId,
        String movieName,
        LocalDateTime startTime
) {}