package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpSession;
import org.example.smartbiobackend.model.Auditorium;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.service.AuditoriumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.smartbiobackend.security.RoleGuard;

import java.util.List;

@RestController
@RequestMapping("/auditoriums")
public class AuditoriumController {

    private final AuditoriumService auditoriumService;
    private final RoleGuard roleGuard;

    public AuditoriumController(
            AuditoriumService auditoriumService,
            RoleGuard roleGuard) {

        this.auditoriumService = auditoriumService;
        this.roleGuard = roleGuard;
    }

    @GetMapping
    public List<Auditorium> getAll() {
        return auditoriumService.getAll();
    }

    @PostMapping
    public Auditorium create(
            @RequestParam String name,
            @RequestParam int rowCount,
            @RequestParam int seatsPerRow) {

        return auditoriumService.createAuditorium(
                name,
                rowCount,
                seatsPerRow
        );
    }

    @PutMapping("/{auditoriumId}/needs-cleaning")
    public ResponseEntity<Auditorium> markNeedsCleaning(
            @PathVariable int auditoriumId,
            HttpSession session) {

        roleGuard.requireEmployee(session);

        return ResponseEntity.ok(
                auditoriumService.markNeedsCleaning(auditoriumId)
        );
    }

    @PutMapping("/{auditoriumId}/clean")
    public ResponseEntity<Auditorium> markClean(
            @PathVariable int auditoriumId,
            HttpSession session) {

        roleGuard.requireEmployee(session);

        return ResponseEntity.ok(
                auditoriumService.markClean(auditoriumId)
        );
    }
}