package org.example.smartbiobackend.servicetest;

import org.example.smartbiobackend.model.Employee;
import org.example.smartbiobackend.model.Role;
import org.example.smartbiobackend.model.dto.EmployeeDetails;
import org.example.smartbiobackend.model.dto.EmployeeRequest;
import org.example.smartbiobackend.repository.EmployeeRepository;
import org.example.smartbiobackend.repository.RoleRepository;
import org.example.smartbiobackend.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private RoleRepository roleRepository;

    // En rigtig encoder, så testen kan tjekke, at adgangskoden faktisk bliver hashet
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, roleRepository, passwordEncoder);
        lenient().when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(roleRepository.findByRoleName("Clerk")).thenReturn(Optional.of(new Role("Clerk")));
        lenient().when(roleRepository.findByRoleName("Admin")).thenReturn(Optional.of(new Role("Admin")));
    }

    private EmployeeRequest request(String username, String password, List<String> roles) {
        return new EmployeeRequest("Sofie", username, username + "@kino.dk", password, "12345678", null, roles);
    }

    private Employee employee(int id, String username, String... roleNames) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName(username);
        employee.setUsername(username);
        employee.setEmail(username + "@kino.dk");
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(new Role(roleName));
        }
        employee.setRoles(roles);
        return employee;
    }

    @Test
    void createEmployeeHashesThePasswordAndSetsRoles() {
        EmployeeDetails created = employeeService.createEmployee(request("sofie", "hemmelig123", List.of("Clerk")));

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        String storedPassword = captor.getValue().getPassword();

        assertNotEquals("hemmelig123", storedPassword);                       // ikke gemt i klartekst
        assertTrue(passwordEncoder.matches("hemmelig123", storedPassword));   // men hashen passer til koden
        assertEquals(List.of("Clerk"), created.roles());
    }

    @Test
    void createEmployeeWithShortPassword_IsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(request("sofie", "123", List.of("Clerk"))));
    }

    @Test
    void createEmployeeWithUsernameThatIsTaken_IsRejected() {
        when(employeeRepository.findByUsername("mads")).thenReturn(Optional.of(employee(1, "mads", "Admin")));

        assertThrows(IllegalStateException.class,
                () -> employeeService.createEmployee(request("mads", "hemmelig123", List.of("Clerk"))));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployeeWithUnknownRole_IsRejected() {
        when(roleRepository.findByRoleName("Pilot")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(request("sofie", "hemmelig123", List.of("Pilot"))));
    }

    @Test
    void updateEmployeeWithoutPasswordKeepsTheOldPassword() {
        Employee sofie = employee(2, "sofie", "Clerk");
        sofie.setPassword("gammelHash");
        when(employeeRepository.findById(2)).thenReturn(Optional.of(sofie));
        when(employeeRepository.findByUsername("sofie")).thenReturn(Optional.of(sofie));

        employeeService.updateEmployee(2, request("sofie", null, List.of("Clerk")));

        assertEquals("gammelHash", sofie.getPassword());
    }

    @Test
    void employeeCannotDeleteThemself() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee(1, "mads", "Admin")));

        assertThrows(IllegalStateException.class, () -> employeeService.deleteEmployee(1, 1));
        verify(employeeRepository, never()).delete(any());
    }

    @Test
    void theLastAdminCannotBeDeleted() {
        Employee onlyAdmin = employee(1, "mads", "Admin");
        when(employeeRepository.findById(1)).thenReturn(Optional.of(onlyAdmin));
        when(employeeRepository.findAll()).thenReturn(List.of(onlyAdmin, employee(2, "sofie", "Clerk")));

        assertThrows(IllegalStateException.class, () -> employeeService.deleteEmployee(1, 2));
        verify(employeeRepository, never()).delete(any());
    }

    @Test
    void theLastAdminCannotLoseTheAdminRole() {
        Employee onlyAdmin = employee(1, "mads", "Admin");
        when(employeeRepository.findById(1)).thenReturn(Optional.of(onlyAdmin));
        when(employeeRepository.findAll()).thenReturn(List.of(onlyAdmin));

        assertThrows(IllegalStateException.class,
                () -> employeeService.updateEmployee(1, request("mads", null, List.of("Clerk"))));
    }

    @Test
    void deleteEmployeeRemovesThem() {
        Employee sofie = employee(2, "sofie", "Clerk");
        when(employeeRepository.findById(2)).thenReturn(Optional.of(sofie));

        employeeService.deleteEmployee(2, 1);

        verify(employeeRepository).delete(sofie);
    }
}
