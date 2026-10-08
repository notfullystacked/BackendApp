package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByShowing_Id(int showingId);
}