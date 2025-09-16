package com.cinema.movie.controller;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.dto.BookingResponse;
import com.cinema.movie.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller per gestione prenotazioni cinema.
 */
@RestController
@RequestMapping("/api/bookings")
@Validated
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Crea nuova prenotazione.
     * Virtual Threads gestiscono automaticamente alta concorrenza.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {

        log.info("Nuova prenotazione: {} posti per screening {}",
                request.numberOfSeats(), request.screeningId());

        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    /**
     * Crea prenotazione asincrona per test alta concorrenza.
     */
    @PostMapping("/async")
    public CompletableFuture<ResponseEntity<BookingResponse>> createBookingAsync(
            @Valid @RequestBody BookingRequest request) {

        log.info("Prenotazione asincrona: {} posti per screening {}",
                request.numberOfSeats(), request.screeningId());

        return bookingService.createBookingAsync(request)
                .thenApply(booking -> ResponseEntity.status(HttpStatus.CREATED).body(booking));
    }

    /**
     * Recupera prenotazione per ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista prenotazioni utente.
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings(
            @RequestParam String userEmail) {

        List<BookingResponse> bookings = bookingService.getUserBookings(userEmail);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancella prenotazione.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestParam String userEmail) {

        log.info("Cancellazione prenotazione {} per utente {}", id, userEmail);

        BookingResponse cancelled = bookingService.cancelBooking(id, userEmail);
        return ResponseEntity.ok(cancelled);
    }

    /**
     * Health check per monitoraggio.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Booking service is running");
    }
}
