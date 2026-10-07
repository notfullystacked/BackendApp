package org.example.smartbiobackend.model.dto;

import java.util.List;

// userId sættes for registrerede kunder. Ellers bruges guestName/guestMail (også når ekspedienten booker over telefonen)
public record BookingRequest(Integer userId, String guestName, String guestMail,
                             int showingId, List<SeatTicket> seats) {
}
