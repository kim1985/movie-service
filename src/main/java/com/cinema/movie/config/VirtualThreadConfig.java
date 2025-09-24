package com.cinema.movie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configurazione Virtual Threads Java 21.
 * Abilita Virtual Threads per gestire alta concorrenza con overhead minimo.
 */
@Configuration
@EnableAsync
public class VirtualThreadConfig implements AsyncConfigurer {

    /**
     * Virtual Thread Executor per operazioni asincrone ad alta concorrenza.
     * Java 21 feature: ogni task ottiene un Virtual Thread dedicato.
     */
    @Bean("virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        // Java 21: crea un Virtual Thread per ogni task
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Configurazione default per @Async senza nome specificato.
     */
    @Override
    public Executor getAsyncExecutor() {
        return virtualThreadExecutor();
    }
}