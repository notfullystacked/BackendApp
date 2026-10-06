package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
}
