# Cinema Booking System

Sistema semplice per prenotare biglietti del cinema usando **Java 21** e **Spring Boot 3.x**.

## Cosa fa il sistema

Gestisce 3 cose principali:
- **Film** disponibili nel cinema
- **Proiezioni** (orari specifici dei film)
- **Prenotazioni** degli utenti

## Esempio pratico

```
Film: "Avatar 3"
├── Proiezione: Oggi 20:30 (200 posti, 50 disponibili)
│   ├── Mario prenota 2 posti
│   └── Lucia prenota 3 posti
└── Proiezione: Domani 18:00 (200 posti, 200 disponibili)
```

## Problema da risolvere

**Overbooking**: 1000 persone provano a prenotare simultaneamente gli ultimi 10 posti.

**Soluzione**: Virtual Threads + Redis locks per gestire la concorrenza.

## Tecnologie usate

- **Java 21**: Virtual Threads, Pattern Matching, Records
- **Spring Boot 3.x**: Web, Data JPA, Cache
- **PostgreSQL**: Database principale
- **Redis**: Cache e distributed locks
- **Docker**: Containerizzazione

## Avvio rapido

```bash
./scripts/start.sh
```

Oppure:

```bash
docker-compose up -d postgres redis
mvn clean package
docker-compose up -d
```

## API di test

```bash
# Lista film
curl http://localhost:8080/api/movies

# Prenota biglietti
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"screeningId": 1, "userEmail": "test@email.com", "numberOfSeats": 2}'
```

## Progressi del progetto

### ✅ Completato

**Entità JPA** (Movie, Screening, Booking)
- Movie: rappresenta un film
- Screening: rappresenta una proiezione specifica
- Booking: rappresenta una prenotazione utente

**Repository Layer**
- MovieRepository: query per film
- ScreeningRepository: query per proiezioni + lock atomici
- BookingRepository: query per prenotazioni + statistiche

**README**
- Spiegazione dominio applicativo
- Architettura semplificata

### 🚧 In corso

**Service Layer**
- Logica business con Virtual Threads
- Gestione concorrenza con Redis

**Controller Layer**
- REST API endpoints
- Validazione input

### 📋 Da fare

**Configurazione Redis**
- Cache configuration
- Distributed locks setup

**Script SQL**
- Creazione tabelle
- Dati di esempio

**Notification Service**
- Event listener
- Email sender (simulato)

**API Gateway**
- Routing configuration
- Load balancing

**Docker & Deployment**
- Dockerfile per ogni servizio
- Docker-compose completo

**Test**
- Unit test
- Integration test
- Load test per concorrenza

## Note tecniche

**Java 21 Features usate:**
- Virtual Threads per alta concorrenza
- Pattern Matching negli switch
- Records per DTOs immutabili

**Spring Features usate:**
- Spring Data JPA per persistenza
- Spring Cache per performance
- Spring Async per operazioni asincrone

**Architettura:**
- 2 microservizi (Movie + Notification)
- 1 API Gateway per routing
- Database condiviso (semplificazione)

---

**Prossimo step**: Implementare Service Layer con Virtual Threads