package com.banque.accountservice.client;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void sendAccountCreationNotification(Long clientId, String accountNumber) {
        // Log the failure
        System.err.println("FALLBACK: Failed to send account creation notification for client " +
                clientId + " and account " + accountNumber);
    }

    @Override
    public void sendDepositNotification(Long clientId, String accountNumber, BigDecimal amount) {
        // Log the failure
        System.err.println("FALLBACK: Failed to send deposit notification for client " +
                clientId + " and account " + accountNumber + " with amount " + amount);
    }

    @Override
    public void sendWithdrawalNotification(Long clientId, String accountNumber, BigDecimal amount) {
        // Log the failure
        System.err.println("FALLBACK: Failed to send withdrawal notification for client " +
                clientId + " and account " + accountNumber + " with amount " + amount);
    }

    @Override
    public void sendAccountClosureNotification(Long clientId, String accountNumber) {
        // Log the failure
        System.err.println("FALLBACK: Failed to send account closure notification for client " +
                clientId + " and account " + accountNumber);
    }
}