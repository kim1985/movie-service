package com.cinema.movie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "number_of_seats", nullable = false)
    private Integer numberOfSeats;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }

    // Java 21 Pattern Matching per messaggi di stato
    public String getStatusMessage() {
        return switch (status) {
            case PENDING -> "Prenotazione in corso...";
            case CONFIRMED -> "Prenotazione confermata!";
            case CANCELLED -> "Prenotazione cancellata";
            case EXPIRED -> "Prenotazione scaduta";
        };
    }

    // Metodi business
    public void confirm() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Can only confirm pending bookings");
        }
        this.status = BookingStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING) {
            this.status = BookingStatus.CANCELLED;
        } else {
            throw new IllegalStateException("Cannot cancel booking in status: " + status);
        }
    }

    public boolean canBeCancelled() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING;
    }
}

