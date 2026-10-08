package org.example.smartbiobackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS: browseren blokerer som udgangspunkt fetch() fra én adresse (frontenden) til en anden (backenden).
// Her siger backenden, hvilke adresser der må kalde /api/**
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Sættes i application.properties (kino.cors.allowed-origins), så adressen kan ændres uden kodeændringer.
    // Flere adresser adskilles med komma
    @Value("${kino.cors.allowed-origins:http://localhost:63342}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                // Nødvendig for login: session-cookien sendes kun med, hvis frontenden bruger
                // fetch(url, { credentials: "include" })
                .allowCredentials(true);
    }
}
