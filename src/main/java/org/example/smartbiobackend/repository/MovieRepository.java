package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByActiveTrue();
}
