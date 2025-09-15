package com.cinema.movie.repository;

import com.cinema.movie.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    // Proiezioni per un film specifico
    List<Screening> findByMovieIdOrderByStartTime(Long movieId);

    // Proiezioni disponibili (future con posti liberi)
    @Query("""
        SELECT s FROM Screening s 
        WHERE s.startTime > :now 
        AND s.availableSeats > 0
        ORDER BY s.startTime
        """)
    List<Screening> findAvailableScreenings(@Param("now") LocalDateTime now);

    // Proiezioni di oggi - QUERY CORRETTA per H2
    @Query("""
        SELECT s FROM Screening s 
        WHERE s.startTime >= :startOfDay 
        AND s.startTime < :endOfDay
        AND s.availableSeats > 0
        ORDER BY s.startTime
        """)
    List<Screening> findTodayScreenings(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // Aggiornamento atomico
    @Modifying
    @Query("""
        UPDATE Screening s 
        SET s.availableSeats = s.availableSeats - :seats 
        WHERE s.id = :screeningId 
        AND s.availableSeats >= :seats
        """)
    int reserveSeatsAtomically(@Param("screeningId") Long screeningId, @Param("seats") int seats);

    // Verifica disponibilità
    @Query("""
        SELECT s FROM Screening s 
        WHERE s.id = :id 
        AND s.availableSeats >= :requiredSeats
        """)
    Optional<Screening> findByIdWithAvailableSeats(@Param("id") Long id, @Param("requiredSeats") int requiredSeats);
}