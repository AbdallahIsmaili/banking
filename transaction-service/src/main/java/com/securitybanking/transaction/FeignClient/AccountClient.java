package com.securitybanking.transaction.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.securitybanking.transaction.dto.ClientDTO;

import java.math.BigDecimal;

@FeignClient(name = "ACCOUNT-SERVICE", url = "${account.service.url}")
public interface AccountClient {

        @GetMapping("/api/accounts/{accountNumber}/exists")
        boolean accountExists(@PathVariable String accountNumber);

        @GetMapping("/api/accounts/{accountNumber}/balance/check")
        boolean hasSufficientBalance(
                        @PathVariable String accountNumber,
                        @RequestParam BigDecimal amount);

        @PutMapping("/api/accounts/{accountNumber}/balance")
        ResponseEntity<Void> updateBalance(
                        @PathVariable String accountNumber,
                        @RequestParam BigDecimal amount);

        @GetMapping("/api/accounts/{accountNumber}/client")
        String getClientByAccountNumber(@PathVariable("accountNumber") String accountNumber);
}