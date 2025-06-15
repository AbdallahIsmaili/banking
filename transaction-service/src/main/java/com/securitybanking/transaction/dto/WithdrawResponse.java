package com.securitybanking.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WithdrawResponse {
    private Long id;
    private String accountId;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String message;

    public WithdrawResponse(Long id, String accountId, BigDecimal amount, LocalDateTime transactionDate,
            String message) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.message = message;
    }

    public WithdrawResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Constructeurs + Getters & setters

}