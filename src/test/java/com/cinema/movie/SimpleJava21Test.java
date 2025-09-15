package com.cinema.movie;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Test semplice senza Spring per verificare Java 21
class SimpleJava21Test {

    @Test
    void testJava21Features() {
        // Test Record (Java 14+)
        record MovieInfo(String title, int duration) {}

        var movie = new MovieInfo("Avatar 3", 180);
        assertEquals("Avatar 3", movie.title());
        assertEquals(180, movie.duration());
    }

    @Test
    void testPatternMatching() {
        // Pattern Matching con switch (Java 21)
        String status = "CONFIRMED";

        String message = switch (status) {
            case "PENDING" -> "In attesa...";
            case "CONFIRMED" -> "Confermato!";
            case "CANCELLED" -> "Cancellato";
            default -> "Sconosciuto";
        };

        assertEquals("Confermato!", message);
    }

    @Test
    void testTextBlocks() {
        // Text Blocks (Java 15+)
        String sql = """
            SELECT m.title, s.start_time 
            FROM movies m 
            JOIN screenings s ON m.id = s.movie_id 
            WHERE s.available_seats > 0
            """;

        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
    }
}