package com.banque.accountservice.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {

    private final String accountNumber;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;

    public InsufficientBalanceException(String accountNumber, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format("Insufficient balance in account %s. Current balance: %s, Requested amount: %s",
                accountNumber, currentBalance, requestedAmount));
        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}