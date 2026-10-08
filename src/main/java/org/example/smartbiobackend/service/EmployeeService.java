package org.example.smartbiobackend.service;

import org.example.smartbiobackend.exception.NotFoundException;
import org.example.smartbiobackend.model.Employee;
import org.example.smartbiobackend.model.Role;
import org.example.smartbiobackend.model.dto.EmployeeDetails;
import org.example.smartbiobackend.model.dto.EmployeeRequest;
import org.example.smartbiobackend.repository.EmployeeRepository;
import org.example.smartbiobackend.repository.RoleRepository;
import org.example.smartbiobackend.security.RoleGuard;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Medarbejdere og roller ligger i databasen, så biografen kan ansætte flere uden kodeændringer
@Service
public class EmployeeService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<EmployeeDetails> getAll() {
        return employeeRepository.findAllByOrderByName().stream().map(this::toDetails).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDetails getEmployee(int employeeId) {
        return toDetails(findEmployee(employeeId));
    }

    @Transactional(readOnly = true)
    public List<String> getRoleNames() {
        return roleRepository.findAll().stream().map(Role::getRoleName).sorted().toList();
    }

    @Transactional
    public EmployeeDetails createEmployee(EmployeeRequest request) {
        validate(request);
        if (request.password() == null || request.password().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Adgangskoden skal være på mindst " + MIN_PASSWORD_LENGTH + " tegn");
        }
        checkUsernameAndEmailAreFree(request, 0);

        Employee employee = new Employee();
        applyRequest(employee, request);
        // Adgangskoden gemmes som BCrypt-hash, aldrig i klartekst
        employee.setPassword(passwordEncoder.encode(request.password()));
        return toDetails(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDetails updateEmployee(int employeeId, EmployeeRequest request) {
        Employee employee = findEmployee(employeeId);
        validate(request);
        checkUsernameAndEmailAreFree(request, employeeId);

        boolean wasAdmin = hasRole(employee, RoleGuard.ADMIN);
        boolean staysAdmin = request.roles() != null && request.roles().contains(RoleGuard.ADMIN);
        if (wasAdmin && !staysAdmin) {
            checkNotLastAdmin();
        }

        applyRequest(employee, request);
        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < MIN_PASSWORD_LENGTH) {
                throw new IllegalArgumentException("Adgangskoden skal være på mindst " + MIN_PASSWORD_LENGTH + " tegn");
            }
            employee.setPassword(passwordEncoder.encode(request.password()));
        }
        return toDetails(employeeRepository.save(employee));
    }

    // currentEmployeeId er den, der er logget ind: man kan ikke slette sig selv
    @Transactional
    public void deleteEmployee(int employeeId, int currentEmployeeId) {
        Employee employee = findEmployee(employeeId);
        if (employeeId == currentEmployeeId) {
            throw new IllegalStateException("Du kan ikke slette dig selv");
        }
        if (hasRole(employee, RoleGuard.ADMIN)) {
            checkNotLastAdmin();
        }
        employeeRepository.delete(employee);
    }

    // Opret en ny rolle, fx "Cleaner". Findes den allerede, sker der ikke noget
    @Transactional
    public List<String> createRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Rollen skal have et navn");
        }
        if (!roleRepository.existsByRoleName(roleName.trim())) {
            roleRepository.save(new Role(roleName.trim()));
        }
        return getRoleNames();
    }

    // ---------- Hjælpemetoder ----------

    private void validate(EmployeeRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Medarbejderen skal have et navn");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Medarbejderen skal have et brugernavn");
        }
        if (request.email() == null || !request.email().contains("@")) {
            throw new IllegalArgumentException("Medarbejderen skal have en gyldig email");
        }
    }

    // ownId er id'et på den medarbejder, vi er ved at rette (0 ved oprettelse), så man ikke "kolliderer" med sig selv
    private void checkUsernameAndEmailAreFree(EmployeeRequest request, int ownId) {
        Optional<Employee> sameUsername = employeeRepository.findByUsername(request.username().trim());
        if (sameUsername.isPresent() && sameUsername.get().getId() != ownId) {
            throw new IllegalStateException("Brugernavnet er allerede i brug");
        }
        Optional<Employee> sameEmail = employeeRepository.findByEmail(request.email().trim());
        if (sameEmail.isPresent() && sameEmail.get().getId() != ownId) {
            throw new IllegalStateException("Emailen er allerede i brug");
        }
    }

    // Der skal altid være mindst én admin, ellers kan ingen længere administrere systemet
    private void checkNotLastAdmin() {
        long admins = employeeRepository.findAll().stream()
                .filter(employee -> hasRole(employee, RoleGuard.ADMIN))
                .count();
        if (admins <= 1) {
            throw new IllegalStateException("Systemet skal have mindst én Admin");
        }
    }

    private boolean hasRole(Employee employee, String roleName) {
        return employee.getRoles().stream().anyMatch(role -> role.getRoleName().equals(roleName));
    }

    private void applyRequest(Employee employee, EmployeeRequest request) {
        employee.setName(request.name().trim());
        employee.setUsername(request.username().trim());
        employee.setEmail(request.email().trim());
        employee.setPhoneNr(request.phoneNr());
        employee.setBirthday(request.birthday());

        Set<Role> roles = new HashSet<>();
        if (request.roles() != null) {
            for (String roleName : request.roles()) {
                Role role = roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Rollen findes ikke: " + roleName));
                roles.add(role);
            }
        }
        employee.setRoles(roles);
    }

    private Employee findEmployee(int employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Medarbejder findes ikke: " + employeeId));
    }

    private EmployeeDetails toDetails(Employee employee) {
        List<String> roles = employee.getRoles().stream().map(Role::getRoleName).sorted().toList();
        return new EmployeeDetails(employee.getId(), employee.getName(), employee.getUsername(),
                employee.getEmail(), employee.getPhoneNr(), employee.getBirthday(), roles);
    }
}
