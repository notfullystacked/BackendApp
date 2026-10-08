package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpSession;
import org.example.smartbiobackend.model.User;
import org.example.smartbiobackend.model.dto.LoginRequest;
import org.example.smartbiobackend.model.dto.RegisterRequest;
import org.example.smartbiobackend.model.dto.UserResponse;
import org.example.smartbiobackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Kunder (users). Medarbejder-login ligger i AuthController under /api/auth
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody RegisterRequest request) {

        User user = userService.register(request);

        UserResponse response =
                new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @RequestBody LoginRequest request,
            HttpSession session) {

        User user = userService.login(request);

        session.setAttribute("userId", user.getId());

        UserResponse response =
                new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            HttpSession session) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userService.getById(userId);

        UserResponse response =
                new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                );

        return ResponseEntity.ok(response);
    }
}