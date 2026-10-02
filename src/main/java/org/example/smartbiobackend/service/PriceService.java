package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.TicketType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceService {

    public int calculateTotal(List<TicketType> tickets) {
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("Der skal være mindst én billet");
        }
        int total = 0;
        for (TicketType ticket : tickets) {
        if (ticket.getPrice() < 0) {
            throw new IllegalArgumentException("Prisen kan ikke være negativ");
        }
        total += ticket.getPrice();
        }
        return total;
    }
}
