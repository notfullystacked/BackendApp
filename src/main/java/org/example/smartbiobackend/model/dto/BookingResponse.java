package org.example.smartbiobackend.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(Integer bookingId, List<String> seatCodes,
                              String recipientEmail, LocalDateTime bookingTime) {
}
