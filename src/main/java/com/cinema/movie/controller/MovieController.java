package com.cinema.movie.controller;

import com.cinema.movie.dto.MovieResponse;
import com.cinema.movie.dto.ScreeningResponse;
import com.cinema.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller per gestione film e proiezioni.
 * Separato da BookingController per rispettare SRP.
 */
@RestController
@RequestMapping("/api/movies")
@Slf4j
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    /**
     * Lista tutti i film.
     */
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        List<MovieResponse> movies = movieService.getAllMovies();
        return ResponseEntity.ok(movies);
    }

    /**
     * Recupera film per ID con proiezioni.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovie(@PathVariable Long id) {
        return movieService.getMovieWithScreenings(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista film con proiezioni disponibili.
     */
    @GetMapping("/available")
    public ResponseEntity<List<MovieResponse>> getAvailableMovies() {
        List<MovieResponse> movies = movieService.getMoviesWithAvailableScreenings();
        return ResponseEntity.ok(movies);
    }

    /**
     * Lista proiezioni disponibili oggi.
     */
    @GetMapping("/screenings/today")
    public ResponseEntity<List<ScreeningResponse>> getTodayScreenings() {
        List<ScreeningResponse> screenings = movieService.getTodayScreenings();
        return ResponseEntity.ok(screenings);
    }

    /**
     * Ricerca film per genere.
     */
    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String title) {

        List<MovieResponse> movies = movieService.searchMovies(genre, title);
        return ResponseEntity.ok(movies);
    }
}
