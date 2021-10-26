package com.example.distlock.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;

import javax.sql.DataSource;

@Configuration
public class JDBCConfiguration {

    @Bean
    @Profile(Constants.JDBC_PROFILE)
    public DefaultLockRepository DefaultLockRepository(DataSource dataSource){
        return new DefaultLockRepository(dataSource);
    }

    @Bean
    @Profile(Constants.JDBC_PROFILE)
    public JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository){
        return new JdbcLockRegistry(lockRepository);
    }
}
