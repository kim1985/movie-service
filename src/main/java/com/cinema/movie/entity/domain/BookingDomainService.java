package com.cinema.movie.entity.domain;

import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Domain Service per business logic delle prenotazioni.
 * Evita di mettere logica business nelle entity.
 */
@Component
public class BookingDomainService {

    /**
     * Conferma una prenotazione.
     */
    public void confirmBooking(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Can only confirm pending bookings");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
    }

    /**
     * Cancella una prenotazione.
     */
    public void cancelBooking(Booking booking) {
        if (isNotCancellable(booking)) {
            throw new IllegalStateException("Cannot cancel booking in status: " + booking.getStatus());
        }
        booking.setStatus(BookingStatus.CANCELLED);
    }

    /**
     * Verifica se una prenotazione può essere cancellata.
     */
    public boolean isCancellable(Booking booking) {
        return booking.getStatus() == BookingStatus.CONFIRMED ||
                booking.getStatus() == BookingStatus.PENDING;
    }
    /**
     * Verifica se una prenotazione NON può essere cancellata.
     */
    public boolean isNotCancellable(Booking booking) {
        return !isCancellable(booking);
    }

    /**
     * Genera messaggio di stato con pattern matching Java 21.
     */
    public String getStatusMessage(Booking booking) {
        return switch (booking.getStatus()) {
            case PENDING -> "Prenotazione in corso...";
            case CONFIRMED -> "Prenotazione confermata!";
            case CANCELLED -> "Prenotazione cancellata";
            case EXPIRED -> "Prenotazione scaduta";
        };
    }

    /**
     * Verifica se una prenotazione è scaduta.
     */
    public boolean isExpired(Booking booking) {
        // Business rule: prenotazione scade se pending da più di 15 minuti
        if (booking.getStatus() != BookingStatus.PENDING) {
            return false;
        }

        LocalDateTime expiryTime = booking.getCreatedAt().plusMinutes(15);
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Scade prenotazioni pendenti.
     */
    public void expireBooking(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.EXPIRED);
        }
    }
}
