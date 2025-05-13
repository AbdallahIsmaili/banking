package com.banque.accountservice.controller;

import com.banque.accountservice.dto.AccountCreationDTO;
import com.banque.accountservice.dto.AccountDTO;
import com.banque.accountservice.dto.AccountResponseDTO;
import com.banque.accountservice.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountCreationDTO accountCreationDTO) {
        AccountResponseDTO response = accountService.createAccount(accountCreationDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long accountId) {
        AccountDTO accountDTO = accountService.getAccountById(accountId);
        return ResponseEntity.ok(accountDTO);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccountByNumber(@PathVariable String accountNumber) {
        AccountDTO accountDTO = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(accountDTO);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountDTO>> getAccountsByClientId(@PathVariable Long clientId) {
        List<AccountDTO> accounts = accountService.getAccountsByClientId(clientId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/client/{clientId}/active")
    public ResponseEntity<List<AccountDTO>> getActiveAccountsByClientId(@PathVariable Long clientId) {
        List<AccountDTO> activeAccounts = accountService.getActiveAccountsByClientId(clientId);
        return ResponseEntity.ok(activeAccounts);
    }

    @PutMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountDTO> updateBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountDTO updatedAccount = accountService.updateBalance(accountNumber, amount);
        return ResponseEntity.ok(updatedAccount);
    }

    @PutMapping("/{accountId}/close")
    public ResponseEntity<AccountResponseDTO> closeAccount(@PathVariable Long accountId) {
        AccountResponseDTO response = accountService.closeAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/exists")
    public ResponseEntity<Boolean> accountExists(@PathVariable String accountNumber) {
        boolean exists = accountService.accountExists(accountNumber);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{accountNumber}/balance/check")
    public ResponseEntity<Boolean> checkSufficientBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        boolean hasSufficientBalance = accountService.hasSufficientBalance(accountNumber, amount);
        return ResponseEntity.ok(hasSufficientBalance);
    }
}