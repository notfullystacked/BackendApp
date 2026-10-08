package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.smartbiobackend.model.TicketType;
import org.example.smartbiobackend.model.dto.TicketTypeRequest;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.service.TicketTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket-types")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;
    private final RoleGuard roleGuard;

    public TicketTypeController(TicketTypeService ticketTypeService, RoleGuard roleGuard) {
        this.ticketTypeService = ticketTypeService;
        this.roleGuard = roleGuard;
    }

    @GetMapping
    public List<TicketType> getAll() {
        return ticketTypeService.getAll();
    }

    // POST /api/ticket-types  body: {"ticketName":"Studerende","price":90}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketType create(@RequestBody TicketTypeRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return ticketTypeService.createTicketType(request);
    }

    @PutMapping("/{ticketTypeId}")
    public TicketType update(@PathVariable("ticketTypeId") int ticketTypeId,
                             @RequestBody TicketTypeRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return ticketTypeService.updateTicketType(ticketTypeId, request);
    }
}
