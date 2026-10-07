package org.example.smartbiobackend.model.dto;

import java.time.LocalDateTime;

public record ShowingRequest(int movieId, int auditoriumId, LocalDateTime startTime) {
}
