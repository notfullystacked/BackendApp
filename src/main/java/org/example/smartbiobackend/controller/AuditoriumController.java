package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.smartbiobackend.model.Auditorium;
import org.example.smartbiobackend.model.dto.AuditoriumRequest;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.service.AuditoriumService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoriums")
public class AuditoriumController {

    private final AuditoriumService auditoriumService;
    private final RoleGuard roleGuard;

    public AuditoriumController(AuditoriumService auditoriumService, RoleGuard roleGuard) {
        this.auditoriumService = auditoriumService;
        this.roleGuard = roleGuard;
    }

    @GetMapping
    public List<Auditorium> getAll() {
        return auditoriumService.getAll();
    }

    // POST /api/auditoriums  body: {"name":"Sal 3","rowCount":10,"seatsPerRow":8}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Auditorium create(@RequestBody AuditoriumRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return auditoriumService.createAuditorium(request.name(), request.rowCount(), request.seatsPerRow());
    }
}
