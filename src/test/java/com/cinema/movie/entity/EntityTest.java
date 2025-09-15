package com.cinema.movie.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

// Test per le entità JPA senza Spring Context
class EntityTest {

    @Test
    void testMovieCreation() {
        var movie = new Movie();
        movie.setTitle("Avatar 3");
        movie.setGenre("Fantascienza");
        movie.setDuration(180);
        movie.setDescription("Jake Sully ritorna su Pandora");

        assertEquals("Avatar 3", movie.getTitle());
        assertEquals("Fantascienza", movie.getGenre());
        assertEquals(180, movie.getDuration());
    }

    @Test
    void testScreeningBusinessLogic() {
        var screening = new Screening();
        screening.setTotalSeats(100);
        screening.setAvailableSeats(100);
        screening.setPrice(new BigDecimal("12.50"));

        // Test disponibilità posti
        assertTrue(screening.hasAvailableSeats(10));
        assertFalse(screening.hasAvailableSeats(150));

        // Test prenotazione posti
        screening.reserveSeats(30);
        assertEquals(70, screening.getAvailableSeats());

        // Test rilascio posti
        screening.releaseSeats(10);
        assertEquals(80, screening.getAvailableSeats());
    }

    @Test
    void testBookingStatusPatternMatching() {
        var booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);

        // Test Java 21 Pattern Matching
        String message = booking.getStatusMessage();
        assertEquals("Prenotazione confermata!", message);

        // Test business logic
        assertTrue(booking.canBeCancelled());

        booking.cancel();
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void testBookingStatusTransitions() {
        var booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);

        // Conferma prenotazione
        booking.confirm();
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertNotNull(booking.getConfirmedAt());

        // Prova a confermare di nuovo (dovrebbe fallire)
        assertThrows(IllegalStateException.class, () -> booking.confirm());
    }

    @Test
    void testScreeningOverbookingPrevention() {
        var screening = new Screening();
        screening.setTotalSeats(10);
        screening.setAvailableSeats(5);

        // Tentativo di prenotare più posti di quelli disponibili
        assertThrows(IllegalArgumentException.class,
                () -> screening.reserveSeats(10));

        // I posti disponibili non dovrebbero essere cambiati
        assertEquals(5, screening.getAvailableSeats());
    }
}