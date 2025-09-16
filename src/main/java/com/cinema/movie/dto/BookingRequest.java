package com.cinema.movie.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO per richiesta prenotazione.
 * Solo trasferimento dati
 */
public record BookingRequest(
        @NotNull(message = "Screening ID obbligatorio")
        @Min(value = 1, message = "Screening ID deve essere positivo")
        Long screeningId,

        @NotBlank(message = "Email obbligatoria")
        @Email(message = "Email non valida")
        String userEmail,

        @NotNull(message = "Numero posti obbligatorio")
        @Min(value = 1, message = "Minimo 1 posto")
        @Max(value = 10, message = "Massimo 10 posti")
        Integer numberOfSeats
) {
    // Compact constructor per normalizzazione dati
    public BookingRequest {
        if (userEmail != null) {
            userEmail = userEmail.trim().toLowerCase();
        }
    }
}
