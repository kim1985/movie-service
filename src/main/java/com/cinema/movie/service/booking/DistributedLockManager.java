package com.cinema.movie.service.booking;

import com.cinema.movie.exception.BookingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Strategy Pattern per gestione distributed locks.
 * Singola responsabilità: gestire concorrenza.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DistributedLockManager {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(30);
    private static final String LOCK_PREFIX = "booking:lock:screening:";

    public <T> T executeWithLock(Long screeningId, Supplier<T> operation) {
        String lockKey = LOCK_PREFIX + screeningId;
        String lockToken = UUID.randomUUID().toString();

        if (!acquireLock(lockKey, lockToken)) {
            throw new BookingException("Sistema occupato, riprova tra poco");
        }

        try {
            return operation.get();
        } finally {
            releaseLock(lockKey, lockToken);
        }
    }

    private boolean acquireLock(String lockKey, String lockToken) {
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockToken, LOCK_TIMEOUT);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Errore acquisizione lock: {}", e.getMessage());
            return false;
        }
    }

    private void releaseLock(String lockKey, String lockToken) {
        try {
            // Usa DefaultRedisScript invece del callback deprecato
            String luaScript = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;

            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(luaScript);
            redisScript.setResultType(Long.class);

            redisTemplate.execute(redisScript, List.of(lockKey), lockToken);

        } catch (Exception e) {
            log.warn("Errore rilascio lock: {}", e.getMessage());
        }
    }
}
