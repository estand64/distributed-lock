package com.example.distlock.services;

import com.example.distlock.DistlockApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = DistlockApplication.class)
class JDBCLockServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.3")
            .withUsername("user")
            .withPassword("password");

    @Autowired
    private JDBCLockService service;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DefaultLockRepository lockRepository;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
    }

    @AfterEach
    void cleanUp(){
        this.cleanUp(lockRepository);
    }

    void cleanUp(DefaultLockRepository lockRepository){
        lockRepository.close();
    }

    @Test
    void testLockSuccess() {
        assertThat(service.lock()).isEqualTo("jdbc lock successful");
    }

    @Test
    void testLockFails() {
        DefaultLockRepository lockRepositoryOfAnotherProcess = new DefaultLockRepository(dataSource);
        lockRepositoryOfAnotherProcess.afterPropertiesSet();
        JdbcLockRegistry lockRegistryOfAnotherProcess = new JdbcLockRegistry(lockRepositoryOfAnotherProcess);

        Lock lockOfAnotherProcess = lockRegistryOfAnotherProcess.obtain("someLockKey");
        lockOfAnotherProcess.lock();

        assertThat(service.lock()).isEqualTo("jdbc lock unsuccessful");
        this.cleanUp(lockRepositoryOfAnotherProcess);
    }

    @Test
    void testProperLock() {
        assertThat(service.properLock()).isEqualTo("jdbc lock successful");
    }

    @Test
    void testProperLockFails() {
        DefaultLockRepository lockRepositoryOfAnotherProcess = new DefaultLockRepository(dataSource);
        lockRepositoryOfAnotherProcess.afterPropertiesSet();
        JdbcLockRegistry lockRegistryOfAnotherProcess = new JdbcLockRegistry(lockRepositoryOfAnotherProcess);

        Lock lockOfAnotherProcess = lockRegistryOfAnotherProcess.obtain("someLockKey");
        lockOfAnotherProcess.lock();

        assertThat(service.properLock()).isEqualTo("jdbc lock unsuccessful");
        this.cleanUp(lockRepositoryOfAnotherProcess);
    }
}