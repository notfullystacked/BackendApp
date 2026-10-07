# Ændringer: sale, sæder, film, planlægning og booking

## Rettede fejl
- Sædeopslag brugte forestillingens id i stedet for salens, og `findBySeatCode` filtrerede ikke på sal.
- Samme sæde kunne bookes to gange. Nu tjekkes det i `BookingService`, og databasen har en unik regel på (forestilling, sæde) i `BookingSeat`.
- `Booking.seat` er fjernet. Sæder ligger kun i `BookingSeat`, så én booking kan have flere sæder.
- `ShowingController` lå på `/showings` (uden for CORS). Nu `/api/showings`, og ændringer kræver login.
- `User.roles` var mappet forkert (`@OneToMany`). Fjernet, da kunder ikke har roller.
- Fejl gav 500. Nu: forkert input = 400, konflikt (fx optaget sæde) = 409 (`GlobalExceptionHandler`).

## Nyt
- `Auditorium` har `rowCount` og `seatsPerRow`. `AuditoriumService.createAuditorium` opretter sæderne automatisk.
- `Seat` har `seatRow` og `seatNumber`; `seatCode` udfyldes som "række-sæde", fx "3-12".
- `Movie` har `genre` (enum `Genre`) og `active` (tag af programmet før tid).
- Planlægning: opret/ret/aflys forestillinger. Overlap i samme sal (inkl. 15 min rengøring) afvises.
- Sædeoversigt pr. forestilling, program 3 måneder frem, liste over film og billettyper.
- `RoleGuard` tjekker medarbejder-login og roller (Admin må alt).
- Booking tager nu en liste af sæder, hver med sin billettype.

## Endpoints
| Metode | Sti | Hvem |
|---|---|---|
| GET | /api/movies | alle |
| GET | /api/movies/all | medarbejder |
| POST/PUT | /api/movies, /api/movies/{id}, /api/movies/{id}/deactivate | MovieEditor |
| GET | /api/showings, /api/showings/{id}, /api/showings/{id}/seats | alle |
| POST/PUT | /api/showings, /api/showings/{id}, /api/showings/{id}/cancel | MovieEditor |
| GET | /api/auditoriums | alle |
| POST | /api/auditoriums | Admin |
| GET | /api/ticket-types | alle |
| POST | /api/bookings/reserve | alle (også ekspedient ved telefonbooking) |

Booking-body:
```json
{ "guestName": "Anna", "guestMail": "anna@mail.dk", "showingId": 2,
  "seats": [ { "seatId": 2, "ticketTypeId": 1 }, { "seatId": 3, "ticketTypeId": 2 } ] }
```

Se `config/httprequests.http` for eksempler på alle kald. Medarbejder-login: mads / kode123.
