package com.example.distlock.configuration;

import com.example.distlock.services.LockableActions;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Constants {
    public static final String JDBC_PROFILE = "jdbc";
    public static final String REDIS_PROFILE = "redis";

    public static final LockableActions<String> createActions(String typeOfLock){
        Supplier<String> success = () -> String.format("successfully locked with %s", typeOfLock);
        Supplier<String> failure = () -> String.format("failed to lock with %s", typeOfLock);
        Function<Exception, String> exception = (e) -> String.format("%s lock went off the rails on a crazy train: %s", typeOfLock, e.getMessage());

        return new LockableActions<>(success, failure, exception);
    }
}
