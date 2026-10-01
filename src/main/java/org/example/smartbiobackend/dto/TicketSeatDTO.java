package org.example.smartbiobackend.dto;

public class TicketSeatDTO {

    private final String seatCode;
    private final String ticketTypeName;
    private final int price;

    public TicketSeatDTO(String seatCode, String ticketTypeName, int price) {
        this.seatCode = seatCode;
        this.ticketTypeName = ticketTypeName;
        this.price = price;
    }

    public String getSeatCode() { return seatCode; }
    public String getTicketTypeName() { return ticketTypeName; }
    public int getPrice() { return price; }
}