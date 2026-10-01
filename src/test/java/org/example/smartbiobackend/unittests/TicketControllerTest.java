package org.example.smartbiobackend.unittests;

import org.example.smartbiobackend.controller.TicketController;
import org.example.smartbiobackend.dto.TicketDTO;
import org.example.smartbiobackend.dto.TicketSeatDTO;
import org.example.smartbiobackend.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void getTicket_WhenBookingExists_ReturnsTicketJson() throws Exception {
        TicketDTO ticket = new TicketDTO(
                1,
                "Inception",
                LocalDateTime.of(2026, 10, 1, 20, 0),
                "Hall 1",
                List.of(new TicketSeatDTO("A1", "Adult", 95)),
                "Alice",
                "alice@example.com"
        );
        when(ticketService.getTicket(1)).thenReturn(ticket);

        mockMvc.perform(get("/api/bookings/1/ticket").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieTitle").value("Inception"))
                .andExpect(jsonPath("$.seats[0].seatCode").value("A1"))
                .andExpect(jsonPath("$.qrPlaceholder").value("TICKET-1"));
    }

    @Test
    void getTicket_WhenBookingDoesNotExist_ReturnsNotFound() throws Exception {
        when(ticketService.getTicket(99))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No booking with id 99"));

        mockMvc.perform(get("/api/bookings/99/ticket"))
                .andExpect(status().isNotFound());
    }
}