package org.example.smartbiobackend.service;

import org.example.smartbiobackend.dto.EmployeeDto;
import org.example.smartbiobackend.exception.UnauthorizedException;
import org.example.smartbiobackend.model.Employee;
import org.example.smartbiobackend.model.Role;
import org.example.smartbiobackend.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Samme fejl uanset om brugernavnet eller adgangskoden er forkert,
    // så man ikke kan gætte sig til hvilke brugernavne der findes
    @Transactional(readOnly = true)
    public EmployeeDto login(String username, String password) {
        Employee employee = employeeRepository.findByUsername(username)
                .filter(e -> passwordEncoder.matches(password, e.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Forkert brugernavn eller adgangskode"));

        return toDto(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployee(int employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new UnauthorizedException("Ikke logget ind"));

        return toDto(employee);
    }

    private EmployeeDto toDto(Employee employee) {
        List<String> roles = employee.getRoles().stream()
                .map(Role::getRoleName)
                .sorted()
                .toList();

        return new EmployeeDto(employee.getId(), employee.getName(), employee.getUsername(), roles);
    }
}