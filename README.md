# Smart bio 

# Dokumentation
Dokumentationen kan findes i docs/ mappen.
[tech stack](docs/techstack.md)

[Backend: oversigt, endpoints og forklaring](docs/backend-guide.md)

# Baggrund
- Lokal biograf med to sale
- 4 medarbejdere
- Salg af billetter, popcorn m.m

## Deployment til VM (Azure)

Hele systemet kører på en Ubuntu-VM med Docker. Det er de samme containere som lokalt:

```
Browser -> http://<VM-IP> -> nginx (frontend) -> /api/... -> Spring Boot (backend) -> MySQL
```

- Kun port 80 (hjemmesiden) og 22 (SSH) er åbne. MySQL er bundet til 127.0.0.1 og kan ikke nås udefra.
- nginx i frontenden sender /api videre til backenden, så browseren kun ser én adresse (ingen CORS-problemer).

Opret VM'en og start systemet (kræver Azure CLI og `az login`):

```
powershell -ExecutionPolicy Bypass -File .\deploy-vm.ps1 -Name kino-vm
```

Scriptet opretter VM'en, installerer Docker, kloner BackendApp og FrontendApp og kører
`docker compose --profile app up -d --build`. Til sidst skriver det adressen ud.

Hele systemet lokalt (FrontendApp skal ligge ved siden af BackendApp):

```
docker compose --profile app up -d --build     # frontend: http://localhost:3000
```

Slet VM'en igen når I er godkendt (sparer kredit):

```
az group delete -n kino-vm-rg --yes
```
