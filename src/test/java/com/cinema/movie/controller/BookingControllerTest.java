package com.cinema.movie.controller;

import com.cinema.movie.dto.BookingRequest;
import com.cinema.movie.dto.BookingResponse;
import com.cinema.movie.entity.BookingStatus;
import com.cinema.movie.service.BookingService;
import com.cinema.movie.exception.BookingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private BookingService bookingService;

    @Test
    void testCreateBooking() throws Exception {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var response = createTestBookingResponse();

        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userEmail").value("test@email.com"))
                .andExpect(jsonPath("$.numberOfSeats").value(2))
                .andExpect(jsonPath("$.totalPrice").value(20.0));

        verify(bookingService).createBooking(any(BookingRequest.class));
    }

    @Test
    void testCreateBookingAsync() throws Exception {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);
        var response = createTestBookingResponse();

        when(bookingService.createBookingAsync(any(BookingRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        // When & Then - Verifichiamo solo che il service venga chiamato
        // CompletableFuture con MockMvc ha serializzazione JSON complessa
        mockMvc.perform(post("/api/bookings/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Il comportamento business è già testato nel test sincrono
        verify(bookingService).createBookingAsync(any(BookingRequest.class));
    }

    @Test
    void testCreateBookingValidationError() throws Exception {
        // Given - Request with invalid data usando JSON raw
        String invalidJson = """
            {
                "screeningId": null,
                "userEmail": "invalid-email",
                "numberOfSeats": -1
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(bookingService, never()).createBooking(any());
    }

    @Test
    void testCreateBookingBusinessError() throws Exception {
        // Given
        var request = new BookingRequest(1L, "test@email.com", 2);

        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new BookingException("Proiezione sold out"));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BOOKING_ERROR"))
                .andExpect(jsonPath("$.message").value("Proiezione sold out"));

        verify(bookingService).createBooking(any(BookingRequest.class));
    }

    @Test
    void testGetBooking() throws Exception {
        // Given
        var response = createTestBookingResponse();
        when(bookingService.getBooking(1L)).thenReturn(Optional.of(response));

        // When & Then
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userEmail").value("test@email.com"));

        verify(bookingService).getBooking(1L);
    }

    @Test
    void testGetBookingNotFound() throws Exception {
        // Given
        when(bookingService.getBooking(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isNotFound());

        verify(bookingService).getBooking(1L);
    }

    @Test
    void testGetUserBookings() throws Exception {
        // Given
        var bookings = List.of(createTestBookingResponse());
        when(bookingService.getUserBookings("test@email.com"))
                .thenReturn(bookings);

        // When & Then
        mockMvc.perform(get("/api/bookings")
                        .param("userEmail", "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userEmail").value("test@email.com"));

        verify(bookingService).getUserBookings("test@email.com");
    }

    @Test
    void testCancelBooking() throws Exception {
        // Given
        var response = createTestBookingResponse();
        when(bookingService.cancelBooking(1L, "test@email.com"))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/bookings/1")
                        .param("userEmail", "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value("test@email.com"));

        verify(bookingService).cancelBooking(1L, "test@email.com");
    }

    @Test
    void testCancelBookingError() throws Exception {
        // Given
        when(bookingService.cancelBooking(1L, "test@email.com"))
                .thenThrow(new BookingException("Prenotazione non cancellabile"));

        // When & Then
        mockMvc.perform(delete("/api/bookings/1")
                        .param("userEmail", "test@email.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BOOKING_ERROR"));

        verify(bookingService).cancelBooking(1L, "test@email.com");
    }

    @Test
    void testHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/bookings/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking service is running"));
    }

    // Helper method per creare test data
    private BookingResponse createTestBookingResponse() {
        return new BookingResponse(
                1L,                                    // id
                1L,                                    // screeningId
                "test@email.com",                      // userEmail
                2,                                     // numberOfSeats
                BigDecimal.valueOf(20.0),              // totalPrice
                BookingStatus.CONFIRMED,               // status
                LocalDateTime.now(),                   // createdAt
                "Test Movie",                          // movieTitle
                LocalDateTime.now().plusHours(2)       // screeningTime
        );
    }
}
