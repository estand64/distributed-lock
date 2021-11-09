package com.example.distlock.services;

import com.example.distlock.configuration.Constants;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Service
@Profile(Constants.JDBC_PROFILE)
public class JDBCLockService implements LockService{
    private static final Time DEFAULT_WAIT = new Time(0, TimeUnit.NANOSECONDS);
    private static final String MY_LOCK_KEY = "someLockKey";
    private final LockRegistry lockRegistry;

    public JDBCLockService(JdbcLockRegistry jdbcLockRegistry) {
        this.lockRegistry = jdbcLockRegistry;
    }

    @Override
    public String lock(){
        var lock = lockRegistry.obtain(MY_LOCK_KEY);
        String returnVal = null;
        if(lock.tryLock()){
            returnVal = "jdbc lock successful";
        }
        else{
            returnVal = "jdbc lock unsuccessful";
        }
        lock.unlock();

        return returnVal;
    }

    @Override
    public String properLock() {
        Lock lock = null;
        try {
            lock = lockRegistry.obtain(MY_LOCK_KEY);
        } catch (Exception e) {
            // in a production environment this should be a log statement
            System.out.println(String.format("Unable to obtain lock: %s", MY_LOCK_KEY));
        }
        String returnVal = null;
        try {
            if (lock.tryLock()) {
                returnVal =  "jdbc lock successful";
            }
            else{
                returnVal = "jdbc lock unsuccessful";
            }
        } catch (Exception e) {
            // in a production environment this should log and do something else
            e.printStackTrace();
        } finally {
            // always have this in a `finally` block in case anything goes wrong
            lock.unlock();
        }

        return returnVal;
    }

    public void failLock(){
        var executor = Executors.newFixedThreadPool(2);
        Runnable lockThreadOne = () -> {
            UUID uuid = UUID.randomUUID();
            StringBuilder sb = new StringBuilder();
            var lock = lockRegistry.obtain(MY_LOCK_KEY);
            try {
                System.out.println("Attempting to lock with thread: " + uuid);
                if(lock.tryLock(1, TimeUnit.SECONDS)){
                    System.out.println("Locked with thread: " + uuid);
                    Thread.sleep(5000);
                }
                else{
                    System.out.println("failed to lock with thread: " + uuid);
                }
            } catch(Exception e0){
                System.out.println("exception thrown with thread: " + uuid);
            } finally {
                lock.unlock();
                System.out.println("unlocked with thread: " + uuid);
            }
        };

        Runnable lockThreadTwo = () -> {
            UUID uuid = UUID.randomUUID();
            StringBuilder sb = new StringBuilder();
            var lock = lockRegistry.obtain(MY_LOCK_KEY);
            try {
                System.out.println("Attempting to lock with thread: " + uuid);
                if(lock.tryLock(1, TimeUnit.SECONDS)){
                    System.out.println("Locked with thread: " + uuid);
                    Thread.sleep(5000);
                }
                else{
                    System.out.println("failed to lock with thread: " + uuid);
                }
            } catch(Exception e0){
                System.out.println("exception thrown with thread: " + uuid);
            } finally {
                lock.unlock();
                System.out.println("unlocked with thread: " + uuid);
            }
        };
        executor.submit(lockThreadOne);
        executor.submit(lockThreadTwo);
        executor.shutdown();
    }

    public String testWaitLock(long lockWaitSeconds, long threadSleepSeconds) {
        var executor = Executors.newFixedThreadPool(2);
        var waitTime = new Time(lockWaitSeconds, TimeUnit.SECONDS);
        var threadOne = createTestThread(lockRegistry, waitTime, threadSleepSeconds);
        var threadTwo = createTestThread(lockRegistry, waitTime, threadSleepSeconds);

        executor.submit(threadOne);
        executor.submit(threadTwo);

        executor.shutdown();

        return "did lock stuff";
    }

    private Runnable createTestThread(LockRegistry lockRegistry, Time waitTime, long threadSleepSeconds){
        return () -> {
            var halfSleep = (threadSleepSeconds*1000)/2;
            var id = UUID.randomUUID();
            Lock lock = null;
            try {
                lock = lockRegistry.obtain(MY_LOCK_KEY);
            } catch (Exception e) {
                System.out.println(String.format("Unable to obtain lock: %s", MY_LOCK_KEY));
            }

            try {
                System.out.println("attempting to grab a lock for thread: " + id);
                if (lock.tryLock(waitTime.duration(), waitTime.timeUnit())) {
                    System.out.println("Locking the thread: " + id);
                    Thread.sleep(halfSleep);
                    System.out.println(id + " is doing some processing!!!");
                    Thread.sleep(halfSleep);
                }
                else{
                    System.out.println("Unable to lock the thread: " + id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                System.out.println("Unlocking the thread: " + id);
            }
        };
    }

    private <T> T lock(LockRegistry lockRegistry, LockableActions<T> lockableActions, Time waitTime){
        Lock lock = null;
        try {
            //even though method takes an object due to the distributed lock interface both LockRegistries force
            // you to use a string as the key
            lock = lockRegistry.obtain(MY_LOCK_KEY);
        } catch (Exception e){
            System.out.println(String.format("Unable to obtain lock: %s", MY_LOCK_KEY));
            return lockableActions.onError().apply(e);
        }

        T successful;
        try{
            waitTime = waitTime == null ? DEFAULT_WAIT : waitTime;
            successful = lock.tryLock(waitTime.duration(), waitTime.timeUnit()) ? lockableActions.onSuccess().get() : lockableActions.onFailure().get();
        } catch (Exception e){
            //this should be a log message
            e.printStackTrace();
            successful = lockableActions.onError().apply(e);
        } finally {
            try {
                lock.unlock();
            }catch (Exception e){
                //we don't care about this case as if we can't unlock it, it means this lock is owned by someone else
            }
        }
        return successful;
    }
}
