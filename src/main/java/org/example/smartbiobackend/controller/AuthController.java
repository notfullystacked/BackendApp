package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.smartbiobackend.dto.EmployeeDto;
import org.example.smartbiobackend.dto.LoginRequest;
import org.example.smartbiobackend.exception.UnauthorizedException;
import org.example.smartbiobackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String EMPLOYEE_ID = "employeeId";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login  body: {"username":"mads","password":"kode123"}
    @PostMapping("/login")
    public EmployeeDto login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        EmployeeDto employee = authService.login(loginRequest.username(), loginRequest.password());

        // Ny session ved login, så en gammel session-id ikke kan genbruges
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        request.getSession(true).setAttribute(EMPLOYEE_ID, employee.id());

        return employee;
    }

    // GET /api/auth/me  – bruges af navbaren til at vise navn og rolle
    @GetMapping("/me")
    public EmployeeDto me(@SessionAttribute(name = EMPLOYEE_ID, required = false) Integer employeeId) {
        if (employeeId == null) {
            throw new UnauthorizedException("Ikke logget ind");
        }
        return authService.getEmployee(employeeId);
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
