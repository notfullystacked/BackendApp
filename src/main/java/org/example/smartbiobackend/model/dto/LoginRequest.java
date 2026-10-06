package org.example.smartbiobackend.model.dto;

import org.example.smartbiobackend.repository.UserRepository;

public record LoginRequest(String email, String password) {
}
