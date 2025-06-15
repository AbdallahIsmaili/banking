package com.securitybanking.transaction.dto;

import java.math.BigDecimal;

public class DepositRequest {
    private String accountId;
    private BigDecimal amount;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public DepositRequest() {
    }

    // Getters & setters

}
