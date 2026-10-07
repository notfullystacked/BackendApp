package org.example.smartbiobackend.model.dto;

import java.util.List;

public record SeatMapResponse(int showingId, String auditoriumName, int rowCount, int seatsPerRow,
                              List<SeatStatus> seats) {
}
