package com.example.distlock.services;

import com.example.distlock.configuration.Constants;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

@Service
@Profile(Constants.REDIS_PROFILE)
public class RedisLockService extends LockServiceImpl{
    public RedisLockService(RedisLockRegistry lockRegistry) {
        super(lockRegistry);
    }

    @Override
    public String lock() {
        return super.lock("redis");
    }

    @Override
    public String lock(Time wait) {
        return super.lock(wait, "redis");
    }
}
