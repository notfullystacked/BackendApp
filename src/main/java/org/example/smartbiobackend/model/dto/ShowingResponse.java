package org.example.smartbiobackend.model.dto;

import org.example.smartbiobackend.model.Genre;
import org.example.smartbiobackend.model.ShowingStatus;

import java.time.LocalDateTime;

public record ShowingResponse(int id, int movieId, String movieName, Genre genre, int ageRestriction,
                              int runTimeSeconds, int auditoriumId, String auditoriumName,
                              LocalDateTime startTime, ShowingStatus status) {
}
