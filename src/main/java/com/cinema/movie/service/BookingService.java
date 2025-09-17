package com.cinema.movie.service;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.dto.BookingResponse;
import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.domain.BookingDomainService;
import com.cinema.movie.entity.domain.ScreeningDomainService;
import com.cinema.movie.repository.BookingRepository;
import com.cinema.movie.service.booking.BookingFactory;
import com.cinema.movie.service.booking.BookingValidator;
import com.cinema.movie.service.booking.DistributedLockManager;
import com.cinema.movie.exception.BookingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service principale per gestione prenotazioni.
 * Usa Domain Services per business logic invece delle entity.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingFactory bookingFactory;
    private final BookingValidator bookingValidator;
    private final DistributedLockManager lockManager;

    // Domain Services per business logic
    private final BookingDomainService bookingDomainService;
    private final ScreeningDomainService screeningDomainService;

    @Async("virtualThreadExecutor")
    @Transactional
    public CompletableFuture<BookingResponse> createBookingAsync(BookingRequest request) {
        return CompletableFuture.supplyAsync(() -> createBooking(request));
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        return lockManager.executeWithLock(
                request.screeningId(),
                () -> processBooking(request)
        );
    }

    private BookingResponse processBooking(BookingRequest request) {
        // 1. Valida e recupera dati
        var screening = bookingValidator.validateAndGetScreening(request);

        // 2. Crea booking usando Factory
        var booking = bookingFactory.createBooking(request, screening);

        // 3. Persisti
        var saved = bookingRepository.save(booking);

        log.info("Prenotazione creata: {} posti per {}",
                request.numberOfSeats(), request.userEmail());

        return BookingResponse.from(saved);
    }

    public Optional<BookingResponse> getBooking(Long id) {
        return bookingRepository.findById(id).map(BookingResponse::from);
    }

    public List<BookingResponse> getUserBookings(String userEmail) {
        return bookingRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Prenotazione non trovata"));

        bookingValidator.validateCancellation(booking, userEmail);

        // Usa Domain Service invece della business logic nell'entity
        bookingDomainService.cancelBooking(booking);
        screeningDomainService.releaseSeats(booking.getScreening(), booking.getNumberOfSeats());

        var cancelled = bookingRepository.save(booking);
        log.info("Prenotazione cancellata: {}", bookingId);

        return BookingResponse.from(cancelled);
    }
}
