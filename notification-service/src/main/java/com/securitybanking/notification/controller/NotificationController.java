package com.securitybanking.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    @GetMapping("/api/notifications")
    public String hello() {
        return "Hello from Notification Service";
    }
}
