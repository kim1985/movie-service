package com.cinema.movie.repository;

import com.cinema.movie.entity.Booking;
import com.cinema.movie.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Prenotazioni per utente
    List<Booking> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    // Prenotazioni per proiezione
    List<Booking> findByScreeningIdAndStatus(Long screeningId, BookingStatus status);

    // Prenotazioni confermate per una proiezione - QUERY SEMPLIFICATA
    @Query("""
        SELECT COALESCE(SUM(b.numberOfSeats), 0) 
        FROM Booking b 
        WHERE b.screening.id = :screeningId 
        AND b.status = 'CONFIRMED'
        """)
    Integer countConfirmedSeatsForScreening(@Param("screeningId") Long screeningId);

    // Prenotazioni scadute - QUERY SEMPLIFICATA
    @Query("""
        SELECT b FROM Booking b 
        WHERE b.status = 'PENDING' 
        AND b.createdAt < :cutoffTime
        """)
    List<Booking> findExpiredPendingBookings(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Prenotazioni di un utente per un film specifico
    List<Booking> findByUserEmailAndScreening_MovieId(String userEmail, Long movieId);
}