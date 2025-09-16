package com.cinema.movie.controller;

import com.cinema.movie.dto.MovieResponse;
import com.cinema.movie.dto.ScreeningResponse;
import com.cinema.movie.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private MovieService movieService;

    @Test
    void testGetAllMovies() throws Exception {
        // Given
        var movies = List.of(createTestMovieResponse());
        when(movieService.getAllMovies()).thenReturn(movies);

        // When & Then
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Movie"))
                .andExpect(jsonPath("$[0].genre").value("Action"));

        verify(movieService).getAllMovies();
    }

    @Test
    void testGetMovie() throws Exception {
        // Given
        var movie = createTestMovieResponse();
        when(movieService.getMovieWithScreenings(1L)).thenReturn(Optional.of(movie));

        // When & Then
        mockMvc.perform(get("/api/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Movie"))
                .andExpect(jsonPath("$.screenings").isArray());

        verify(movieService).getMovieWithScreenings(1L);
    }

    @Test
    void testGetMovieNotFound() throws Exception {
        // Given
        when(movieService.getMovieWithScreenings(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/movies/1"))
                .andExpect(status().isNotFound());

        verify(movieService).getMovieWithScreenings(1L);
    }

    @Test
    void testGetAvailableMovies() throws Exception {
        // Given
        var movies = List.of(createTestMovieResponse());
        when(movieService.getMoviesWithAvailableScreenings()).thenReturn(movies);

        // When & Then
        mockMvc.perform(get("/api/movies/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Movie"));

        verify(movieService).getMoviesWithAvailableScreenings();
    }

    @Test
    void testGetTodayScreenings() throws Exception {
        // Given
        var screenings = List.of(createTestScreeningResponse());
        when(movieService.getTodayScreenings()).thenReturn(screenings);

        // When & Then
        mockMvc.perform(get("/api/movies/screenings/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].movieTitle").value("Test Movie"))
                .andExpect(jsonPath("$[0].availableSeats").value(50));

        verify(movieService).getTodayScreenings();
    }

    @Test
    void testSearchMovies() throws Exception {
        // Given
        var movies = List.of(createTestMovieResponse());
        when(movieService.searchMovies("Action", "Test")).thenReturn(movies);

        // When & Then
        mockMvc.perform(get("/api/movies/search")
                        .param("genre", "Action")
                        .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].genre").value("Action"));

        verify(movieService).searchMovies("Action", "Test");
    }

    @Test
    void testSearchMoviesNoParameters() throws Exception {
        // Given
        var movies = List.of(createTestMovieResponse());
        when(movieService.searchMovies(null, null)).thenReturn(movies);

        // When & Then
        mockMvc.perform(get("/api/movies/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(movieService).searchMovies(null, null);
    }

    @Test
    void testSearchMoviesEmptyResult() throws Exception {
        // Given
        when(movieService.searchMovies("Horror", "NonExistent")).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/movies/search")
                        .param("genre", "Horror")
                        .param("title", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(movieService).searchMovies("Horror", "NonExistent");
    }

    private MovieResponse createTestMovieResponse() {
        var screening = createTestScreeningResponse();

        return new MovieResponse(
                1L,                           // id
                "Test Movie",                 // title
                "Action",                     // genre
                120,                          // duration
                "Test movie description",      // description
                List.of(screening)            // screenings
        );
    }

    private ScreeningResponse createTestScreeningResponse() {
        return new ScreeningResponse(
                1L,                                    // id
                1L,                                    // movieId
                "Test Movie",                          // movieTitle
                LocalDateTime.now().plusHours(2),      // startTime
                50,                                    // availableSeats
                BigDecimal.valueOf(10.0)               // price
        );
    }
}
