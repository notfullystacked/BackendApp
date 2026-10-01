package org.example.smartbiobackend.model.dto;

import java.time.LocalDateTime;

public record BookingResponse (Integer bookingId, String seatCode, String recipientEmail, LocalDateTime bookingTime) {
}
