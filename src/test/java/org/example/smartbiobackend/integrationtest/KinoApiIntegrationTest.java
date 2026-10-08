package org.example.smartbiobackend.integrationtest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integrationstest: starter hele applikationen (controller + service + repository + H2-database) med testdata
// fra InitData og kalder de rigtige endpoints. Unit-testene bruger mocks; her testes, at lagene virker sammen.
// @DirtiesContext giver hver test en frisk database, så testene ikke påvirker hinanden
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class KinoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Logger ind som medarbejder og returnerer sessionen, som de næste kald skal sende med
    private MockHttpSession loginAs(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"kode123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String movieJson(String name, LocalDate premiereDate) {
        return """
                {
                  "name": "%s",
                  "runTime": 7200,
                  "description": "Testfilm",
                  "imdbRating": 7.5,
                  "director": "Test Testesen",
                  "releaseYear": 2026,
                  "releaseDate": "2026-01-01",
                  "ageRestriction": 11,
                  "genre": "DRAMA",
                  "premiereDate": "%s"
                }
                """.formatted(name, premiereDate);
    }

    // ---------- ISSUE-12: se film ----------

    @Test
    void anyoneCanSeeTheMoviesOnTheProgram() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].name", hasItem("Jaws")));
    }

    @Test
    void movieThatDoesNotExist_Gives404WithErrorMessage() throws Exception {
        mockMvc.perform(get("/api/movies/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Film findes ikke: 9999"));
    }

    @Test
    void moviesCanBeFilteredByGenre() throws Exception {
        mockMvc.perform(get("/api/movies").param("genre", "HORROR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Jaws"));
    }

    // ---------- ISSUE-8: opret, ret og fjern film ----------

    @Test
    void createMovieRequiresLogin() throws Exception {
        mockMvc.perform(post("/api/movies").contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson("Ny film", LocalDate.now())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createMovieRequiresTheRightRole() throws Exception {
        MockHttpSession inspector = loginAs("emma");   // Inspector må ikke rette film

        mockMvc.perform(post("/api/movies").session(inspector).contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson("Ny film", LocalDate.now())))
                .andExpect(status().isForbidden());
    }

    @Test
    void movieEditorCanCreateUpdateAndDeleteAMovie() throws Exception {
        MockHttpSession editor = loginAs("oliver");

        // Opret: 201, og serveren vælger selv id'et
        mockMvc.perform(post("/api/movies").session(editor).contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson("Ny film", LocalDate.now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.active").value(true));

        // Ret: 200, og ændringen er gemt
        mockMvc.perform(put("/api/movies/6").session(editor).contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson("Nyt navn", LocalDate.now())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nyt navn"));
        mockMvc.perform(get("/api/movies/6"))
                .andExpect(jsonPath("$.name").value("Nyt navn"));

        // Slet: filmen har ingen forestillinger, så den kan slettes helt
        mockMvc.perform(delete("/api/movies/6").session(editor))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/movies/6"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMovieWithoutName_Gives400AndNot500() throws Exception {
        MockHttpSession editor = loginAs("oliver");

        mockMvc.perform(put("/api/movies/1").session(editor).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"runTime\": 7200}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Filmen skal have et navn"));
    }

    @Test
    void updateMovieThatDoesNotExist_Gives404() throws Exception {
        MockHttpSession editor = loginAs("oliver");

        mockMvc.perform(put("/api/movies/9999").session(editor).contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson("Findes ikke", LocalDate.now())))
                .andExpect(status().isNotFound());
    }

    @Test
    void movieWithShowingsCannotBeDeletedButCanBeTakenOffTheProgram() throws Exception {
        MockHttpSession editor = loginAs("oliver");

        mockMvc.perform(delete("/api/movies/1").session(editor))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/movies/1/deactivate").session(editor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        // Kunderne ser ikke længere filmen eller dens forestillinger, men medarbejderne kan stadig se den
        mockMvc.perform(get("/api/movies"))
                .andExpect(jsonPath("$[*].name", not(hasItem("Jaws"))));
        mockMvc.perform(get("/api/showings"))
                .andExpect(jsonPath("$[*].movieName", not(hasItem("Jaws"))));
        mockMvc.perform(get("/api/movies/all").session(editor))
                .andExpect(jsonPath("$[*].name", hasItem("Jaws")));
    }

    // ---------- ISSUE-9: salenes kapacitet ----------

    @Test
    void auditoriumsShowTheirCapacity() throws Exception {
        mockMvc.perform(get("/api/auditoriums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].capacity").value(240))    // 20 x 12
                .andExpect(jsonPath("$[1].capacity").value(400));   // 25 x 16
    }

    @Test
    void adminCanAddANewAuditoriumWithoutCodeChanges() throws Exception {
        MockHttpSession admin = loginAs("mads");

        mockMvc.perform(post("/api/auditoriums").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sal 3\",\"rowCount\":10,\"seatsPerRow\":8}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.capacity").value(80));

        mockMvc.perform(get("/api/auditoriums/3/seats"))
                .andExpect(jsonPath("$", hasSize(80)));
    }

    @Test
    void showingsShowHowManySeatsAreLeft() throws Exception {
        // Forestilling 2 (Jaws kl. 20 i Sal 1) har én booking fra InitData
        mockMvc.perform(get("/api/showings/2"))
                .andExpect(jsonPath("$.capacity").value(240))
                .andExpect(jsonPath("$.availableSeats").value(239));
    }

    // ---------- ISSUE-10: promovering ----------

    @Test
    void comingSoonAndPremiereListsFollowThePremiereDate() throws Exception {
        mockMvc.perform(get("/api/movies/coming-soon"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Spirited Away"))   // nærmeste premiere først
                .andExpect(jsonPath("$[0].status").value("COMING_SOON"));

        mockMvc.perform(get("/api/movies/premieres"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Mad Max: Fury Road"))
                .andExpect(jsonPath("$[0].status").value("PREMIERE"));
    }

    @Test
    void movieEditorCanPromoteAndUnpromoteAMovie() throws Exception {
        MockHttpSession editor = loginAs("oliver");

        mockMvc.perform(get("/api/movies/promoted"))
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(put("/api/movies/1/promote").session(editor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promoted").value(true));
        mockMvc.perform(get("/api/movies/promoted"))
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(put("/api/movies/1/unpromote").session(editor))
                .andExpect(jsonPath("$.promoted").value(false));
        mockMvc.perform(get("/api/movies/promoted"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void showingCannotBePlannedBeforeTheMoviesPremiere() throws Exception {
        MockHttpSession editor = loginAs("oliver");
        // Film 5 (Alien) har premiere om 25 dage. I morgen kl. 10 er Sal 2 ledig
        String startTime = LocalDate.now().plusDays(1).atTime(10, 0).toString();

        mockMvc.perform(post("/api/showings").session(editor).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":5,\"auditoriumId\":2,\"startTime\":\"" + startTime + "\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- Forestillinger ----------

    @Test
    void overlappingShowingsInTheSameAuditorium_AreRejected() throws Exception {
        MockHttpSession editor = loginAs("oliver");
        // Jaws vises i morgen kl. 17 i Sal 1 (forestilling 1)
        String startTime = LocalDate.now().plusDays(1).atTime(18, 0).toString();

        mockMvc.perform(post("/api/showings").session(editor).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":2,\"auditoriumId\":1,\"startTime\":\"" + startTime + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void movieEditorCanAddAnExtraShowing() throws Exception {
        MockHttpSession editor = loginAs("oliver");
        String startTime = LocalDate.now().plusDays(1).atTime(10, 0).toString();

        mockMvc.perform(post("/api/showings").session(editor).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":2,\"auditoriumId\":1,\"startTime\":\"" + startTime + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movieName").value("The Notebook"))
                .andExpect(jsonPath("$.availableSeats").value(240));
    }

    // ---------- Booking, betaling, check-in og annullering ----------

    @Test
    void fullBookingFlow() throws Exception {
        // 1. Gæst booker to sæder til forestilling 2: en voksen (120) og et barn (80)
        mockMvc.perform(post("/api/bookings/reserve").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "guestName": "Anna", "guestMail": "anna@mail.dk", "showingId": 2,
                                  "seats": [ { "seatId": 2, "ticketTypeId": 1 }, { "seatId": 3, "ticketTypeId": 2 } ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(2))
                .andExpect(jsonPath("$.seatCodes", hasSize(2)));

        // 2. Sæderne er nu optaget, og de kan ikke bookes igen
        mockMvc.perform(get("/api/showings/2"))
                .andExpect(jsonPath("$.availableSeats").value(237));
        mockMvc.perform(post("/api/bookings/reserve").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "guestName": "Bo", "guestMail": "bo@mail.dk", "showingId": 2,
                                  "seats": [ { "seatId": 2, "ticketTypeId": 1 } ] }
                                """))
                .andExpect(status().isConflict());

        // 3. Pris og detaljer
        mockMvc.perform(get("/api/bookings/2/price"))
                .andExpect(jsonPath("$.total").value(200));
        mockMvc.perform(get("/api/bookings/2"))
                .andExpect(jsonPath("$.movieName").value("Jaws"))
                .andExpect(jsonPath("$.totalPrice").value(200))
                .andExpect(jsonPath("$.paid").value(false));

        // 4. Billetkontrol afvises før betaling, og virker efter
        MockHttpSession inspector = loginAs("emma");
        mockMvc.perform(put("/api/bookings/2/check-in").session(inspector))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/bookings/2/pay"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/bookings/2/check-in").session(inspector))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(true));

        // 5. Ekspedienten kan finde bookingen på kundens email
        MockHttpSession clerk = loginAs("sofie");
        mockMvc.perform(get("/api/bookings").session(clerk).param("email", "anna@mail.dk"))
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());

        // 6. Annullering: forkert email afvises, rigtig email frigiver sæderne
        mockMvc.perform(delete("/api/bookings/2").param("email", "forkert@mail.dk"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/bookings/2").param("email", "anna@mail.dk"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/showings/2"))
                .andExpect(jsonPath("$.availableSeats").value(239));
        mockMvc.perform(get("/api/bookings/2"))
                .andExpect(status().isNotFound());
    }

    // ---------- Medarbejdere og login ----------

    @Test
    void loginWithWrongPassword_Gives401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"mads\",\"password\":\"forkert\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Forkert brugernavn eller adgangskode"));
    }

    @Test
    void adminCanAddAnEmployeeWhoCanThenLogIn() throws Exception {
        MockHttpSession admin = loginAs("mads");

        mockMvc.perform(post("/api/employees").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Noah", "username": "noah", "email": "noah@kino.dk",
                                  "password": "kode123", "roles": ["Clerk"] }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("Clerk"))
                .andExpect(jsonPath("$.password").doesNotExist());

        loginAs("noah");

        mockMvc.perform(get("/api/employees").session(admin))
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void onlyAdminCanSeeTheEmployees() throws Exception {
        mockMvc.perform(get("/api/employees").session(loginAs("sofie")))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCanRegisterAndLogIn() throws Exception {
        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Lucas", "email": "lucas@mail.dk", "password": "hemmelig123", "birthday": "2000-01-01" }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"lucas@mail.dk\",\"password\":\"hemmelig123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lucas"));

        mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"lucas@mail.dk\",\"password\":\"forkert\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- QR-kode, rengøring og kunde-session ----------

    @Test
    void ticketContainsAQrCodeAsBase64Png() throws Exception {
        mockMvc.perform(get("/api/bookings/1/ticket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieTitle").value("Jaws"))
                // Alle PNG-filer starter med de samme bytes, som i Base64 bliver til "iVBORw0KGgo"
                .andExpect(jsonPath("$.qrCode", startsWith("iVBORw0KGgo")));
    }

    @Test
    void staffCanMarkAnAuditoriumAsNeedingCleaningAndCleanAgain() throws Exception {
        MockHttpSession inspector = loginAs("emma");

        mockMvc.perform(get("/api/auditoriums/1"))
                .andExpect(jsonPath("$.cleaningStatus").value("CLEAN"));

        mockMvc.perform(put("/api/auditoriums/1/needs-cleaning").session(inspector))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cleaningStatus").value("NEEDS_CLEANING"));

        mockMvc.perform(put("/api/auditoriums/1/clean").session(inspector))
                .andExpect(jsonPath("$.cleaningStatus").value("CLEAN"));
    }

    @Test
    void cleaningStatusRequiresLoginAndTheRightRole() throws Exception {
        mockMvc.perform(put("/api/auditoriums/1/needs-cleaning"))
                .andExpect(status().isUnauthorized());

        // Oliver er Operator + MovieEditor og har ikke med rengøring at gøre
        mockMvc.perform(put("/api/auditoriums/1/needs-cleaning").session(loginAs("oliver")))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerProfileFollowsTheSession() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Lucas", "email": "lucas@mail.dk", "password": "hemmelig123", "birthday": "2000-01-01" }
                                """))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"lucas@mail.dk\",\"password\":\"hemmelig123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(get("/api/users/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lucas@mail.dk"));

        mockMvc.perform(post("/api/users/logout").session(session))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/profile").session(session))
                .andExpect(status().isUnauthorized());
    }
}
