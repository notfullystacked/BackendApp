package org.example.smartbiobackend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.smartbiobackend.dto.EmployeeDto;
import org.example.smartbiobackend.exception.ForbiddenException;
import org.example.smartbiobackend.exception.UnauthorizedException;
import org.example.smartbiobackend.service.AuthService;
import org.springframework.stereotype.Component;

import java.util.Arrays;

// Tjekker at en medarbejder er logget ind (via AuthController) og har en af de tilladte roller. Admin må altid
@Component
public class RoleGuard {

    // Samme navne som i data.sql
    public static final String ADMIN = "Admin";
    public static final String MOVIE_EDITOR = "MovieEditor";
    public static final String CLERK = "Clerk";           // billetsalg og kiosk
    public static final String OPERATOR = "Operator";     // kører filmene
    public static final String INSPECTOR = "Inspector";   // billetkontrol og rengøring

    private static final String EMPLOYEE_ID = "employeeId"; // samme nøgle som AuthController

    private final AuthService authService;

    public RoleGuard(AuthService authService) {
        this.authService = authService;
    }

    // Uden roller: kræver bare at man er logget ind som medarbejder
    public EmployeeDto require(HttpServletRequest request, String... allowedRoles) {
        HttpSession session = request.getSession(false);
        Object id = session != null ? session.getAttribute(EMPLOYEE_ID) : null;
        if (!(id instanceof Integer employeeId)) {
            throw new UnauthorizedException("Ikke logget ind");
        }

        EmployeeDto employee = authService.getEmployee(employeeId);
        if (allowedRoles.length == 0 || employee.roles().contains(ADMIN)) {
            return employee;
        }

        boolean hasRole = Arrays.stream(allowedRoles).anyMatch(employee.roles()::contains);
        if (!hasRole) {
            throw new ForbiddenException("Du har ikke adgang til dette");
        }
        return employee;
    }

    // true hvis der er en medarbejder logget ind. Kaster ikke en fejl, i modsætning til require
    public boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(EMPLOYEE_ID) instanceof Integer;
    }
}
