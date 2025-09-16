package com.cinema.movie.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleBookingException() {
        // Given
        var exception = new BookingException("Test booking error");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleBookingException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BOOKING_ERROR", response.getBody().code());
        assertEquals("Test booking error", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void testHandleValidationException() throws Exception {
        // Given - Create proper MethodParameter for constructor
        Method method = GlobalExceptionHandlerTest.class.getMethod("dummyMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        // Create proper binding result with target object
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "bookingRequest");
        bindingResult.addError(new FieldError("bookingRequest", "userEmail", "Email obbligatoria"));

        // Create exception with proper parameters
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response =
                exceptionHandler.handleValidationException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertTrue(response.getBody().fieldErrors().containsKey("userEmail"));
        assertEquals("Email obbligatoria", response.getBody().fieldErrors().get("userEmail"));
    }

    @Test
    void testHandleGenericException() {
        // Given
        var exception = new RuntimeException("Generic error");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("Errore interno del server", response.getBody().message());
    }

    // Dummy method needed for MethodParameter creation
    public void dummyMethod(String param) {
        // Used only for creating MethodParameter in tests
    }
}
