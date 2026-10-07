package org.example.smartbiobackend.model;


import jakarta.persistence.*;

import java.time.LocalDate;

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

    public User() {

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

    public int getId() {
        return id;
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
                "birthday=" + birthday +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
