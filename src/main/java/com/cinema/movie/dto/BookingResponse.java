package com.cinema.movie.dto;

import com.cinema.movie.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO per risposta prenotazione.
 */
public record BookingResponse(
        Long id,
        Long screeningId,
        String userEmail,
        Integer numberOfSeats,
        BigDecimal totalPrice,
        BookingStatus status,
        LocalDateTime createdAt,
        String movieTitle,
        LocalDateTime screeningTime
) {
    // Factory method semplice per mapping
    public static BookingResponse from(com.cinema.movie.entity.Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getScreening().getId(),
                booking.getUserEmail(),
                booking.getNumberOfSeats(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getScreening().getMovie().getTitle(),
                booking.getScreening().getStartTime()
        );
    }
}
