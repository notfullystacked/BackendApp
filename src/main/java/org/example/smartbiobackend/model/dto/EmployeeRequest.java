package org.example.smartbiobackend.model.dto;

import java.time.LocalDate;
import java.util.List;

// Opret/ret medarbejder. roles er rollenavne, fx ["Clerk", "MovieEditor"].
// password er påkrævet ved oprettelse. Ved rettelse betyder null/tom: behold den gamle adgangskode
public record EmployeeRequest(String name, String username, String email, String password,
                              String phoneNr, LocalDate birthday, List<String> roles) {
}
