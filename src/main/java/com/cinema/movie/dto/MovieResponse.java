package com.cinema.movie.dto;

import java.util.List;

/**
 * DTO per film con proiezioni.
 */
public record MovieResponse(
        Long id,
        String title,
        String genre,
        Integer duration,
        String description,
        List<ScreeningResponse> screenings
) {
    public static MovieResponse from(com.cinema.movie.entity.Movie movie) {
        var screenings = movie.getScreenings().stream()
                .map(ScreeningResponse::from)
                .toList();

        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getGenre(),
                movie.getDuration(),
                movie.getDescription(),
                screenings
        );
    }
}
