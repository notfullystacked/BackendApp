package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Showing;
import org.example.smartbiobackend.model.ShowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowingRepository extends JpaRepository<Showing, Integer> {

    List<Showing> findByStartTimeBetweenAndStatusOrderByStartTime(LocalDateTime from, LocalDateTime to, ShowingStatus status);

    List<Showing> findByAuditoriumIdAndStatus(int auditoriumId, ShowingStatus status);

    List<Showing> findByMovieIdAndStatusAndStartTimeAfter(int movieId, ShowingStatus status, LocalDateTime after);

    List<Showing> findByMovieIdAndStatusAndStartTimeAfterOrderByStartTime(int movieId, ShowingStatus status, LocalDateTime after);

    List<Showing> findByAuditoriumIdAndStatusAndStartTimeAfter(int auditoriumId, ShowingStatus status, LocalDateTime after);

    List<Showing> findAllByOrderByStartTime();

    boolean existsByMovieId(int movieId);

    boolean existsByAuditoriumId(int auditoriumId);
}
