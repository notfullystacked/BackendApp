package org.example.smartbiobackend.security;

import jakarta.servlet.http.HttpSession;
import org.example.smartbiobackend.exception.UnauthorizedException;
import org.example.smartbiobackend.model.Employee;
import org.example.smartbiobackend.repository.EmployeeRepository;
import org.springframework.stereotype.Component;

@Component
public class RoleGuard {

    private static final String EMPLOYEE_ID = "employeeId";

    private final EmployeeRepository employeeRepository;

    public RoleGuard(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public void requireEmployee(HttpSession session) {
        if (session == null) {
            throw new UnauthorizedException("Ikke logget ind");
        }

        Integer employeeId =
                (Integer) session.getAttribute(EMPLOYEE_ID);

        if (employeeId == null) {
            throw new UnauthorizedException("Ikke logget ind");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new UnauthorizedException("Medarbejder ikke fundet"));

        boolean isEmployee = employee.getRoles().stream()
                .anyMatch(role ->
                        "EMPLOYEE".equalsIgnoreCase(role.getRoleName()));

        if (!isEmployee) {
            throw new UnauthorizedException("Ingen adgang");
        }
    }

}