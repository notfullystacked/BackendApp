package org.example.smartbiobackend.controller;

import org.example.smartbiobackend.service.ShowingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/showings")
public class ShowingController {
    private final ShowingService showingService;

    public ShowingController (ShowingService showingService){
        this.showingService = showingService;
    }

    @PutMapping("/{showingId}/cancel")
    public void cancelShowing(@PathVariable int showingId){
        showingService.cancelShowing(showingId);

    }

}
