package com.cinema.movie.service.booking;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.BookingStatus;
import com.cinema.movie.entity.Movie;
import com.cinema.movie.entity.Screening;
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
    @InjectMocks private BookingValidator bookingValidator;

    @Test
    void testValidateAndGetScreening() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createValidScreening();

        when(screeningRepository.findByIdWithAvailableSeats(1L, 2))
                .thenReturn(Optional.of(screening));

        // When
        Screening result = bookingValidator.validateAndGetScreening(request);

        // Then
        assertNotNull(result);
        assertEquals(screening.getId(), result.getId());
        verify(screeningRepository).findByIdWithAvailableSeats(1L, 2);
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
    }

    @Test
    void testValidateAndGetScreeningPastTime() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createValidScreening();
        screening.setStartTime(LocalDateTime.now().minusHours(1)); // Past time

        when(screeningRepository.findByIdWithAvailableSeats(1L, 2))
                .thenReturn(Optional.of(screening));

        // When & Then
        assertThrows(BookingException.class,
                () -> bookingValidator.validateAndGetScreening(request));
    }

    @Test
    void testValidateCancellation() {
        // Given
        var booking = createValidBooking();
        booking.setStatus(BookingStatus.CONFIRMED);

        // When & Then
        assertDoesNotThrow(() ->
                bookingValidator.validateCancellation(booking, "test@email.com"));
    }

    @Test
    void testValidateCancellationWrongUser() {
        // Given
        var booking = createValidBooking();
        booking.setUserEmail("other@email.com");

        // When & Then
        assertThrows(BookingException.class, () ->
                bookingValidator.validateCancellation(booking, "test@email.com"));
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
