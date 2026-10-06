package org.example.smartbiobackend.model.dto;

import java.time.LocalDate;

public record RegisterRequest (String name, String email, String password, LocalDate birthday){

}
