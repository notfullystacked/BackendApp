package org.example.smartbiobackend.model.dto;

import org.example.smartbiobackend.model.ShowingStatus;

import java.time.LocalDateTime;
import java.util.List;

// Alt om én booking samlet til frontenden (kundens oversigt og ekspedientens liste).
// showingStatus er med, så man kan se, hvis forestillingen er blevet aflyst
public record BookingDetails(int bookingId, int showingId, String movieName, String auditoriumName,
                             LocalDateTime startTime, ShowingStatus showingStatus,
                             String customerName, String customerEmail,
                             List<TicketSeatDTO> seats, int totalPrice,
                             boolean paid, boolean checkedIn, LocalDateTime createdAt) {
}
