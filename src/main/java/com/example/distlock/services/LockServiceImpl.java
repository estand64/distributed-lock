package com.example.distlock.services;

import org.springframework.integration.support.locks.LockRegistry;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class LockServiceImpl implements LockService{
    private static final Time DEFAULT_WAIT = new Time(0, TimeUnit.NANOSECONDS);
    private static final String MY_LOCK_KEY = "someLockKey";
    private final LockRegistry lockRegistry;

    protected LockServiceImpl(LockRegistry lockRegistry) {
        this.lockRegistry = lockRegistry;
    }

    protected String lock(Time wait, String type){
        var actions = createActions(type);
        return lock(lockRegistry, actions, wait);
    }

    protected String lock(String type){
        var actions = createActions(type);
        return lock(lockRegistry, actions, DEFAULT_WAIT);
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

    private LockableActions<String> createActions(String typeOfLock){
        Supplier<String> success = () -> String.format("successfully locked with %s", typeOfLock);
        Supplier<String> failure = () -> String.format("failed to lock with %s", typeOfLock);
        Function<Exception, String> exception = (e) -> String.format("%s lock went off the rails on a crazy train: %s", typeOfLock, e.getMessage());

        return new LockableActions<>(success, failure, exception);
    }
}

