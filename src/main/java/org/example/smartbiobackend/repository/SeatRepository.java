package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByAuditoriumIdOrderBySeatRowAscSeatNumberAsc(int auditoriumId);
}
