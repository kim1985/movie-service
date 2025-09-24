package com.cinema.movie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configurazione Redis per distributed locking.
 * Previene race conditions nelle prenotazioni simultanee.
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate per operazioni di locking distribuito.
     * Usa String serializer per performance ottimali sui lock.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer per chiavi e valori dei lock
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }
}