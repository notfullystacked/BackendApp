# Kino-backend: oversigt og forklaring

Denne fil forklarer, hvad backenden kan, hvordan den er bygget, og hvorfor. Den er skrevet, så du kan bruge den,
når du skal forklare koden til gruppen, en underviser eller en censor.

Kør applikationen fra IntelliJ (`SmartBioBackendApplication`). Alle kald ligger klar i
`src/main/java/org/example/smartbiobackend/config/httprequests.http`.

## 1. Arkitektur

```
Browser / Postman
      |  HTTP + JSON
Controller   tager imod requesten, tjekker login/rolle, kalder service
      |
Service      forretningsregler og validering (fx "en sal må ikke dobbeltbookes")
      |
Repository   Spring Data JPA: laver SQL ud fra metodenavne
      |
Database     H2 i hukommelsen (standard) eller MySQL (profilen "docker")
```

| Pakke | Indhold |
|---|---|
| `controller` | Ét `@RestController` pr. område: film, forestillinger, sale, bookinger, medarbejdere, login |
| `service` | Reglerne. Her kastes exceptions, når noget er forkert |
| `repository` | Interfaces der udvider `JpaRepository` |
| `model` | `@Entity`-klasser (tabeller) og enums |
| `model.dto` / `dto` | Records til det, der sendes ind og ud som JSON |
| `security` | `RoleGuard`: tjekker session og roller |
| `exception` | Egne exceptions og `GlobalExceptionHandler` |
| `config` | CORS (`WebConfig`), BCrypt (`PasswordConfig`), testdata (`InitData`) |

## 2. Dine fire user stories

### ISSUE-12: se alle film
- `GET /api/movies` giver film på programmet. Kan filtreres med `?genre=HORROR&search=jaws`.
- `GET /api/movies/{id}` giver én film eller 404.
- Koden: `MovieController.getActiveMovies` -> `MovieService.getActiveMovies` -> `MovieRepository.findByActiveTrueOrderByName`.

### ISSUE-8: opret, ret og fjern film
- `POST /api/movies` (201), `PUT /api/movies/{id}` (200), `PUT /api/movies/{id}/deactivate`, `DELETE /api/movies/{id}`.
- Kræver login med rollen `MovieEditor` (eller `Admin`).
- Body'en er en `MovieRequest`-record, ikke hele `Movie`-entiteten. Derfor kan klienten ikke selv sætte `id`,
  `active` eller `promoted`.
- "Fjern" findes i to udgaver:
  - **Soft delete** (`/deactivate`): `active` sættes til `false`, og filmens kommende forestillinger aflyses.
    Rækken bliver i databasen, fordi gamle forestillinger og bookinger peger på filmen.
  - **Rigtig sletning** (`DELETE`): kun tilladt, hvis filmen ingen forestillinger har. Ellers 409.

De to spørgsmål, jeg stillede dig sidst:
1. *Skal PUT have `@ResponseStatus(HttpStatus.CREATED)`?* Nej. 201 betyder "noget nyt er oprettet". En rettelse
   opretter ikke noget, så standardkoden 200 er rigtig.
2. *Body'en siger `"id": 99`, URL'en siger `/api/movies/1`. Hvilken film rettes?* Film 1. Servicen henter filmen med
   id'et fra URL'en og kopierer felterne over. I den færdige udgave findes `id` slet ikke i `MovieRequest`.

### ISSUE-9: kapacitet i alle sale
- `GET /api/auditoriums` giver hver sal med `rowCount`, `seatsPerRow` og `capacity`.
- `capacity` er ikke en kolonne. Den beregnes i `Auditorium.getCapacity()` som `rowCount * seatsPerRow`, og Jackson
  tager den med i JSON'en, fordi det er en getter.
- Opgavens hint (flere sale og andre størrelser uden kodeændringer): `POST /api/auditoriums` opretter en ny sal,
  og `AuditoriumService` opretter selv alle sæderne. `PUT /api/auditoriums/{id}` ændrer størrelsen.
- Hver forestilling viser også `capacity` og `availableSeats` (kapacitet minus bookede sæder).

### ISSUE-10: promovér film
Jeres idé var premiere og "coming soon", så nye film får en god start. Det er løst med to felter på `Movie`:

| Felt | Betydning |
|---|---|
| `premiereDate` | Første dag filmen vises i Kino |
| `promoted` | Filmen fremhæves på forsiden (sættes af en MovieEditor) |

Ud fra dem beregner `Movie.getStatus()` en status, som kommer med i JSON'en:

| Status | Hvornår |
|---|---|
| `COMING_SOON` | `premiereDate` ligger efter i dag |
| `PREMIERE` | Premieren var for 0-13 dage siden (`Movie.PREMIERE_DAYS = 14`) |
| `NOW_SHOWING` | Alt andet på programmet |
| `ARCHIVED` | Taget af programmet (`active = false`) |

Endpoints: `GET /api/movies/coming-soon`, `/premieres`, `/promoted` og `PUT /api/movies/{id}/promote` og `/unpromote`.
En forestilling kan ikke planlægges før filmens premiere, men billetter kan godt sælges før (forsalg).

**Det skal gruppen tage stilling til:** Det er min fortolkning af "promote". Hvis I hellere vil have rabatter eller
noget andet, er det `MovieService` (afsnittet "Promovering") og de to felter, der skal ændres.

## 3. Resten af systemet

| Område | Hvad |
|---|---|
| Forestillinger | Program 3 måneder frem, ekstra forestillinger, ret, aflys. Overlap i samme sal afvises (spilletid + 15 min. rengøring) |
| Sæder | Sædeoversigt pr. forestilling med ledige og optagne sæder |
| Booking | Flere sæder pr. booking, hver med sin billettype. Gæst, registreret kunde eller telefonbooking via ekspedient |
| Betaling og billet | Betal, hent pris, hent billet |
| Annullering | `DELETE /api/bookings/{id}`. Kunden skal oplyse bookingens email, en medarbejder behøver ikke |
| Billetkontrol | `PUT /api/bookings/{id}/check-in`. Kræver betalt booking, og virker kun én gang |
| Medarbejdere | Admin kan oprette, rette og slette medarbejdere og roller. Adgangskoder gemmes som BCrypt-hash |
| Billettyper | Priser ligger i databasen og kan ændres af Admin |
| Sale | Kan lukkes og åbnes igen (fx ved ombygning) |
| Rengøring | Personalet markerer en sal som `NEEDS_CLEANING` og `CLEAN` (Damolas feature fra `main`) |
| QR-kode | Billetten indeholder en QR-kode som Base64-PNG i feltet `qrCode` (Damolas feature fra `main`) |
| Kunde-session | Kunde-login gemmes i sessionen, og `GET /api/users/profile` viser, hvem der er logget ind |

### QR-koden på billetten
`GET /api/bookings/{id}/ticket` har feltet `qrCode`. Det er et PNG-billede kodet som Base64-tekst, og indholdet
af koden er `TICKET-<bookingId>`. I frontenden vises det sådan:

```js
img.src = "data:image/png;base64," + ticket.qrCode;
```

Koden laves af `QrCodeService` med biblioteket ZXing (`com.google.zxing:core` i `pom.xml`).

### Login og roller
Medarbejdere logger ind med `POST /api/auth/login`. Serveren gemmer medarbejderens id i en **session**, og browseren
får en cookie (`JSESSIONID`). Ved hvert beskyttet kald slår `RoleGuard.require(...)` medarbejderen op og tjekker rollen.

| Rolle | Må |
|---|---|
| Alle medarbejdere | Se alle film og forestillinger (også aflyste) og alle bookinger |
| `Admin` | Alt |
| `MovieEditor` | Oprette, rette og fjerne film og forestillinger, promovere film |
| `Clerk` | Billetkontrol, rengøringsstatus (og telefonbookinger, som er åbne for alle) |
| `Inspector` | Billetkontrol, rengøringsstatus |
| `Operator` | Ingen ekstra rettigheder endnu |

Testbrugere (alle med koden `kode123`): `mads` (Admin + Clerk), `sofie` (Clerk), `oliver` (Operator + MovieEditor),
`emma` (Inspector).

Fra frontenden skal `fetch` have `credentials: "include"`, ellers sendes cookien ikke med:

```js
fetch("http://localhost:8080/api/auth/me", { credentials: "include" })
```

### Fejl og statuskoder
Alle fejl har samme form: `{"error": "besked"}`. Det styres ét sted, i `GlobalExceptionHandler`:

| Exception | Statuskode | Eksempel |
|---|---|---|
| `IllegalArgumentException` | 400 | Film uden navn |
| `UnauthorizedException` | 401 | Ikke logget ind, forkert adgangskode |
| `ForbiddenException` | 403 | Logget ind, men mangler rollen |
| `NotFoundException` | 404 | `GET /api/movies/9999` |
| `IllegalStateException` | 409 | Sædet er allerede booket, salen er optaget |

## 4. Alle endpoints

| Metode | Sti | Hvem |
|---|---|---|
| POST | `/api/auth/login`, `/api/auth/logout` | alle |
| GET | `/api/auth/me` | medarbejder |
| GET | `/api/movies`, `/api/movies/{id}`, `/api/movies/{id}/showings`, `/api/movies/genres` | alle |
| GET | `/api/movies/promoted`, `/api/movies/coming-soon`, `/api/movies/premieres` | alle |
| GET | `/api/movies/all` | medarbejder |
| POST, PUT, DELETE | `/api/movies`, `/api/movies/{id}` | MovieEditor |
| PUT | `/api/movies/{id}/deactivate`, `/activate`, `/promote`, `/unpromote` | MovieEditor |
| GET | `/api/showings`, `/api/showings/{id}`, `/api/showings/{id}/seats` | alle |
| GET | `/api/showings/all` | medarbejder |
| POST, PUT | `/api/showings`, `/api/showings/{id}`, `/api/showings/{id}/cancel` | MovieEditor |
| GET | `/api/auditoriums`, `/api/auditoriums/{id}`, `/api/auditoriums/{id}/seats` | alle |
| POST, PUT | `/api/auditoriums`, `/api/auditoriums/{id}`, `/{id}/close`, `/{id}/open` | Admin |
| PUT | `/api/auditoriums/{id}/needs-cleaning`, `/api/auditoriums/{id}/clean` | Inspector, Clerk |
| GET | `/api/ticket-types` | alle |
| POST, PUT | `/api/ticket-types`, `/api/ticket-types/{id}` | Admin |
| POST | `/api/bookings/reserve`, `/api/bookings/{id}/pay` | alle |
| GET | `/api/bookings/{id}`, `/{id}/price`, `/{id}/ticket`, `/api/bookings/showing/{id}/seats` | alle |
| DELETE | `/api/bookings/{id}?email=...` | kunde med rigtig email, eller medarbejder |
| GET | `/api/bookings` (`?showingId=` eller `?email=`) | medarbejder |
| PUT | `/api/bookings/{id}/check-in` | Inspector, Clerk |
| POST | `/api/users/register`, `/api/users/login`, `/api/users/logout` | alle |
| GET | `/api/users/profile` | kunde, der er logget ind |
| GET | `/api/users/{id}/bookings` | alle |
| GET, POST, PUT, DELETE | `/api/employees`, `/api/employees/{id}`, `/api/employees/roles` | Admin |

## 5. Hvor koden kommer fra

Branchen `kino-samlet-backend` samler tre ting:

1. `Færdit-Backend`: sale med automatiske sæder, forestillinger, booking af flere sæder, roller.
2. `kino-faerdig-backend`: de fire user stories gjort færdige, fejlkoder, medarbejdere, tests.
3. Fra `main` (Damola, 8. oktober): QR-kode på billetten, rengøringsstatus og kunde-session med `/profile`.

`main` er flettet ind, så branchen kan merges til `main` uden konflikter. Hvor `main` og denne branch havde bygget
det samme på hver sin måde, er det denne branchs udgave, der gælder. Det betyder, at nogle kald fra `main` har
skiftet form:

| På `main` før | Nu |
|---|---|
| `GET /movies` | `GET /api/movies` (kun film på programmet) eller `GET /api/movies/all` (medarbejder) |
| `POST /movies`, `PUT /movies/{id}` uden login | `POST /api/movies`, `PUT /api/movies/{id}`, kræver MovieEditor |
| `PUT /movies/{id}/remove-from-program` | `PUT /api/movies/{id}/deactivate` |
| `PUT /movies/{id}/promote` | `PUT /api/movies/{id}/promote` (og `/unpromote`) |
| Film har `category` (fri tekst) | Film har `genre` (enum, se `GET /api/movies/genres`) |
| `GET /showings/plan` | `GET /api/showings` (evt. `?date=` og `?movieId=`) |
| `POST /showings?movieId=..&date=..` | `POST /api/showings` og `PUT /api/showings/{id}` med JSON-body, kræver MovieEditor |
| `GET /auditoriums` | `GET /api/auditoriums` |
| `POST /auditoriums?name=..&rowCount=..` | `POST /api/auditoriums` med JSON-body, kræver Admin |
| `PUT /auditoriums/{id}/needs-cleaning`, `/clean` | Samme under `/api/auditoriums/...` |
| `POST /api/bookings/reserve` med `seatCode`, kun medarbejdere | Samme sti, men med en liste `seats` (sæde-id + billettype), åben for alle |
| `GET /api/bookings/showing/{id}` | `GET /api/bookings?showingId={id}` |
| Rollen `EMPLOYEE` (bruger `employee`) | Rollerne Admin, MovieEditor, Clerk, Inspector, Operator (fx `mads` / `kode123`) |

Stierne er flyttet ind under `/api`, fordi CORS kun er slået til for `/api/**`. Uden det blokerer browseren
frontendens kald.

Andre ændringer i forhold til `Færdit-Backend`:

- "Findes ikke" giver 404 i stedet for 400 (`NotFoundException`), og 401/403 har en JSON-besked.
- Film oprettes og rettes med `MovieRequest`, og `PUT` validerer ligesom `POST`.
- `data.sql` er slettet. Roller og medarbejdere oprettes i `InitData`, så det også virker på MySQL.
- `ShowingResponse` har fået `endTime`, `capacity` og `availableSeats`.
- CORS-adressen står i `application.properties` (`kino.cors.allowed-origins`).
- Nyt: `Dockerfile` og en `app`-service i `compose.yaml`.

## 6. Test

`mvn test` kører 109 tests uden database-server:

- Unit-tests med Mockito for hver service (fx `MovieServiceTest`, `AuditoriumServiceTest`, `EmployeeServiceTest`).
- `MovieStatusTest` for premiere/coming soon-beregningen.
- `KinoApiIntegrationTest` starter hele applikationen på H2 og kalder de rigtige endpoints, inkl. login og roller.

`SmartBioBackendApplicationTests` bruger profilen `docker` og kræver, at MySQL kører (`docker compose up -d`).
Den kører i GitHub Actions.

## 7. Kendte begrænsninger

Sig dem selv til eksamen, før censor finder dem.

- **Booking-endpoints er åbne.** Kunder har en session (`/api/users/profile`), men pris, billet og betaling
  tjekker den ikke og er åbne for alle, der kender booking-id'et. Gæster uden konto skal også kunne se deres
  billet, så login alene løser det ikke. I et rigtigt system ville man bruge Spring Security.
- **Betaling er kun et flag** (`paid`). Der er ingen betalingsudbyder.
- **Prisændringer slår igennem på gamle bookinger**, fordi en booking peger på billettypen og ikke gemmer prisen.
- **`ddl-auto=create-drop`**: databasen nulstilles ved hver start. Fint til udvikling, ikke til drift.
- **Ikke afprøvet af mig:** MySQL-profilen og Docker-filerne. Jeg kunne kun køre H2. Tabellerne er genereret
  med MySQL-dialekten og ser rigtige ud, men kør `docker compose up -d` og test selv.
- Der er stadig to `LoginRequest`-klasser (`dto` til medarbejdere, `model.dto` til kunder).

## 8. Spørgsmål en censor kunne stille

1. **Hvorfor en service mellem controller og repository?** Reglerne ligger ét sted og kan unit-testes uden HTTP
   og database. Controlleren handler kun om HTTP.
2. **Hvorfor DTO'er i stedet for at sende entiteten?** Så klienten kun kan sætte tilladte felter, og så
   adgangskoder aldrig sendes ud (`EmployeeDetails` har intet password-felt).
3. **PUT eller PATCH?** PUT erstatter hele ressourcen, så et felt, der mangler, bliver nulstillet.
   PATCH ville kun ændre de felter, der sendes.
4. **Hvorfor soft delete?** Fremmednøgler: forestillinger og bookinger peger på filmen. Historikken bevares.
5. **Hvordan undgår I, at to kunder får samme sæde?** Servicen tjekker først, og databasen har en unik regel på
   (forestilling, sæde) i `BookingSeat`. Reglen fanger det tilfælde, hvor to requests kommer på præcis samme tid.
6. **Hvorfor er kapacitet og status ikke kolonner?** De kan beregnes ud fra andre felter. Gemte man dem, kunne
   de komme ud af trit med de felter, de afhænger af.
7. **Hvad er forskellen på 401 og 403?** 401: vi ved ikke, hvem du er. 403: vi ved det, men du må ikke.
8. **Hvordan gemmes adgangskoder?** Som BCrypt-hash med salt. `passwordEncoder.matches` sammenligner uden at
   hashen kan regnes tilbage til koden.
9. **Hvad betyder `@Transactional`?** Alt i metoden lykkes, eller intet gemmes. Fx oprettes en sal og dens sæder samlet.
10. **Hvordan håndterer systemet flere sale og medarbejdere uden kodeændringer?** Sale, sæder, roller, medarbejdere
    og billetpriser er rækker i databasen og oprettes via API'et.
