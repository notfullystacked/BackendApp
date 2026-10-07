package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.TicketType;
import org.example.smartbiobackend.repository.TicketTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    public TicketTypeService(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    public List<TicketType> getAll() {
        return ticketTypeRepository.findAll();
    }
}
