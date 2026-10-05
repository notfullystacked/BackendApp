package org.example.smartbiobackend.service;

import org.example.smartbiobackend.model.User;
import org.example.smartbiobackend.model.dto.RegisterRequest;
import org.example.smartbiobackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register (RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())){
            throw new IllegalStateException("Email already in use");
        }
        User user = new User(request.name(), request.email(), request.birthday());
        user.setPassword(passwordEncoder.encode(request.password()));
        return userRepository.save(user);
    }
}