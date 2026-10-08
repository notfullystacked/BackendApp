package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByShowingIdOrderByIdDesc(int showingId);

    List<Booking> findByCustomerEmailIgnoreCaseOrderByIdDesc(String customerEmail);

    List<Booking> findByUserIdOrderByIdDesc(int userId);

    List<Booking> findAllByOrderByIdDesc();
}
