package com.example.distlock.services;

import java.util.concurrent.TimeUnit;

public record Time(long duration, TimeUnit timeUnit){ }
