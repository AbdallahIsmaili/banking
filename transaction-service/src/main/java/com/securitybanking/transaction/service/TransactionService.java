package com.securitybanking.transaction.service;

import com.securitybanking.transaction.dto.ClientDTO;
import com.securitybanking.transaction.dto.EmailRequest;
import com.securitybanking.transaction.dto.TransactionRequest;
import com.securitybanking.transaction.entity.Transaction;
import com.securitybanking.transaction.repository.TransactionRepository;
import com.securitybanking.transaction.FeignClient.AccountClient;
import com.securitybanking.transaction.FeignClient.NotificationClient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public TransactionService(TransactionRepository transactionRepository, AccountClient accountClient,
            NotificationClient notificationClient) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public Transaction createTransaction(TransactionRequest request) {
        String sourceId = request.getSourceAccountId();
        String destId = request.getDestinationAccountId();
        BigDecimal amount = request.getAmount();

        // 1. Vérifier si les comptes existent
        if (!accountClient.accountExists(sourceId)) {
            throw new IllegalArgumentException("Le compte source n'existe pas");
        }

        if (!accountClient.accountExists(destId)) {
            throw new IllegalArgumentException("Le compte destination n'existe pas");
        }

        // 2. Vérifier le solde suffisant
        boolean hasBalance = accountClient.hasSufficientBalance(sourceId, amount);
        if (!hasBalance) {
            throw new IllegalArgumentException("Solde insuffisant dans le compte source");
        }

        // Débiter le compte source
        ResponseEntity<Void> debitResponse = accountClient.updateBalance(sourceId, amount.negate());
        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Erreur lors du débit du compte source");
        }

        // Créditer le compte destination
        ResponseEntity<Void> creditResponse = accountClient.updateBalance(destId, amount);
        if (!creditResponse.getStatusCode().is2xxSuccessful()) {
            // Optionnel : rollback du débit (selon ton système, ex: Saga pattern)
            throw new IllegalStateException("Erreur lors du crédit du compte destination");
        }

        // Créer la transaction
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setSourceAccountId(sourceId);
        tx.setDestinationAccountId(destId);
        tx.setTransactionDate(LocalDateTime.now());

        String email = accountClient.getClientByAccountNumber(request.getSourceAccountId());

        // Envoyer une notification

        /*
         * EmailRequest emailRequest = new EmailRequest();
         * emailRequest.setTo(email);
         * emailRequest.setSubject("Confirmation de transaction");
         * emailRequest.setBody("Votre transaction de " + request.getAmount() +
         * " a été effectuée avec succès.");
         * 
         * notificationClient.sendEmail(emailRequest);
         * 
         */

        return transactionRepository.save(tx);
    }
}
