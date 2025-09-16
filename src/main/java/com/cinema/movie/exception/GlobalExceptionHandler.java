package com.cinema.movie.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler per API REST.
 * Centralizza gestione errori per tutti i controller.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Gestisce errori business delle prenotazioni.
     */
    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ErrorResponse> handleBookingException(BookingException e) {
        log.warn("Errore prenotazione: {}", e.getMessage());

        ErrorResponse error = new ErrorResponse(
                "BOOKING_ERROR",
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Gestisce errori di validazione input.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {

        log.warn("Errore validazione: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse response = new ValidationErrorResponse(
                "VALIDATION_ERROR",
                "Errori di validazione input",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Gestisce errori generici.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Errore generico", e);

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "Errore interno del server",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Record per risposta errore standard.
     */
    public record ErrorResponse(
            String code,
            String message,
            LocalDateTime timestamp
    ) {}

    /**
     * Record per errori di validazione con dettagli.
     */
    public record ValidationErrorResponse(
            String code,
            String message,
            Map<String, String> fieldErrors,
            LocalDateTime timestamp
    ) {}
}
