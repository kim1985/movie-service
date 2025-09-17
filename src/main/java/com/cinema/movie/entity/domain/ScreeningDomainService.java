package com.cinema.movie.entity.domain;

import com.cinema.movie.entity.Screening;
import org.springframework.stereotype.Component;

/**
 * Domain Service per business logic delle proiezioni.
 * Centralizza operazioni sui posti disponibili.
 */
@Component
public class ScreeningDomainService {

    /**
     * Verifica se ci sono posti disponibili.
     */
    public boolean hasSufficientSeats(Screening screening, int requestedSeats) {
        return screening.getAvailableSeats() != null &&
                screening.getAvailableSeats() >= requestedSeats;
    }
    /**
     * Verifica se NON ci sono posti sufficienti.
     */
    public boolean hasInsufficientSeats(Screening screening, int requestedSeats) {
        return !hasSufficientSeats(screening, requestedSeats);
    }

    /**
     * Prenota posti (logica business).
     */
    public void reserveSeats(Screening screening, int seats) {
        if (hasInsufficientSeats(screening, seats)) {
            throw new IllegalArgumentException("Not enough available seats");
        }
        screening.setAvailableSeats(screening.getAvailableSeats() - seats);
    }

    /**
     * Rilascia posti (logica business).
     */
    public void releaseSeats(Screening screening, int seats) {
        int newAvailable = screening.getAvailableSeats() + seats;
        int maxSeats = Math.min(screening.getTotalSeats(), newAvailable);
        screening.setAvailableSeats(maxSeats);
    }

    /**
     * Verifica se la proiezione è iniziata.
     */
    public boolean hasStarted(Screening screening) {
        return screening.getStartTime().isBefore(java.time.LocalDateTime.now());
    }

    /**
     * Verifica se è possibile prenotare (business rules).
     */
    public boolean isBookingAllowed(Screening screening) {
        // Non si può prenotare se la proiezione è iniziata
        if (hasStarted(screening)) {
            return false;
        }

        // Non si può prenotare 30 minuti prima dell'inizio
        java.time.LocalDateTime cutoffTime = screening.getStartTime().minusMinutes(30);
        return java.time.LocalDateTime.now().isBefore(cutoffTime);
    }

    public boolean isBookingNotAllowed(Screening screening) {
        return !isBookingAllowed(screening);
    }

}
