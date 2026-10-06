package org.example.smartbiobackend.model.dto;

public record BookingRequest(String seatCode, Integer userId, String guestName, String guestMail, int showingId, int ticketTypeId) {
}

//Lucas added ticketTypeId