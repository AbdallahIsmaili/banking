package com.securitybanking.transaction.controller;

import com.securitybanking.transaction.dto.TransactionRequest;
import com.securitybanking.transaction.entity.Transaction;
import com.securitybanking.transaction.service.TransactionService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionRequest request) {
        Transaction tx = transactionService.createTransaction(request);
        return new ResponseEntity<>(tx, HttpStatus.CREATED);
    }

    @GetMapping("/debug-auth")
    public ResponseEntity<String> debugAuth(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("Token reçu dans transaction-service: " + authHeader);
        return ResponseEntity.ok("Token reçu");
    }

}
