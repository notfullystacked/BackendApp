package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    Optional<Seat> findBySeatCodeAndAuditoriumId(String seatCode, int auditoriumId);
    List<Seat> findByAuditoriumId(int auditoriumId);
}
