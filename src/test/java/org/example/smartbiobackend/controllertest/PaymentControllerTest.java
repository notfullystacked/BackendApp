package org.example.smartbiobackend.controllertest;

import org.example.smartbiobackend.controller.PaymentController;
import org.example.smartbiobackend.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void PayingBookingReturnsOk() throws Exception {
        mockMvc.perform(post("/api/bookings/1/pay")).andExpect(status().isOk());

        verify(paymentService).payForBooking(1);
    }
}
