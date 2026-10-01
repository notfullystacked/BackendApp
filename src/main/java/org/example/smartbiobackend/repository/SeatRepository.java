package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
}
