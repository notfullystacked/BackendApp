package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Integer> {

    List<BookingSeat> findByBookingId(int bookingId);
}
