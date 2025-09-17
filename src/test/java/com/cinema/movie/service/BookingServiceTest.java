package com.cinema.movie.service;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.dto.BookingResponse;
import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.BookingStatus;
import com.cinema.movie.entity.Movie;
import com.cinema.movie.entity.Screening;
import com.cinema.movie.entity.domain.BookingDomainService;
import com.cinema.movie.entity.domain.ScreeningDomainService;
import com.cinema.movie.repository.BookingRepository;
import com.cinema.movie.service.booking.BookingFactory;
import com.cinema.movie.service.booking.BookingValidator;
import com.cinema.movie.service.booking.DistributedLockManager;
import com.cinema.movie.exception.BookingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingFactory bookingFactory;
    @Mock private BookingValidator bookingValidator;
    @Mock private DistributedLockManager lockManager;

    // Nuovi mock per Domain Services
    @Mock private BookingDomainService bookingDomainService;
    @Mock private ScreeningDomainService screeningDomainService;

    @InjectMocks private BookingService bookingService;

    @Test
    void testCreateBooking() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createTestScreening();
        var booking = createTestBooking();

        when(lockManager.executeWithLock(eq(1L), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<BookingResponse> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(bookingValidator.validateAndGetScreening(request)).thenReturn(screening);
        when(bookingFactory.createBooking(request, screening)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);

        // When
        BookingResponse result = bookingService.createBooking(request);

        // Then
        assertNotNull(result);
        assertEquals("test@email.com", result.userEmail());
        assertEquals(2, result.numberOfSeats());
        verify(lockManager).executeWithLock(eq(1L), any(Supplier.class));
        verify(bookingValidator).validateAndGetScreening(request);
        verify(bookingFactory).createBooking(request, screening);
        verify(bookingRepository).save(booking);
    }

    @Test
    void testGetBooking() {
        // Given
        var booking = createTestBooking();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // When
        Optional<BookingResponse> result = bookingService.getBooking(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@email.com", result.get().userEmail());
        verify(bookingRepository).findById(1L);
    }

    @Test
    void testGetBookingNotFound() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<BookingResponse> result = bookingService.getBooking(1L);

        // Then
        assertFalse(result.isPresent());
        verify(bookingRepository).findById(1L);
    }

    @Test
    void testGetUserBookings() {
        // Given
        var bookings = List.of(createTestBooking());
        when(bookingRepository.findByUserEmailOrderByCreatedAtDesc("test@email.com"))
                .thenReturn(bookings);

        // When
        List<BookingResponse> result = bookingService.getUserBookings("test@email.com");

        // Then
        assertEquals(1, result.size());
        assertEquals("test@email.com", result.getFirst().userEmail());
        verify(bookingRepository).findByUserEmailOrderByCreatedAtDesc("test@email.com");
    }

    @Test
    void testCancelBooking() {
        // Given
        var booking = createTestBooking();
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        doNothing().when(bookingValidator).validateCancellation(booking, "test@email.com");

        // Mock Domain Services - non più business logic nell'entity
        doNothing().when(bookingDomainService).cancelBooking(booking);
        doNothing().when(screeningDomainService).releaseSeats(any(Screening.class), eq(2));

        when(bookingRepository.save(booking)).thenReturn(booking);

        // When
        BookingResponse result = bookingService.cancelBooking(1L, "test@email.com");

        // Then
        assertNotNull(result);
        verify(bookingValidator).validateCancellation(booking, "test@email.com");

        // Verifica interazione con Domain Services invece delle entity
        verify(bookingDomainService).cancelBooking(booking);
        verify(screeningDomainService).releaseSeats(booking.getScreening(), 2);

        verify(bookingRepository).save(booking);
    }

    @Test
    void testCancelBookingNotFound() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BookingException.class,
                () -> bookingService.cancelBooking(1L, "test@email.com"));
        verify(bookingRepository).findById(1L);

        // Domain Services non dovrebbero essere chiamati
        verify(bookingDomainService, never()).cancelBooking(any());
        verify(screeningDomainService, never()).releaseSeats(any(), anyInt());
    }

    private Screening createTestScreening() {
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

    private Booking createTestBooking() {
        var booking = new Booking();
        booking.setId(1L);
        booking.setScreening(createTestScreening());
        booking.setUserEmail("test@email.com");
        booking.setNumberOfSeats(2);
        booking.setTotalPrice(BigDecimal.valueOf(20.0));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());
        return booking;
    }
}
