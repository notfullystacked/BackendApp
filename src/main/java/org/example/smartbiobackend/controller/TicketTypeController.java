package org.example.smartbiobackend.controller;

import org.example.smartbiobackend.model.TicketType;
import org.example.smartbiobackend.service.TicketTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ticket-types")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @GetMapping
    public List<TicketType> getAll() {
        return ticketTypeService.getAll();
    }
}
