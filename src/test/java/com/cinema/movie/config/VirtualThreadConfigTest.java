package com.cinema.movie.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class VirtualThreadConfigTest {

    @Test
    void testVirtualThreadExecutor() {
        VirtualThreadConfig config = new VirtualThreadConfig();
        Executor executor = config.virtualThreadExecutor();
        assertNotNull(executor);
    }

    @Test
    void testAsyncExecutor() {
        VirtualThreadConfig config = new VirtualThreadConfig();
        Executor executor = config.getAsyncExecutor();
        assertNotNull(executor);
    }
}
