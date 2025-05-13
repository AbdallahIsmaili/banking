package com.banque.accountservice.dto;

import com.banque.accountservice.model.AccountType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AccountCreationDTO {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    private BigDecimal initialDeposit = BigDecimal.ZERO;

    // Getters and Setters
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
}