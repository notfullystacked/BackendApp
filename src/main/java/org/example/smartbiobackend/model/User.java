package org.example.smartbiobackend.model;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private LocalDate birthday;

    private String password;

    @OneToMany
    Set<Role> roles;

}
