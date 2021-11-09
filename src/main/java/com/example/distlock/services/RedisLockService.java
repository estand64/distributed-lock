package com.example.distlock.services;

import com.example.distlock.configuration.Constants;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Profile(Constants.REDIS_PROFILE)
public class RedisLockService implements LockService{
    private static final Time DEFAULT_WAIT = new Time(0, TimeUnit.NANOSECONDS);
    private static final String MY_LOCK_KEY = "someLockKey";
    private final LockRegistry lockRegistry;

    public RedisLockService(LockRegistry redisLockRegistry) {
        this.lockRegistry = redisLockRegistry;
    }

    public String lock(){
        var lock = lockRegistry.obtain(MY_LOCK_KEY);
        String returnVal = null;
        if(lock.tryLock()){
            returnVal = "redis lock successful";
        }
        else{
            returnVal = "redis lock unsuccessful";
        }
        lock.unlock();

        return returnVal;
    }

    @Override
    public void failLock() {

    }

    @Override
    public String properLock() {
        return null;
    }

    public String testWaitLock(long lockWaitSeconds, long threadSleepSeconds) {
        return null;
    }

}
