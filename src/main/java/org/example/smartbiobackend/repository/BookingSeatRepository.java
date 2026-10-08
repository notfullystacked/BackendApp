package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Integer> {

    List<BookingSeat> findByBookingId(int bookingId);

    List<BookingSeat> findByShowingId(int showingId);

    boolean existsByShowingIdAndSeatId(int showingId, int seatId);

    // Antal solgte sæder til en forestilling
    long countByShowingId(int showingId);

    // Har sædet nogensinde været booket? Bruges når en sal gøres mindre
    boolean existsBySeatId(int seatId);
}
