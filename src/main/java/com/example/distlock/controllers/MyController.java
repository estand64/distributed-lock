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

    @PutMapping("/lock")
    public String lock(){
        return lockService.lock();
    }

    @PutMapping("/properLock")
    public String properLock(){
        return lockService.properLock();
    }

    @PutMapping("/failLock")
    public String failLock(){
        lockService.failLock();
        return "fail lock called, output in logs";
    }

//    @PutMapping("/testWaitLock")
//    public String waitLock(@RequestParam long waitTime, @RequestParam long sleepTime){
//        return lockService.testWaitLock(waitTime, sleepTime);
//    }
}
