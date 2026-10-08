package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.smartbiobackend.model.Auditorium;
import org.example.smartbiobackend.model.Seat;
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

    // ISSUE-9: alle sale med rowCount, seatsPerRow og capacity
    @GetMapping
    public List<Auditorium> getAll() {
        return auditoriumService.getAll();
    }

    @GetMapping("/{auditoriumId}")
    public Auditorium getAuditorium(@PathVariable("auditoriumId") int auditoriumId) {
        return auditoriumService.getAuditorium(auditoriumId);
    }

    @GetMapping("/{auditoriumId}/seats")
    public List<Seat> getSeats(@PathVariable("auditoriumId") int auditoriumId) {
        return auditoriumService.getSeats(auditoriumId);
    }

    // POST /api/auditoriums  body: {"name":"Sal 3","rowCount":10,"seatsPerRow":8}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Auditorium create(@RequestBody AuditoriumRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return auditoriumService.createAuditorium(request.name(), request.rowCount(), request.seatsPerRow());
    }

    @PutMapping("/{auditoriumId}")
    public Auditorium update(@PathVariable("auditoriumId") int auditoriumId,
                             @RequestBody AuditoriumRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return auditoriumService.updateAuditorium(auditoriumId, request.name(), request.rowCount(), request.seatsPerRow());
    }

    @PutMapping("/{auditoriumId}/close")
    public Auditorium close(@PathVariable("auditoriumId") int auditoriumId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return auditoriumService.closeAuditorium(auditoriumId);
    }

    @PutMapping("/{auditoriumId}/open")
    public Auditorium open(@PathVariable("auditoriumId") int auditoriumId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return auditoriumService.openAuditorium(auditoriumId);
    }
}
