package com.securitybanking.transaction.controller;

import com.securitybanking.transaction.dto.*;
import com.securitybanking.transaction.service.TransactionService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Virement (transfert entre deux comptes)
    @PreAuthorize("hasAnyRole('USER'),'ADMIN')")
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    // Dépôt sur un compte
    @PreAuthorize("hasAnyRole('USER'),'ADMIN')")
    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(@RequestBody DepositRequest request) {
        return ResponseEntity.ok(transactionService.deposit(request));
    }

    // Retrait d'un compte
    @PreAuthorize("hasAnyRole('USER'),'ADMIN')")
    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponse> withdraw(@RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(transactionService.withdraw(request));
    }
}