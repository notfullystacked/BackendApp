package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Showing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShowingRepository extends JpaRepository<Showing, Integer> {

    List<Showing> findByAuditorium_IdAndDate(
            int auditoriumId,
            LocalDate date
    );

    List<Showing> findByDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );
}