package com.cinema.movie.service;

import com.cinema.movie.dto.MovieResponse;
import com.cinema.movie.dto.ScreeningResponse;
import com.cinema.movie.entity.Movie;
import com.cinema.movie.entity.Screening;
import com.cinema.movie.repository.MovieRepository;
import com.cinema.movie.repository.ScreeningRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock private MovieRepository movieRepository;
    @Mock private ScreeningRepository screeningRepository;
    @InjectMocks private MovieService movieService;

    @Test
    void testGetAllMovies() {
        // Given
        var movies = List.of(createTestMovie());
        when(movieRepository.findAll()).thenReturn(movies);

        // When
        List<MovieResponse> result = movieService.getAllMovies();

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.getFirst().title());
        verify(movieRepository).findAll();
    }

    @Test
    void testGetMovieWithScreenings() {
        // Given
        var movie = createTestMovie();
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        // When
        Optional<MovieResponse> result = movieService.getMovieWithScreenings(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Movie", result.get().title());
        verify(movieRepository).findById(1L);
    }

    @Test
    void testGetMovieWithScreeningsNotFound() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<MovieResponse> result = movieService.getMovieWithScreenings(1L);

        // Then
        assertFalse(result.isPresent());
        verify(movieRepository).findById(1L);
    }

    @Test
    void testGetMoviesWithAvailableScreenings() {
        // Given
        var movies = List.of(createTestMovie());
        when(movieRepository.findMoviesWithAvailableScreenings()).thenReturn(movies);

        // When
        List<MovieResponse> result = movieService.getMoviesWithAvailableScreenings();

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.getFirst().title());
        verify(movieRepository).findMoviesWithAvailableScreenings();
    }

    @Test
    void testGetTodayScreenings() {
        // Given
        var screenings = List.of(createTestScreening());
        when(screeningRepository.findTodayScreenings(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(screenings);

        // When
        List<ScreeningResponse> result = movieService.getTodayScreenings();

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.getFirst().movieTitle());
        verify(screeningRepository).findTodayScreenings(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testSearchMovies() {
        // Given
        var movies = List.of(createTestMovie());
        when(movieRepository.searchMovies("Action", "Test", null, null)).thenReturn(movies);

        // When
        List<MovieResponse> result = movieService.searchMovies("Action", "Test");

        // Then
        assertEquals(1, result.size());
        assertEquals("Action", result.getFirst().genre());
        verify(movieRepository).searchMovies("Action", "Test", null, null);
    }

    private Movie createTestMovie() {
        var movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");
        movie.setGenre("Action");
        movie.setDuration(120);
        movie.setDescription("Test description");
        movie.setScreenings(List.of(createTestScreening()));
        return movie;
    }

    private Screening createTestScreening() {
        var movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");

        var screening = new Screening();
        screening.setId(1L);
        screening.setMovie(movie);
        screening.setStartTime(LocalDateTime.now().plusHours(2));
        screening.setAvailableSeats(50);
        screening.setPrice(BigDecimal.valueOf(10.0));
        return screening;
    }
}
