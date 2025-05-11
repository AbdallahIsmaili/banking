package com.securitybanking.transaction.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    @GetMapping("/api/transactions/hello")
    public String hello() {
        return "Hello from Transaction Service";
    }
}
