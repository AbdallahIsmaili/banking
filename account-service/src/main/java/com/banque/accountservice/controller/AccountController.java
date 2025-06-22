package com.banque.accountservice.controller;

import com.banque.accountservice.dto.AccountCreationDTO;
import com.banque.accountservice.dto.AccountDTO;
import com.banque.accountservice.dto.AccountResponseDTO;
import com.banque.accountservice.dto.AccountUpdateDTO;
import com.banque.accountservice.model.AccountType;
import com.banque.accountservice.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:8080")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    // account-service/src/main/java/com/banque/accountservice/controller/AccountController.java

    @GetMapping("/{accountNumber}/client")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<String> getClientEmailByAccountNumber(@PathVariable String accountNumber) {
        String email = accountService.getClientEmailByAccountNumber(accountNumber);
        return ResponseEntity.ok(email);
    }

    @GetMapping("/{accountNumber}/client-id")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<Long> getClientIdByAccountNumber(@PathVariable String accountNumber) {
        Long clientId = accountService.getClientIdByAccountNumber(accountNumber);
        return ResponseEntity.ok(clientId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountCreationDTO accountCreationDTO) {
        AccountResponseDTO response = accountService.createAccount(accountCreationDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long accountId) {
        AccountDTO accountDTO = accountService.getAccountById(accountId);
        return ResponseEntity.ok(accountDTO);
    }

    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<AccountDTO> getAccountByNumber(@PathVariable String accountNumber) {
        AccountDTO accountDTO = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(accountDTO);
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<AccountDTO>> getAccountsByClientId(@PathVariable Long clientId) {
        List<AccountDTO> accounts = accountService.getAccountsByClientId(clientId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/type/{accountType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByType(@PathVariable AccountType accountType) {
        return ResponseEntity.ok(accountService.getAccountsByType(accountType));
    }

    @PatchMapping("/{accountId}/status")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<AccountResponseDTO> updateAccountStatus(
            @PathVariable Long accountId,
            @RequestParam boolean active) {
        return ResponseEntity.ok(accountService.updateAccountStatus(accountId, active));
    }

    @GetMapping("/client/{clientId}/active")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<AccountDTO>> getActiveAccountsByClientId(@PathVariable Long clientId) {
        List<AccountDTO> activeAccounts = accountService.getActiveAccountsByClientId(clientId);
        return ResponseEntity.ok(activeAccounts);
    }

    @PutMapping("/{accountNumber}/balance")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<AccountDTO> updateBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountDTO updatedAccount = accountService.updateBalance(accountNumber, amount);
        return ResponseEntity.ok(updatedAccount);
    }

    @PutMapping("/{accountId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponseDTO> closeAccount(@PathVariable Long accountId) {
        AccountResponseDTO response = accountService.closeAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/exists")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<Boolean> accountExists(@PathVariable String accountNumber) {
        boolean exists = accountService.accountExists(accountNumber);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{accountNumber}/balance/check")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<Boolean> checkSufficientBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        boolean hasSufficientBalance = accountService.hasSufficientBalance(accountNumber, amount);
        return ResponseEntity.ok(hasSufficientBalance);
    }

    @PutMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<AccountDTO> updateAccountInfo(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountUpdateDTO accountUpdateDTO) {
        accountUpdateDTO.setId(accountId); // Assurer la cohérence de l'ID
        AccountDTO updatedAccount = accountService.updateAccountInfo(accountUpdateDTO);
        return ResponseEntity.ok(updatedAccount);
    }
}