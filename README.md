# Cinema Booking System - Java 21 Mission-Critical

Sistema di prenotazione cinema progettato per **alta concorrenza** e **bassa latenza**, implementato con **Java 21** e **Spring Boot 3.x** per dimostrare le nuove feature del linguaggio in un contesto enterprise.

## Problema Business Risolto

**Scenario**: 1000 persone provano simultaneamente a prenotare gli ultimi 10 posti per una proiezione.

**Soluzione**: Virtual Threads + Distributed Locking + Atomic Operations per gestire concorrenza senza overbooking.

## Architettura Java 21 Features

### Virtual Threads per Alta Concorrenza
```java
@Async("virtualThreadExecutor")
public CompletableFuture<BookingResponse> createBookingAsync(BookingRequest request) {
    // Virtual Thread gestisce migliaia di richieste simultanee
    return CompletableFuture.supplyAsync(() -> createBooking(request));
}

@Bean("virtualThreadExecutor")
public Executor virtualThreadExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor(); // Java 21
}
```

### Pattern Matching con Switch Expressions
```java
public String getStatusMessage(Booking booking) {
    return switch (booking.getStatus()) {
        case PENDING -> "Prenotazione in elaborazione per %s".formatted(movieTitle);
        case CONFIRMED -> "Confermato! %d posti per %s".formatted(numberOfSeats, movieTitle);
        case CANCELLED -> "Prenotazione annullata per %s".formatted(movieTitle);
        case EXPIRED -> "Prenotazione scaduta per %s".formatted(movieTitle);
    };
}
```

### Records Immutabili per DTOs
```java
public record BookingRequest(
        @NotNull @Min(1) Long screeningId,
        @NotBlank @Email String userEmail,
        @NotNull @Min(1) @Max(10) Integer numberOfSeats
) {
    // Compact constructor per validazione
    public BookingRequest {
        if (userEmail != null) {
            userEmail = userEmail.trim().toLowerCase();
        }
    }
}
```

### Text Blocks per Query Complesse
```java
private void releaseLock(String lockKey, String lockToken) {
    String luaScript = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;
    // Script Redis per rilascio atomico
}
```

## Struttura del Progetto

```
src/main/java/com/cinema/movie/
├── MovieServiceApplication.java          # Entry point con Virtual Threads
├── config/
│   ├── VirtualThreadConfig.java          # Configurazione Java 21 Virtual Threads
│   └── RedisConfig.java                  # Redis per distributed locking
├── controller/
│   ├── BookingController.java            # REST API endpoints
│   ├── MovieController.java              # Film e proiezioni API
│   └── GlobalExceptionHandler.java       # Error handling centralizzato
├── dto/
│   ├── BookingRequest.java               # Record con validazione
│   ├── BookingResponse.java              # Record per response
│   ├── MovieResponse.java                # DTOs immutabili
│   └── ScreeningResponse.java            
├── entity/
│   ├── Movie.java                        # JPA Entity (solo data holder)
│   ├── Screening.java                    # Entity semplificata
│   ├── Booking.java                      # Nessuna business logic
│   └── BookingStatus.java                # Enum per stati
├── repository/
│   ├── MovieRepository.java              # Spring Data JPA
│   ├── ScreeningRepository.java          # Query atomiche
│   └── BookingRepository.java            
├── service/
│   ├── BookingService.java               # Orchestrazione principale
│   ├── MovieService.java                 # Gestione film
│   ├── booking/                          # Design patterns
│   │   ├── DistributedLockManager.java   # Strategy Pattern
│   │   ├── BookingValidator.java         # Validation Pattern
│   │   └── BookingFactory.java           # Factory Pattern
│   └── domain/                           # Business Logic Layer
│       ├── BookingDomainService.java     # Domain Service Pattern
│       └── ScreeningDomainService.java   # Business rules isolate
└── service/exception/
    └── BookingException.java             # Custom exceptions
```

## Design Patterns Implementati

### 1. Domain Services (Business Logic Separation)
```java
@Component
public class BookingDomainService {
    public boolean isCancellable(Booking booking) {
        return booking.getStatus() == BookingStatus.CONFIRMED ||
                booking.getStatus() == BookingStatus.PENDING;
    }

    public void cancelBooking(Booking booking) {
        // Business logic centralizzata, non nelle entity
    }
}
```

### 2. Strategy Pattern per Distributed Locking
```java
@Component
public class DistributedLockManager {
    public <T> T executeWithLock(Long screeningId, Supplier<T> operation) {
        // Redis distributed lock per prevenire race conditions
    }
}
```

### 3. Factory Pattern per Entity Creation
```java
@Component
public class BookingFactory {
    public Booking createBooking(BookingRequest request, Screening screening) {
        // Creazione centralizzata con business rules
    }
}
```

## Principi SOLID Implementati

### Single Responsibility Principle (SRP)
- **Entity**: Solo data holders
- **Repository**: Solo accesso dati
- **Service**: Solo orchestrazione
- **Domain Services**: Solo business logic
- **Controller**: Solo gestione HTTP

### Dependency Inversion Principle (DIP)
```java
@Service
public class BookingService {
    private final BookingDomainService bookingDomainService; // Abstraction
    private final DistributedLockManager lockManager;        // Strategy
    private final BookingValidator validator;                // Validator
}
```

## Configurazioni Mission-Critical

### VirtualThreadConfig.java - Java 21 Concorrenza
```java
@Configuration
@EnableAsync
public class VirtualThreadConfig implements AsyncConfigurer {
    
    @Bean("virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor(); // Java 21 feature
    }
}
```

**A cosa serve:**
- **Alta concorrenza**: gestisce 1000+ richieste simultanee
- **Overhead minimo**: ~1KB per Virtual Thread vs ~2MB thread tradizionali
- **Performance**: context switching più veloce per I/O intensive operations

**Come viene usata:**
```java
@Async("virtualThreadExecutor")
public CompletableFuture<BookingResponse> createBookingAsync(BookingRequest request) {
    // Ogni richiesta ottiene un Virtual Thread dedicato
    return CompletableFuture.supplyAsync(() -> createBooking(request));
}
```

**Perché è necessaria:**
Nel scenario mission-critical, 1000 persone prenotano simultaneamente. I thread tradizionali limiterebbero la concorrenza, mentre Virtual Threads permettono di gestire tutte le richieste senza degradazione delle performance.

### RedisConfig.java - Distributed Locking
```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        // Configurazione per operazioni atomiche di locking
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
```

**A cosa serve:**
- **Race condition prevention**: solo una richiesta alla volta può prenotare posti per una proiezione
- **Consistency**: garantisce che non vengano venduti più biglietti dei posti disponibili
- **Coordinazione distribuita**: funziona anche con multiple istanze dell'applicazione

**Come viene usata:**
```java
@Component
public class DistributedLockManager {
    
    public <T> T executeWithLock(Long screeningId, Supplier<T> operation) {
        String lockKey = "booking:lock:screening:" + screeningId;
        String lockToken = UUID.randomUUID().toString();
        
        // Acquisisce lock atomicamente
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockToken, Duration.ofSeconds(30));
                
        if (!acquired) {
            throw new BookingException("Sistema occupato, riprova tra poco");
        }
        
        try {
            return operation.get(); // Esegue operazione protetta
        } finally {
            // Rilascia lock con script Lua atomico
            releaseLock(lockKey, lockToken);
        }
    }
}
```

**Perché è necessaria:**
Senza distributed locking, 1000 richieste simultanee per gli ultimi 10 posti causerebbero overbooking. Redis garantisce che solo una richiesta alla volta possa modificare i posti disponibili.

### Integrazione Sistema Mission-Critical

```java
@Service
public class BookingService {
    
    @Async("virtualThreadExecutor")  // ← VirtualThreadConfig
    @Transactional
    public CompletableFuture<BookingResponse> createBookingAsync(BookingRequest request) {
        return lockManager.executeWithLock(  // ← RedisConfig
                request.screeningId(),
                () -> processBooking(request)
        );
    }
}
```

### Atomic Database Operations
```java
@Modifying
@Query("""
        UPDATE Screening s 
        SET s.availableSeats = s.availableSeats - :seats 
        WHERE s.id = :screeningId 
        AND s.availableSeats >= :seats
        """)
int reserveSeatsAtomically(@Param("screeningId") Long screeningId, @Param("seats") int seats);
```

**Flusso completo:**
1. **Virtual Thread** gestisce la richiesta asincrona (alta concorrenza)
2. **Redis Lock** previene race conditions (consistency)
3. **Atomic DB operation** aggiorna posti disponibili
4. **Domain Services** applicano business logic
5. **Virtual Thread** completa senza bloccare altri thread

## Tecnologie e Versioni

- **Java 21** - Virtual Threads, Pattern Matching, Records, Text Blocks
- **Spring Boot 3.5.5** - Framework enterprise
- **Spring Data JPA** - Persistence layer
- **Redis** - Distributed caching e locking
- **PostgreSQL** - Database principale
- **H2** - Database per test
- **JUnit 5** - Testing framework
- **Mockito** - Mocking per unit test

## API Endpoints

### Prenotazioni
```bash
# Creazione prenotazione (sincrona)
POST /api/bookings
{
  "screeningId": 1,
  "userEmail": "user@example.com", 
  "numberOfSeats": 2
}

# Creazione prenotazione (asincrona - Virtual Threads)
POST /api/bookings/async

# Recupero prenotazione
GET /api/bookings/{id}

# Lista prenotazioni utente
GET /api/bookings?userEmail=user@example.com

# Cancellazione prenotazione  
DELETE /api/bookings/{id}?userEmail=user@example.com
```

### Film e Proiezioni
```bash
# Lista film
GET /api/movies

# Film con proiezioni
GET /api/movies/{id}

# Film disponibili
GET /api/movies/available

# Proiezioni di oggi
GET /api/movies/screenings/today

# Ricerca film
GET /api/movies/search?genre=Action&title=Avatar
```

## Esecuzione

### Avvio rapido
```bash
# Clone repository
git clone <repo-url>
cd cinema-booking-system

# Avvio infrastruttura
docker-compose up -d postgres redis

# Build e run
./mvnw clean package
java -jar target/movie-service-0.0.1-SNAPSHOT.jar
```

### Test Load per Alta Concorrenza
```bash
# Test 100 prenotazioni simultanee
chmod +x scripts/load-test.sh
./scripts/load-test.sh
```

## Testing

### Coverage Completo
- **Unit Tests**: 40+ test methods
- **Integration Tests**: Controller layer
- **Load Tests**: Concorrenza alta
- **Domain Tests**: Business logic isolata

```bash
# Esecuzione test
./mvnw test

# Test con coverage report
./mvnw test jacoco:report
```

### Test Classes
```
src/test/java/com/cinema/movie/
├── controller/
│   ├── BookingControllerTest.java        # REST API tests
│   ├── MovieControllerTest.java          # Film API tests
│   └── GlobalExceptionHandlerTest.java   # Error handling
├── service/
│   ├── BookingServiceTest.java           # Service orchestration
│   ├── MovieServiceTest.java             
│   ├── booking/
│   │   ├── BookingValidatorTest.java     # Validation logic
│   │   ├── BookingFactoryTest.java       # Factory pattern
│   │   └── DistributedLockManagerTest.java # Concurrency
│   └── domain/                           # Business logic tests
└── entity/
    └── EntityTest.java                   # JPA entities
```

## Certificazioni Coperte

### Java SE 21 Developer (1Z0-830)
- Virtual Threads per concorrenza
- Pattern Matching con switch expressions
- Records per DTOs immutabili
- Text Blocks per SQL/scripts
- Sealed interfaces per type safety

### Spring Professional
- Spring Boot 3.x con Java 21
- Spring Data JPA avanzato
- Async Processing con Virtual Threads
- Redis caching e distributed locking
- RESTful API design
- Exception handling centralizzato

## Monitoraggio

### Metriche disponibili
- `/actuator/health` - Health check
- `/actuator/metrics` - Performance metrics
- `/api/bookings/health` - Service-specific health

### Logging
- Structured logging per operazioni critiche
- Correlation IDs per tracciabilità
- Performance monitoring per Virtual Threads

## Benefici Java 21 Ottenuti

1. **Scalabilità**: Virtual Threads gestiscono milioni di connessioni concorrenti
2. **Performance**: Riduzione overhead thread tradizionali
3. **Leggibilità**: Pattern matching e switch expressions
4. **Type Safety**: Records immutabili e sealed interfaces
5. **Manutenibilità**: Codice più espressivo e meno verboso
6. **Produttività**: Meno boilerplate code

Il sistema dimostra come **Java 21** risolva problemi reali di **alta concorrenza** in applicazioni **mission-critical**, mantenendo **codice pulito** e **architettura enterprise-grade**.
