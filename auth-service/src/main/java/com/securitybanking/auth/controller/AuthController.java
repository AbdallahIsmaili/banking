package com.securitybanking.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/api/auth/hello")
    public String hello() {
        return "Hello from Auth Service";
    }
}
