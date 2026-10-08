package org.example.smartbiobackend.model.dto;

import org.example.smartbiobackend.model.Genre;
import org.example.smartbiobackend.model.ShowingStatus;

import java.time.LocalDateTime;

// capacity = antal sæder i salen, availableSeats = dem der ikke er booket til denne forestilling
public record ShowingResponse(int id, int movieId, String movieName, Genre genre, int ageRestriction,
                              int runTimeSeconds, int auditoriumId, String auditoriumName,
                              LocalDateTime startTime, LocalDateTime endTime, ShowingStatus status,
                              int capacity, int availableSeats) {
}
