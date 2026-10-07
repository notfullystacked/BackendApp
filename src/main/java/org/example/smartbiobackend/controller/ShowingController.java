package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.smartbiobackend.model.dto.SeatMapResponse;
import org.example.smartbiobackend.model.dto.ShowingRequest;
import org.example.smartbiobackend.model.dto.ShowingResponse;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.service.ShowingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/showings")
public class ShowingController {

    private final ShowingService showingService;
    private final RoleGuard roleGuard;

    public ShowingController(ShowingService showingService, RoleGuard roleGuard) {
        this.showingService = showingService;
        this.roleGuard = roleGuard;
    }

    // GET /api/showings – programmet de næste 3 måneder
    @GetMapping
    public List<ShowingResponse> getUpcoming() {
        return showingService.getUpcoming();
    }

    @GetMapping("/{showingId}")
    public ShowingResponse getShowing(@PathVariable("showingId") int showingId) {
        return showingService.getShowing(showingId);
    }

    // GET /api/showings/{id}/seats – sædeoversigt med ledige/optagne sæder
    @GetMapping("/{showingId}/seats")
    public SeatMapResponse getSeatMap(@PathVariable("showingId") int showingId) {
        return showingService.getSeatMap(showingId);
    }

    // POST /api/showings  body: {"movieId":1,"auditoriumId":1,"startTime":"2026-11-01T19:00:00"}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowingResponse createShowing(@RequestBody ShowingRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return showingService.createShowing(request);
    }

    @PutMapping("/{showingId}")
    public ShowingResponse updateShowing(@PathVariable("showingId") int showingId,
                                         @RequestBody ShowingRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        return showingService.updateShowing(showingId, request);
    }

    @PutMapping("/{showingId}/cancel")
    public void cancelShowing(@PathVariable("showingId") int showingId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.MOVIE_EDITOR);
        showingService.cancelShowing(showingId);
    }
}
