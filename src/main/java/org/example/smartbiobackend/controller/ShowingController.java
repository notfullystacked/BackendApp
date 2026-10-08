package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpSession;
import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.service.ShowingService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/showings")
public class ShowingController {

    private final ShowingService showingService;
    private final RoleGuard roleGuard;

    public ShowingController(
            ShowingService showingService,
            RoleGuard roleGuard) {
        this.showingService = showingService;
        this.roleGuard = roleGuard;
    }

    @PutMapping("/{showingId}/cancel")
    public void cancelShowing(
            @PathVariable int showingId,
            HttpSession session) {

        roleGuard.requireEmployee(session);
        showingService.cancelShowing(showingId);
    }

    @PostMapping
    public Showing createShowing(
            @RequestParam int movieId,
            @RequestParam int auditoriumId,
            @RequestParam String date,
            @RequestParam String startTime) {

        return showingService.createShowing(
                movieId,
                auditoriumId,
                LocalDate.parse(date),
                LocalDateTime.parse(startTime)
        );
    }

    @PutMapping("/{showingId}")
    public Showing updateShowing(
            @PathVariable int showingId,
            @RequestParam int movieId,
            @RequestParam int auditoriumId,
            @RequestParam String date,
            @RequestParam String startTime) {

        return showingService.updateShowing(
                showingId,
                movieId,
                auditoriumId,
                LocalDate.parse(date),
                LocalDateTime.parse(startTime)
        );
    }

    @GetMapping("/plan")
    public List<Showing> getThreeMonthPlan() {
        return showingService.getThreeMonthPlan();
    }
}