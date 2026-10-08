package org.example.smartbiobackend.model.dto;

import org.example.smartbiobackend.model.Genre;

import java.time.LocalDate;

// Det klienten må sende ved opret/ret film. id, active og promoted er med vilje ikke med:
// dem styrer serveren selv (via /deactivate, /promote osv.), så en klient ikke kan sætte dem i en POST/PUT.
// runTime er i sekunder (som i Movie).
// Tallene er Integer/Double og ikke int/double, så et felt, der mangler i JSON'en, bliver til null.
// Så kan MovieService selv give en præcis fejlbesked (fx "Filmen skal have en spilletid") i stedet for en JSON-fejl
public record MovieRequest(String name, Integer runTime, String description, Double imdbRating, String director,
                           Integer releaseYear, LocalDate releaseDate, Integer ageRestriction, Genre genre,
                           LocalDate premiereDate, String posterUrl) {
}
