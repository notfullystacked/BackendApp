package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketType, Integer> {
}