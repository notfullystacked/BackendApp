package org.example.smartbiobackend.controllertest;

import org.example.smartbiobackend.controller.AuthController;
import org.example.smartbiobackend.model.User;
import org.example.smartbiobackend.model.dto.LoginRequest;
import org.example.smartbiobackend.model.dto.RegisterRequest;
import org.example.smartbiobackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void registerReturnsCreatedWithoutPassword() throws Exception {
        User user = new User("Lucas", "lucas@mail.dk", LocalDate.of(2000, 1, 1));
        user.setPassword("$2a$10$enHashSomIkkeMaaSendesUd");
        when(userService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lucas",
                                  "email": "lucas@mail.dk",
                                  "password": "hemmelig123",
                                  "birthday": "2000-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lucas"))
                .andExpect(jsonPath("$.email").value("lucas@mail.dk"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginWithCorrectCredentials_ReturnsOkWithoutPassword() throws Exception {
        User user = new User("Lucas", "lucas@mail.dk", LocalDate.of(2000, 1, 1));
        user.setPassword("$2a$10$enHashSomIkkeMaaSendesUd");
        when(userService.login(any(LoginRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "lucas@mail.dk",
                              "password": "hemmelig123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lucas"))
                .andExpect(jsonPath("$.email").value("lucas@mail.dk"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}