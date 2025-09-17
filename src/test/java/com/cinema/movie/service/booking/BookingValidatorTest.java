package com.cinema.movie.service.booking;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.BookingStatus;
import com.cinema.movie.entity.Movie;
import com.cinema.movie.entity.Screening;
import com.cinema.movie.entity.domain.BookingDomainService;
import com.cinema.movie.entity.domain.ScreeningDomainService;
import com.cinema.movie.repository.ScreeningRepository;
import com.cinema.movie.exception.BookingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingValidatorTest {

    @Mock private ScreeningRepository screeningRepository;

    // Nuovi mock per Domain Services
    @Mock private BookingDomainService bookingDomainService;
    @Mock private ScreeningDomainService screeningDomainService;

    @InjectMocks private BookingValidator bookingValidator;

    @Test
    void testValidateAndGetScreening() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createValidScreening();

        when(screeningRepository.findByIdWithAvailableSeats(1L, 2))
                .thenReturn(Optional.of(screening));

        // Mock Domain Services per validazioni
        when(screeningDomainService.hasInsufficientSeats(screening, 2)).thenReturn(false);
        when(screeningDomainService.isBookingNotAllowed(screening)).thenReturn(false);

        // When
        Screening result = bookingValidator.validateAndGetScreening(request);

        // Then
        assertNotNull(result);
        assertEquals(screening.getId(), result.getId());
        verify(screeningRepository).findByIdWithAvailableSeats(1L, 2);

        // Verifica interazione con Domain Services
        verify(screeningDomainService).hasInsufficientSeats(screening, 2);
        verify(screeningDomainService).isBookingNotAllowed(screening);
    }

    @Test
    void testValidateAndGetScreeningNotFound() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        when(screeningRepository.findByIdWithAvailableSeats(1L, 2))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(BookingException.class,
                () -> bookingValidator.validateAndGetScreening(request));
        verify(screeningRepository).findByIdWithAvailableSeats(1L, 2);

        // Domain Services non dovrebbero essere chiamati se screening non trovato
        verify(screeningDomainService, never()).hasInsufficientSeats(any(), anyInt());
        verify(screeningDomainService, never()).isBookingNotAllowed(any());
    }

    @Test
    void testValidateAndGetScreeningSoldOut() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createValidScreening();
        screening.setAvailableSeats(0);

        when(screeningRepository.findByIdWithAvailableSeats(1L, 2))
                .thenReturn(Optional.of(screening));

        // When & Then
        assertThrows(BookingException.class,
                () -> bookingValidator.validateAndGetScreening(request));
        verify(screeningRepository).findByIdWithAvailableSeats(1L, 2);
    }

    @Test
    void testValidateAndGetScreeningInsufficientSeats() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createValidScreening();
        screening.setAvailableSeats(1);

        when(screeningRepository.findByIdWithAvailableSeats(1L, 2))
                .thenReturn(Optional.of(screening));
        when(screeningDomainService.hasInsufficientSeats(screening, 2)).thenReturn(true);

        // When & Then
        assertThrows(BookingException.class,
                () -> bookingValidator.validateAndGetScreening(request));
        verify(screeningDomainService).hasInsufficientSeats(screening, 2);
    }

    @Test
    void testValidateAndGetScreeningBookingNotAllowed() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createValidScreening();

        when(screeningRepository.findByIdWithAvailableSeats(1L, 2))
                .thenReturn(Optional.of(screening));
        when(screeningDomainService.hasInsufficientSeats(screening, 2)).thenReturn(false);
        when(screeningDomainService.isBookingNotAllowed(screening)).thenReturn(true);
        when(screeningDomainService.hasStarted(screening)).thenReturn(true);

        // When & Then
        assertThrows(BookingException.class,
                () -> bookingValidator.validateAndGetScreening(request));

        verify(screeningDomainService).isBookingNotAllowed(screening);
        verify(screeningDomainService).hasStarted(screening);
    }

    @Test
    void testValidateAndGetScreeningTimingNotAllowedButNotStarted() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createValidScreening();

        when(screeningRepository.findByIdWithAvailableSeats(1L, 2))
                .thenReturn(Optional.of(screening));
        when(screeningDomainService.hasInsufficientSeats(screening, 2)).thenReturn(false);
        when(screeningDomainService.isBookingNotAllowed(screening)).thenReturn(true);
        when(screeningDomainService.hasStarted(screening)).thenReturn(false);

        // When & Then
        assertThrows(BookingException.class,
                () -> bookingValidator.validateAndGetScreening(request));

        verify(screeningDomainService).isBookingNotAllowed(screening);
        verify(screeningDomainService).hasStarted(screening);
    }

    @Test
    void testValidateCancellation() {
        // Given
        var booking = createValidBooking();
        when(bookingDomainService.isNotCancellable(booking)).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() ->
                bookingValidator.validateCancellation(booking, "test@email.com"));

        // Verifica interazione con Domain Service invece di entity
        verify(bookingDomainService).isNotCancellable(booking);
    }

    @Test
    void testValidateCancellationWrongUser() {
        // Given
        var booking = createValidBooking();
        booking.setUserEmail("other@email.com");

        // When & Then
        assertThrows(BookingException.class, () ->
                bookingValidator.validateCancellation(booking, "test@email.com"));

        // Domain Service non dovrebbe essere chiamato per errore di autorizzazione
        verify(bookingDomainService, never()).isNotCancellable(any());
    }

    @Test
    void testValidateCancellationNotCancellable() {
        // Given
        var booking = createValidBooking();
        when(bookingDomainService.isNotCancellable(booking)).thenReturn(true);
        when(bookingDomainService.getStatusMessage(booking)).thenReturn("Prenotazione confermata!");

        // When & Then
        assertThrows(BookingException.class, () ->
                bookingValidator.validateCancellation(booking, "test@email.com"));

        verify(bookingDomainService).isNotCancellable(booking);
        verify(bookingDomainService).getStatusMessage(booking);
    }

    private Screening createValidScreening() {
        var movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");

        var screening = new Screening();
        screening.setId(1L);
        screening.setMovie(movie);
        screening.setStartTime(LocalDateTime.now().plusHours(2));
        screening.setTotalSeats(100);
        screening.setAvailableSeats(50);
        screening.setPrice(BigDecimal.valueOf(10.0));
        return screening;
    }

    private Booking createValidBooking() {
        var booking = new Booking();
        booking.setId(1L);
        booking.setScreening(createValidScreening());
        booking.setUserEmail("test@email.com");
        booking.setNumberOfSeats(2);
        booking.setStatus(BookingStatus.CONFIRMED);
        return booking;
    }
}
