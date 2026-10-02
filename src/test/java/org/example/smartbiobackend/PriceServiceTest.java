package org.example.smartbiobackend;

import org.example.smartbiobackend.model.TicketType;
import org.example.smartbiobackend.service.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PriceServiceTest {

    private PriceService priceService = new PriceService();

    @BeforeEach
    void setUp() {
        priceService = new PriceService();
    }

    @Test
    public void totalForOneTicketIsTheTicketPrice() {
        TicketType adult = new TicketType("Voksen", 120);

        int total = priceService.calculateTotal(List.of(adult));

        assertEquals(120, total);
    }

    @Test
    void totalForSeveralTicketsIsTheTotalPrice() {
        TicketType adult  = new TicketType("Voksen", 120);
        TicketType child = new TicketType("Barn", 80);

        int total = priceService.calculateTotal(List.of(adult,adult,child));

        assertEquals(320, total);
    }

    @Test
    void calculatingTotalWithNoTickets_IsRejected() {

        assertThrows(IllegalArgumentException.class, () -> priceService.calculateTotal(List.of()));
    }

    @Test
    void calculatingTotalWithNegativeTickets_IsRejected() {
        TicketType negative = new TicketType("Fejl", -10);

        assertThrows(IllegalArgumentException.class, () -> priceService.calculateTotal(List.of(negative)));
    }
}
