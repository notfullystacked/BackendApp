package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
<<<<<<< HEAD
    Optional<Seat> findBySeatCode(String seatCode, int auditoriumId);
=======
>>>>>>> 359cbcc (added repositories, added an initial data config to show a booking, starting on controller from now on)
}
