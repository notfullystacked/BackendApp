package org.example.smartbiobackend.model;

// Beregnes ud fra premiereDate og active (se Movie.getStatus). Gemmes ikke i databasen
public enum MovieStatus {
    COMING_SOON,   // premieren ligger i fremtiden
    PREMIERE,      // filmen har lige haft premiere (de første 14 dage)
    NOW_SHOWING,   // almindelig film på programmet
    ARCHIVED       // taget af programmet
}
