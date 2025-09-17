package com.cinema.movie.service.booking;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.BookingStatus;
import com.cinema.movie.entity.Movie;
import com.cinema.movie.entity.Screening;
import com.cinema.movie.entity.domain.BookingDomainService;
import com.cinema.movie.repository.ScreeningRepository;
import com.cinema.movie.exception.BookingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingFactoryTest {

    @Mock private ScreeningRepository screeningRepository;
    @Mock private BookingDomainService bookingDomainService;

    @InjectMocks private BookingFactory bookingFactory;

    @Test
    void testCreateBooking() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createTestScreening();

        when(screeningRepository.reserveSeatsAtomically(1L, 2)).thenReturn(1);
        doNothing().when(bookingDomainService).confirmBooking(any(Booking.class));

        // When
        Booking result = bookingFactory.createBooking(request, screening);

        // Then
        assertNotNull(result);
        assertEquals("test@email.com", result.getUserEmail());
        assertEquals(2, result.getNumberOfSeats());
        assertEquals(BigDecimal.valueOf(20.0), result.getTotalPrice());
        assertEquals(BookingStatus.PENDING, result.getStatus()); // Prima della conferma
        verify(screeningRepository).reserveSeatsAtomically(1L, 2);
        verify(bookingDomainService).confirmBooking(result);
    }

    @Test
    void testCreateBookingSeatsNotAvailable() {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var screening = createTestScreening();

        when(screeningRepository.reserveSeatsAtomically(1L, 2)).thenReturn(0);

        // When & Then
        assertThrows(BookingException.class,
                () -> bookingFactory.createBooking(request, screening));
        verify(screeningRepository).reserveSeatsAtomically(1L, 2);
        verify(bookingDomainService, never()).confirmBooking(any());
    }

    private Screening createTestScreening() {
        var movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");

        var screening = new Screening();
        screening.setId(1L);
        screening.setMovie(movie);
        screening.setStartTime(LocalDateTime.now().plusHours(2));
        screening.setPrice(BigDecimal.valueOf(10.0));
        return screening;
    }
}
