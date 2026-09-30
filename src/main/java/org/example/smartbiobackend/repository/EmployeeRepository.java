package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByUsername(String username);
}