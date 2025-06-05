package com.securitybanking.transaction.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransactionRequest {
    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @Positive
    private String sourceAccountId;

    @Positive
    private String destinationAccountId;

    // Getters et Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
}