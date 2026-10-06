package org.example.smartbiobackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Auditorium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String auditoriumName;

    public Auditorium() {
    }
    public Auditorium(String auditoriumName) {
        this.auditoriumName = auditoriumName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuditoriumName() {
        return auditoriumName;
    }

    public void setAuditoriumName(String auditoriumName) {
        this.auditoriumName = auditoriumName;
    }


    @Override
    public String toString() {
        return "Auditorium{" +
                "id=" + id +
                ", auditoriumName='" + auditoriumName + '\'' +
                '}';
    }
}
