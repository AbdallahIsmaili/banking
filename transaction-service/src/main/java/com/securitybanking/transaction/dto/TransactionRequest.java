package com.securitybanking.transaction.dto;

import java.math.BigDecimal;

public class TransactionRequest {

    private BigDecimal amount;
    private Long sourceAccountId;
    private Long destinationAccountId;


    public BigDecimal getAmount() {
        return amount;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }
}

