package org.example.smartbiobackend.controller;

import org.example.smartbiobackend.model.User;
import org.example.smartbiobackend.model.dto.LoginRequest;
import org.example.smartbiobackend.model.dto.RegisterRequest;
import org.example.smartbiobackend.model.dto.UserResponse;
import org.example.smartbiobackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@RequestBody LoginRequest request) {
        User user = userService.login(request);
        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getEmail());
        return ResponseEntity.ok(response);
    }
}
