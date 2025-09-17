package com.cinema.movie.service.booking;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.Screening;
import com.cinema.movie.entity.domain.BookingDomainService;
import com.cinema.movie.entity.domain.ScreeningDomainService;
import com.cinema.movie.exception.BookingException;
import com.cinema.movie.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator usando Domain Services per business logic.
 */
@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final ScreeningRepository screeningRepository;
    private final BookingDomainService bookingDomainService;
    private final ScreeningDomainService screeningDomainService;

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

        // Usa Domain Service per business logic
        if (bookingDomainService.isNotCancellable(booking)) {
            String statusMessage = bookingDomainService.getStatusMessage(booking);
            throw new BookingException("Prenotazione non cancellabile: " + statusMessage);
        }
    }

    private void validateAvailability(Screening screening, int requestedSeats) {
        Integer availableSeats = screening.getAvailableSeats();

        switch (availableSeats) {
            case null -> throw new BookingException("Proiezione non valida");
            case 0 -> throw new BookingException("Proiezione sold out");
            default -> {
                // Usa Domain Service invece del metodo nell'entity
                if (screeningDomainService.hasInsufficientSeats(screening, requestedSeats)) {
                    throw new BookingException("Solo " + availableSeats + " posti disponibili");
                }
            }
        }
    }

    private void validateTiming(Screening screening) {
        // Usa Domain Service per business rules
        if (screeningDomainService.isBookingNotAllowed(screening)) {
            if (screeningDomainService.hasStarted(screening)) {
                throw new BookingException("Non è possibile prenotare per proiezioni iniziate");
            } else {
                throw new BookingException("Prenotazione chiusa 30 minuti prima dell'inizio");
            }
        }
    }
}
