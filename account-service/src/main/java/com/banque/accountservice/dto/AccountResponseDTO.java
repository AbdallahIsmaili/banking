package com.banque.accountservice.dto;

import java.time.LocalDateTime;

public class AccountResponseDTO {
    private String message;
    private String accountNumber;
    private boolean success;
    private LocalDateTime timestamp;

    public AccountResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public AccountResponseDTO(String message, String accountNumber, boolean success) {
        this.message = message;
        this.accountNumber = accountNumber;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }

    // Static factory methods for common responses
    public static AccountResponseDTO success(String message, String accountNumber) {
        return new AccountResponseDTO(message, accountNumber, true);
    }

    public static AccountResponseDTO failure(String message) {
        return new AccountResponseDTO(message, null, false);
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}