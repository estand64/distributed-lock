package com.example.distlock.services;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class RedisLockServiceTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0.2")
            .withExposedPorts(6379);

    static LettuceConnectionFactory connectionFactory;

    @BeforeAll
    static void beforeAll() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setPort(redis.getFirstMappedPort());

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .clientOptions(
                        ClientOptions.builder()
                                .socketOptions(
                                        SocketOptions.builder()
                                                .connectTimeout(Duration.ofMillis(10000))
                                                .keepAlive(true)
                                                .build())
                                .build())
                .commandTimeout(Duration.ofSeconds(10000))
                .build();

        connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfiguration);
        connectionFactory.afterPropertiesSet();
    }

    @AfterEach
    void cleanUp(){
        connectionFactory.getConnection().serverCommands().flushAll();
    }

    @Test
    void testLock() {
        RedisLockRegistry redisLockRegistry = new RedisLockRegistry(connectionFactory, "lock_prefix");
        RedisLockService service = new RedisLockService(redisLockRegistry);
        assertThat(service.lock()).isEqualTo("redis lock successful");
    }

    @Test
    void testLockFails() {
        RedisLockRegistry redisLockRegistryOfAnotherProcess = new RedisLockRegistry(connectionFactory, "lock_prefix");
        Lock lockOfAnotherProcess = redisLockRegistryOfAnotherProcess.obtain("someLockKey");
        lockOfAnotherProcess.lock();

        RedisLockRegistry redisLockRegistry = new RedisLockRegistry(connectionFactory, "lock_prefix");
        RedisLockService service = new RedisLockService(redisLockRegistry);

        assertThat(service.lock()).isEqualTo("redis lock unsuccessful");
    }
}
