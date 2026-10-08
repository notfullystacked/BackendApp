package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

// Spring Data laver SQL'en ud fra metodenavnet, fx findByActiveTrue = WHERE active = true
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    List<Movie> findByActiveTrue();

    List<Movie> findByActiveTrueOrderByName();

    // Fremhævede film, dem med nærmeste premiere først
    List<Movie> findByActiveTrueAndPromotedTrueOrderByPremiereDateDesc();

    // Coming soon: premieren ligger efter i dag
    List<Movie> findByActiveTrueAndPremiereDateAfterOrderByPremiereDate(LocalDate date);

    // Premierefilm: premieren ligger mellem to datoer (begge inklusive)
    List<Movie> findByActiveTrueAndPremiereDateBetweenOrderByPremiereDateDesc(LocalDate from, LocalDate to);
}
