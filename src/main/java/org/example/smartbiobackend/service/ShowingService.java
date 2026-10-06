package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.ShowingStatus;
import org.example.smartbiobackend.repository.ShowingRepository;
import org.springframework.stereotype.Service;

@Service
public class ShowingService {
    private final ShowingRepository showingRepository;

    public ShowingService(ShowingRepository showingRepository) {
        this.showingRepository = showingRepository;
    }
    public void cancelShowing(int showingId){
        Showing showing = showingRepository.findById(showingId)
                .orElseThrow(() -> new IllegalArgumentException("Showing not found"));
        showing.setStatus(ShowingStatus.CANCELLED);
        showingRepository.save(showing);
    }
}
