package org.example.smartbiobackend.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TicketDTO {

    private final int bookingId;
    private final String movieTitle;
    private final LocalDateTime showingStart;
    private final String auditoriumName;
    private final List<TicketSeatDTO> seats;
    private final String customerName;
    private final String customerEmail;

    public TicketDTO(int bookingId, String movieTitle, LocalDateTime showingStart,
                     String auditoriumName, List<TicketSeatDTO> seats,
                     String customerName, String customerEmail) {
        this.bookingId = bookingId;
        this.movieTitle = movieTitle;
        this.showingStart = showingStart;
        this.auditoriumName = auditoriumName;
        this.seats = seats;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }

    public int getBookingId() { return bookingId; }
    public String getMovieTitle() { return movieTitle; }
    public LocalDateTime getShowingStart() { return showingStart; }
    public String getAuditoriumName() { return auditoriumName; }
    public List<TicketSeatDTO> getSeats() { return seats; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getQrPlaceholder() { return "TICKET-" + bookingId; }

    public int getTotalPrice() {
        return seats.stream().mapToInt(TicketSeatDTO::getPrice).sum();
    }

}