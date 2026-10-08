package org.example.smartbiobackend.unittests;

import org.example.smartbiobackend.model.Movie;
import org.example.smartbiobackend.model.MovieStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

// ISSUE-10: status beregnes ud fra premiereDate. Testen bruger en fast "i dag"-dato, så den ikke afhænger af kalenderen
class MovieStatusTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 8);

    private Movie movieWithPremiere(LocalDate premiereDate) {
        Movie movie = new Movie("Testfilm");
        movie.setPremiereDate(premiereDate);
        return movie;
    }

    @Test
    void premiereInTheFuture_IsComingSoon() {
        assertEquals(MovieStatus.COMING_SOON, movieWithPremiere(TODAY.plusDays(1)).getStatus(TODAY));
    }

    @Test
    void premiereToday_IsPremiere() {
        assertEquals(MovieStatus.PREMIERE, movieWithPremiere(TODAY).getStatus(TODAY));
    }

    @Test
    void lastDayOfPremierePeriod_IsStillPremiere() {
        assertEquals(MovieStatus.PREMIERE, movieWithPremiere(TODAY.minusDays(13)).getStatus(TODAY));
    }

    @Test
    void afterPremierePeriod_IsNowShowing() {
        assertEquals(MovieStatus.NOW_SHOWING, movieWithPremiere(TODAY.minusDays(14)).getStatus(TODAY));
    }

    @Test
    void noPremiereDate_IsNowShowing() {
        assertEquals(MovieStatus.NOW_SHOWING, movieWithPremiere(null).getStatus(TODAY));
    }

    @Test
    void inactiveMovie_IsArchivedNoMatterThePremiereDate() {
        Movie movie = movieWithPremiere(TODAY.plusDays(5));
        movie.setActive(false);

        assertEquals(MovieStatus.ARCHIVED, movie.getStatus(TODAY));
    }
}
