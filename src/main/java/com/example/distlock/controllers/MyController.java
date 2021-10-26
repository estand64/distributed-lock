package com.example.distlock.controllers;

import com.example.distlock.services.LockServiceImpl;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MyController {
    private final LockServiceImpl lockService;

    public MyController(LockServiceImpl lockService) {
        this.lockService = lockService;
    }

    @PutMapping("/lock")
    public String lock(){
        return lockService.lock(null);
    }

    @PutMapping("/testWaitLock")
    public String waitLock(@RequestParam long waitTime, @RequestParam long sleepTime){
        return lockService.testWaitLock(waitTime, sleepTime);
    }
}
