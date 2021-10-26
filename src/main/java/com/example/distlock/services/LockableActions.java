package com.example.distlock.services;

import java.util.function.Function;
import java.util.function.Supplier;

public record LockableActions<T>(Supplier<T> onSuccess, Supplier<T> onFailure,
                                 Function<Exception, T> onError) { }
