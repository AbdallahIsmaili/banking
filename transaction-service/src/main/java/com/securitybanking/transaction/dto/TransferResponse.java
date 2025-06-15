package com.securitybanking.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferResponse {
    private Long id;
    private String sourceAccountId;
    private String destinationAccountId;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String message;

    public TransferResponse(Long id, String sourceAccountId, String destinationAccountId, BigDecimal amount,
            LocalDateTime transactionDate, String message) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.message = message;
    }

    public TransferResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
