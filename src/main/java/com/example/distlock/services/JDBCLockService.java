package com.example.distlock.services;

import com.example.distlock.configuration.Constants;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.stereotype.Service;

@Service
@Profile(Constants.JDBC_PROFILE)
public class JDBCLockService extends LockServiceImpl{
    public JDBCLockService(JdbcLockRegistry jdbcLockRegistry) {
        super(jdbcLockRegistry);
    }

    @Override
    public String lock() {
        return super.lock("jdbc");
    }

    @Override
    public String lock(Time wait) {
        return super.lock(wait, "jdbc");
    }
}
