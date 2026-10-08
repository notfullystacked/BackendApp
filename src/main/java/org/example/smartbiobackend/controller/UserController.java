package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpSession;
import org.example.smartbiobackend.exception.UnauthorizedException;
import org.example.smartbiobackend.model.User;
import org.example.smartbiobackend.model.dto.BookingDetails;
import org.example.smartbiobackend.model.dto.LoginRequest;
import org.example.smartbiobackend.model.dto.RegisterRequest;
import org.example.smartbiobackend.model.dto.UserResponse;
import org.example.smartbiobackend.service.BookingService;
import org.example.smartbiobackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Kunder (users). Medarbejder-login ligger i AuthController under /api/auth
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String USER_ID = "userId";

    private final UserService userService;
    private final BookingService bookingService;

    public UserController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    // "Mine bookinger" for en registreret kunde
    @GetMapping("/{userId}/bookings")
    public List<BookingDetails> getBookings(@PathVariable("userId") int userId) {
        return bookingService.getBookingsForUser(userId);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Kunde-login. Kundens id gemmes i sessionen (nøglen "userId"), så /profile ved, hvem der er logget ind.
    // Medarbejdere bruger en anden nøgle ("employeeId" i AuthController), så de to slags login ikke blandes
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        User user = userService.login(request);
        session.setAttribute(USER_ID, user.getId());
        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getEmail());
        return ResponseEntity.ok(response);
    }

    // Den kunde, der er logget ind (401 hvis ingen er det)
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(HttpSession session) {
        if (!(session.getAttribute(USER_ID) instanceof Integer userId)) {
            throw new UnauthorizedException("Ikke logget ind");
        }
        User user = userService.getById(userId);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getEmail()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.removeAttribute(USER_ID);
        return ResponseEntity.noContent().build();
    }
}
