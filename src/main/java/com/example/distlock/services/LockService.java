package com.example.distlock.services;

public interface LockService {
    String lock();
    String lock(Time wait);
    String testWaitLock(long lockWaitSeconds, long threadSleepSeconds);
}
