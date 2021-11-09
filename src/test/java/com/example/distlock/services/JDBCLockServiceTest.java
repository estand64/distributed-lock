package com.example.distlock.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;

import java.util.concurrent.locks.Lock;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JDBCLockServiceTest {
    private JDBCLockService service;
    private Lock lock = mock(Lock.class);
    private JdbcLockRegistry registry = mock(JdbcLockRegistry.class);

    @BeforeEach
    void setup() {
        service = new JDBCLockService(registry);
    }

    @Test
    void testLockSuccess() {
        when(lock.tryLock()).thenReturn(true);
        when(registry.obtain(anyString())).thenReturn(lock);
        service.lock();
    }

    @Test
    void testLockFails() {
        when(lock.tryLock()).thenReturn(true);
        when(registry.obtain(anyString())).thenReturn(lock);
        service.lock();
    }
}