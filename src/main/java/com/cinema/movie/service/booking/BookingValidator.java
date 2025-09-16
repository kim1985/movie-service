package com.cinema.movie.service.booking;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.Screening;
import com.cinema.movie.repository.ScreeningRepository;
import com.cinema.movie.exception.BookingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Validator con Pattern Matching Java 21.
 * Singola responsabilità: validazioni business.
 */
@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final ScreeningRepository screeningRepository;

    public Screening validateAndGetScreening(BookingRequest request) {
        Screening screening = screeningRepository
                .findByIdWithAvailableSeats(request.screeningId(), request.numberOfSeats())
                .orElseThrow(() -> new BookingException("Proiezione non disponibile"));

        validateAvailability(screening, request.numberOfSeats());
        validateTiming(screening);

        return screening;
    }

    public void validateCancellation(Booking booking, String userEmail) {
        if (!booking.getUserEmail().equals(userEmail)) {
            throw new BookingException("Non autorizzato");
        }

        if (!booking.canBeCancelled()) {
            throw new BookingException("Prenotazione non cancellabile: " + booking.getStatusMessage());
        }
    }

    private void validateAvailability(Screening screening, int requestedSeats) {
        Integer availableSeats = screening.getAvailableSeats();

        // Java 21 compatible validation
        switch (availableSeats) {
            case null -> throw new BookingException("Proiezione non valida");
            case 0 -> throw new BookingException("Proiezione sold out");
            default -> {
                if (availableSeats < requestedSeats) {
                    throw new BookingException("Solo " + availableSeats + " posti disponibili");
                }
                // OK - posti sufficienti
            }
        }
    }

    private void validateTiming(Screening screening) {
        var now = LocalDateTime.now();
        var minutesUntilStart = Duration.between(now, screening.getStartTime()).toMinutes();

        // Java 21 compatible pattern matching
        if (minutesUntilStart <= 0) {
            throw new BookingException("Non è possibile prenotare per proiezioni passate");
        }

        if (minutesUntilStart < 30) {
            throw new BookingException("Prenotazione chiusa 30 minuti prima dell'inizio");
        }

        // OK - può procedere
    }
}
