package org.example.smartbiobackend.service;

import org.example.smartbiobackend.exception.NotFoundException;
import org.example.smartbiobackend.model.TicketType;
import org.example.smartbiobackend.model.dto.TicketTypeRequest;
import org.example.smartbiobackend.repository.TicketTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Billettyper og priser ligger i databasen, så priserne kan ændres uden kodeændringer
@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    public TicketTypeService(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    public List<TicketType> getAll() {
        return ticketTypeRepository.findAll();
    }

    @Transactional
    public TicketType createTicketType(TicketTypeRequest request) {
        validate(request);
        return ticketTypeRepository.save(new TicketType(request.ticketName().trim(), request.price()));
    }

    // Allerede solgte billetter peger på billettypen, så en prisændring ændrer også prisen på gamle bookinger.
    // Det er en bevidst forenkling (se docs/backend-guide.md)
    @Transactional
    public TicketType updateTicketType(int ticketTypeId, TicketTypeRequest request) {
        validate(request);
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new NotFoundException("Billettype findes ikke: " + ticketTypeId));
        ticketType.setTicketName(request.ticketName().trim());
        ticketType.setPrice(request.price());
        return ticketTypeRepository.save(ticketType);
    }

    private void validate(TicketTypeRequest request) {
        if (request.ticketName() == null || request.ticketName().isBlank()) {
            throw new IllegalArgumentException("Billettypen skal have et navn");
        }
        if (request.price() < 0) {
            throw new IllegalArgumentException("Prisen kan ikke være negativ");
        }
    }
}
