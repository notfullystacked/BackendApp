package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
}
