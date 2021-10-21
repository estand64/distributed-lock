package com.example.distlock.controllers;

import com.example.distlock.services.LockService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MyController {
    private final LockService lockService;

    public MyController(LockService lockService) {
        this.lockService = lockService;
    }

    @PutMapping("/lockRedis")
    public String lockRedis(){
        return lockService.redisLock(null);
    }

    @PutMapping("/lockJdbc")
    public String lockJdbc(){
        return lockService.jdbcLock(null);
    }

    @PutMapping("/testWaitLock")
    public String waitLock(@RequestParam long waitTime, @RequestParam long sleepTime){
        return lockService.testWaitLock(waitTime, sleepTime);
    }
}
