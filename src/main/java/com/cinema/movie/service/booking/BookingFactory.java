package com.cinema.movie.service.booking;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.BookingStatus;
import com.cinema.movie.entity.Screening;
import com.cinema.movie.entity.domain.BookingDomainService;
import com.cinema.movie.repository.ScreeningRepository;
import com.cinema.movie.exception.BookingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Factory Pattern per creare Booking entities.
 * Singola responsabilità: creazione oggetti.
 */
@Component
@RequiredArgsConstructor
public class BookingFactory {

    private final ScreeningRepository screeningRepository;
    private final BookingDomainService bookingDomainService;

    public Booking createBooking(BookingRequest request, Screening screening) {
        // Aggiorna posti atomicamente
        int updatedRows = screeningRepository.reserveSeatsAtomically(
                screening.getId(),
                request.numberOfSeats()
        );

        if (updatedRows == 0) {
            throw new BookingException("Posti non più disponibili");
        }

        // Crea booking entity (solo data holder)
        var booking = new Booking();
        booking.setScreening(screening);
        booking.setUserEmail(request.userEmail());
        booking.setNumberOfSeats(request.numberOfSeats());
        booking.setTotalPrice(calculateTotalPrice(screening, request.numberOfSeats()));
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        // Usa Domain Service per business logic invece del metodo nell'entity
        bookingDomainService.confirmBooking(booking);

        return booking;
    }

    private BigDecimal calculateTotalPrice(Screening screening, int numberOfSeats) {
        return screening.getPrice().multiply(BigDecimal.valueOf(numberOfSeats));
    }
}
