package org.example.smartbiobackend.model.dto;

import java.time.LocalDate;
import java.util.List;

// Det admin ser om en medarbejder. Adgangskoden (hashen) er aldrig med
public record EmployeeDetails(int id, String name, String username, String email, String phoneNr,
                              LocalDate birthday, List<String> roles) {
}
