package org.example.smartbiobackend.repository;

import org.example.smartbiobackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail (String email);
}
