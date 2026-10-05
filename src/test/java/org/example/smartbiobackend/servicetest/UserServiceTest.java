package org.example.smartbiobackend.servicetest;

import org.example.smartbiobackend.model.User;
import org.example.smartbiobackend.model.dto.LoginRequest;
import org.example.smartbiobackend.model.dto.RegisterRequest;
import org.example.smartbiobackend.repository.UserRepository;
import org.example.smartbiobackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registeringSavesUserWithNameAndEmail() {
        RegisterRequest request = new RegisterRequest(
                "Lucas", "lucas@mail.dk", "hemmelig123", LocalDate.of(2000,1,1));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->  invocation.getArgument(0));

        User saved = userService.register(request);

        assertEquals("Lucas", saved.getName());
        assertEquals("lucas@mail.dk", saved.getEmail());
    }

    @Test
    void registeringStoresHashedPasswordNotPlainText() {
        RegisterRequest request = new RegisterRequest(
                "Lucas", "lucas@gmail.dk",  "hemmelig123", LocalDate.of(2000,1,1));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->  invocation.getArgument(0));

        User saved = userService.register(request);

        assertNotEquals("hemmelig123", saved.getPassword());
        assertTrue(passwordEncoder.matches("hemmelig123", saved.getPassword()));
    }

    @Test
    void registeringWithAlreadyUsedEmail_IsRejected() {
        RegisterRequest request = new RegisterRequest(
                "Lucas", "lucas@mail.dk", "hemmelig123", LocalDate.of(2000,1,1));
        when(userRepository.existsByEmail("lucas@mail.dk")).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userService.register(request));

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginWithCorrectEmailAndPasswordReturnsUser() {
        User existingUser = new User("Lucas", "lucas@mail.dk", LocalDate.of(2000,1,1));
        existingUser.setPassword(passwordEncoder.encode("hemmelig123"));
        when(userRepository.findByEmail("lucas@mail.dk")).thenReturn(Optional.of(existingUser));

        User loggedIn = userService.login(new LoginRequest("lucas@mail.dk", "hemmelig123"));

        assertEquals("lucas@mail.dk", loggedIn.getEmail());
    }

    @Test
    void loginWithWrongEmail_IsRejected() {
        when(userRepository.findByEmail("ukendt@mail.dk")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.login(new LoginRequest("ukendt@mail.dk", "hemmelig123")));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void loginWithWrongPassword_IsRejected() {
        User user = new User("Lucas", "lucas@mail.dk", LocalDate.of(2000,1,1));
        user.setPassword(passwordEncoder.encode("hemmelig123"));
        when(userRepository.findByEmail("lucas@mail.dk")).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.login(new LoginRequest("lucas@mail.dk", "forkert")));

        assertEquals("Invalid email or password", exception.getMessage());
    }
}
