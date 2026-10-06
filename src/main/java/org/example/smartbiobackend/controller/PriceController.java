package org.example.smartbiobackend.controller;

import org.example.smartbiobackend.model.dto.PriceResponse;
import org.example.smartbiobackend.service.PriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/{bookingId}/price")
    public PriceResponse getPrice(@PathVariable int bookingId) {
    int total = priceService.calculateTotalForBooking(bookingId);
    return new PriceResponse(bookingId, total);
    }
}
