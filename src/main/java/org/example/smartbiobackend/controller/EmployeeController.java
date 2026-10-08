package org.example.smartbiobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.smartbiobackend.dto.EmployeeDto;
import org.example.smartbiobackend.model.dto.EmployeeDetails;
import org.example.smartbiobackend.model.dto.EmployeeRequest;
import org.example.smartbiobackend.security.RoleGuard;
import org.example.smartbiobackend.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Administration af medarbejdere. Alt her kræver rollen Admin
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final RoleGuard roleGuard;

    public EmployeeController(EmployeeService employeeService, RoleGuard roleGuard) {
        this.employeeService = employeeService;
        this.roleGuard = roleGuard;
    }

    @GetMapping
    public List<EmployeeDetails> getAll(HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return employeeService.getAll();
    }

    // Rollerne, der kan vælges mellem. Står før /{employeeId}, men Spring vælger alligevel altid den mest præcise sti
    @GetMapping("/roles")
    public List<String> getRoles(HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return employeeService.getRoleNames();
    }

    // POST /api/employees/roles  body: {"roleName":"Cleaner"}
    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public List<String> createRole(@RequestBody Map<String, String> body, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return employeeService.createRole(body.get("roleName"));
    }

    @GetMapping("/{employeeId}")
    public EmployeeDetails getEmployee(@PathVariable("employeeId") int employeeId, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return employeeService.getEmployee(employeeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDetails create(@RequestBody EmployeeRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return employeeService.createEmployee(request);
    }

    @PutMapping("/{employeeId}")
    public EmployeeDetails update(@PathVariable("employeeId") int employeeId,
                                  @RequestBody EmployeeRequest request, HttpServletRequest http) {
        roleGuard.require(http, RoleGuard.ADMIN);
        return employeeService.updateEmployee(employeeId, request);
    }

    @DeleteMapping("/{employeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("employeeId") int employeeId, HttpServletRequest http) {
        EmployeeDto current = roleGuard.require(http, RoleGuard.ADMIN);
        employeeService.deleteEmployee(employeeId, current.id());
    }
}
