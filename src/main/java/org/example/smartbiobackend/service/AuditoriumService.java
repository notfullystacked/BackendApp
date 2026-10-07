package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.Auditorium;
import org.example.smartbiobackend.model.Seat;
import org.example.smartbiobackend.repository.AuditoriumRepository;
import org.example.smartbiobackend.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriumService {

    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;

    public AuditoriumService(AuditoriumRepository auditoriumRepository, SeatRepository seatRepository) {
        this.auditoriumRepository = auditoriumRepository;
        this.seatRepository = seatRepository;
    }

    public List<Auditorium> getAll() {
        return auditoriumRepository.findAll();
    }

    // Ny sal = nye sæder automatisk. Ingen kodeændringer nødvendige for flere/større sale
    @Transactional
    public Auditorium createAuditorium(String name, int rowCount, int seatsPerRow) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Salen skal have et navn");
        }
        if (rowCount <= 0 || seatsPerRow <= 0) {
            throw new IllegalArgumentException("Antal rækker og sæder skal være over 0");
        }

        Auditorium auditorium = auditoriumRepository.save(new Auditorium(name, rowCount, seatsPerRow));

        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= rowCount; row++) {
            for (int number = 1; number <= seatsPerRow; number++) {
                seats.add(new Seat(auditorium, row, number));
            }
        }
        seatRepository.saveAll(seats);

        return auditorium;
    }
}
