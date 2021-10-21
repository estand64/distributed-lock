package com.example.distlock;

import com.example.distlock.services.LockService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.redis.util.RedisLockRegistry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistlockApplicationTests {

	@Mock
	private RedisLockRegistry redisLockRegistry;
	@Mock
	private JdbcLockRegistry jdbcLockRegistry;
	@Mock
	private Lock lock;

	private LockService lockService;

	@BeforeEach
	void setUp(){
		lockService = new LockService(redisLockRegistry, jdbcLockRegistry);
	}

	@SneakyThrows
	@Test
	void testWhereServiceIsAbleToLock() {
		when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
		when(redisLockRegistry.obtain(anyString())).thenReturn(lock);
	}

	@SneakyThrows
	@Test
	void testWhereServiceIsntAbleToLock() {
		when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(false);
		when(redisLockRegistry.obtain(anyString())).thenReturn(lock);
	}
}
