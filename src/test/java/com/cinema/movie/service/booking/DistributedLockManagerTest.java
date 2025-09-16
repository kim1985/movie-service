package com.cinema.movie.service.booking;

import com.cinema.movie.exception.BookingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributedLockManagerTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private DistributedLockManager lockManager;

    @Test
    void testExecuteWithLock() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        Supplier<String> operation = () -> "success";

        // When
        String result = lockManager.executeWithLock(1L, operation);

        // Then
        assertEquals("success", result);
        verify(valueOperations).setIfAbsent(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void testExecuteWithLockFailsToAcquire() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);

        Supplier<String> operation = () -> "success";

        // When & Then
        assertThrows(BookingException.class,
                () -> lockManager.executeWithLock(1L, operation));
        verify(valueOperations).setIfAbsent(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void testExecuteWithLockRedisException() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenThrow(new RuntimeException("Redis error"));

        Supplier<String> operation = () -> "success";

        // When & Then
        assertThrows(BookingException.class,
                () -> lockManager.executeWithLock(1L, operation));
    }
}
