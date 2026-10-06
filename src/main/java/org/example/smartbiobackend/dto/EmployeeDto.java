package org.example.smartbiobackend.dto;

import java.util.List;

// Det frontenden får – aldrig password
public record EmployeeDto(int id, String name, String username, List<String> roles) {
}