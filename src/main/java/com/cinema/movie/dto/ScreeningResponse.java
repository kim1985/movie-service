package com.cinema.movie.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO per proiezione.
 */
public record ScreeningResponse(
        Long id,
        Long movieId,
        String movieTitle,
        LocalDateTime startTime,
        Integer availableSeats,
        BigDecimal price
) {
    public static ScreeningResponse from(com.cinema.movie.entity.Screening screening) {
        return new ScreeningResponse(
                screening.getId(),
                screening.getMovie().getId(),
                screening.getMovie().getTitle(),
                screening.getStartTime(),
                screening.getAvailableSeats(),
                screening.getPrice()
        );
    }
}
