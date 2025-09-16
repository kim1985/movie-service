package com.cinema.movie.service;

import com.cinema.movie.dto.MovieResponse;
import com.cinema.movie.dto.ScreeningResponse;
import com.cinema.movie.repository.MovieRepository;
import com.cinema.movie.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service per gestione film e proiezioni.
 * Separato da BookingService per rispettare SRP.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    public Optional<MovieResponse> getMovieWithScreenings(Long id) {
        return movieRepository.findById(id)
                .map(MovieResponse::from);
    }

    public List<MovieResponse> getMoviesWithAvailableScreenings() {
        return movieRepository.findMoviesWithAvailableScreenings()
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    public List<ScreeningResponse> getTodayScreenings() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return screeningRepository.findTodayScreenings(startOfDay, endOfDay)
                .stream()
                .map(ScreeningResponse::from)
                .toList();
    }

    public List<MovieResponse> searchMovies(String genre, String title) {
        return movieRepository.searchMovies(genre, title, null, null)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }
}
