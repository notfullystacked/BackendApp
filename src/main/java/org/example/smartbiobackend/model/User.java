package org.example.smartbiobackend.model;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users") // I had to rename it because SQL is confusing and I think it has a keyword called User.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private LocalDate birthday;

    @Column(nullable = true)
    private String password;

    @OneToMany
    @JoinColumn
    Set<Role> roles = new HashSet<>();

    /***
    Constructor without password for initial creation
     */
    public User(int userId, String name, String email, LocalDate birthday) {
        this.id = userId;
        this.name = name;
        this.email = email;
        this.birthday = birthday;
    }

    public User(String name, String email, LocalDate birthday) {
        this.name = name;
        this.email = email;
        this.birthday = birthday;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "roles=" + roles +
                ", birthday=" + birthday +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
