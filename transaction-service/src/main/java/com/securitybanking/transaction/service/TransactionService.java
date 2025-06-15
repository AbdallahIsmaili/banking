package com.securitybanking.transaction.service;

import com.securitybanking.transaction.dto.ClientDTO;
import com.securitybanking.transaction.dto.DepositRequest;
import com.securitybanking.transaction.dto.DepositResponse;
import com.securitybanking.transaction.dto.EmailRequest;
import com.securitybanking.transaction.dto.TransactionRequest;
import com.securitybanking.transaction.dto.TransferRequest;
import com.securitybanking.transaction.dto.TransferResponse;
import com.securitybanking.transaction.dto.WithdrawRequest;
import com.securitybanking.transaction.dto.WithdrawResponse;
import com.securitybanking.transaction.entity.Transaction;
import com.securitybanking.transaction.repository.TransactionRepository;
import com.securitybanking.transaction.FeignClient.AccountClient;
import com.securitybanking.transaction.FeignClient.NotificationClient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public TransactionService(TransactionRepository transactionRepository, AccountClient accountClient,
            NotificationClient notificationClient) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    // Dépôt
    public DepositResponse deposit(DepositRequest request) {
        validateAmount(request.getAmount());
        String accountId = request.getAccountId();

        logger.info("Dépôt sur compte {} montant {}", mask(accountId), request.getAmount());

        if (!accountClient.accountExists(accountId)) {
            throw new IllegalArgumentException("Compte inexistant");
        }

        accountClient.updateBalance(accountId, request.getAmount());

        Transaction tx = saveTransaction(null, accountId, request.getAmount(), "DEPOT");

        return buildDepositResponse(tx);
    }

    // Retrait
    public WithdrawResponse withdraw(WithdrawRequest request) {
        validateAmount(request.getAmount());
        String accountId = request.getAccountId();

        logger.info("Retrait sur compte {} montant {}", mask(accountId), request.getAmount());

        if (!accountClient.accountExists(accountId)) {
            throw new IllegalArgumentException("Compte inexistant");
        }

        if (!accountClient.hasSufficientBalance(accountId, request.getAmount())) {
            logger.warn("Solde insuffisant pour {}", mask(accountId));
            throw new RuntimeException("Solde insuffisant");
        }

        accountClient.updateBalance(accountId, request.getAmount().negate());

        Transaction tx = saveTransaction(accountId, null, request.getAmount(), "RETRAIT");

        return buildWithdrawResponse(tx);
    }

    // Virement
    public TransferResponse transfer(TransferRequest request) {
        validateAmount(request.getAmount());
        String sourceAccountId = request.getSourceAccountId();
        String destAccountId = request.getDestinationAccountId();

        logger.info("Virement de {} vers {} pour montant {}",
                mask(sourceAccountId), mask(destAccountId), request.getAmount());

        if (sourceAccountId.equals(destAccountId)) {
            throw new IllegalArgumentException("Source et destination identiques");
        }

        if (!accountClient.accountExists(sourceAccountId) || !accountClient.accountExists(destAccountId)) {
            throw new IllegalArgumentException("Compte(s) inexistant(s)");
        }

        if (!accountClient.hasSufficientBalance(sourceAccountId, request.getAmount())) {
            logger.warn("Solde insuffisant pour {}", mask(sourceAccountId));
            throw new RuntimeException("Solde insuffisant");
        }

        accountClient.updateBalance(sourceAccountId, request.getAmount().negate());
        accountClient.updateBalance(destAccountId, request.getAmount());

        Transaction tx = saveTransaction(sourceAccountId, destAccountId, request.getAmount(), "VIREMENT");

        return buildTransferResponse(tx);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant invalide");
        }
    }

    private Transaction saveTransaction(String source, String dest, BigDecimal amount, String type) {
        Transaction tx = new Transaction();
        tx.setSourceAccountId(source);
        tx.setDestinationAccountId(dest);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setTransactionDate(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    private DepositResponse buildDepositResponse(Transaction transaction) {
        DepositResponse response = new DepositResponse();
        response.setId(transaction.getId());
        response.setAccountId(transaction.getDestinationAccountId());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setMessage("Dépôt effectué avec succès");
        return response;
    }

    private WithdrawResponse buildWithdrawResponse(Transaction transaction) {
        WithdrawResponse response = new WithdrawResponse();
        response.setId(transaction.getId());
        response.setAccountId(transaction.getSourceAccountId());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setMessage("Retrait effectué avec succès");
        return response;
    }

    private TransferResponse buildTransferResponse(Transaction transaction) {
        TransferResponse response = new TransferResponse();
        response.setId(transaction.getId());
        response.setSourceAccountId(transaction.getSourceAccountId());
        response.setDestinationAccountId(transaction.getDestinationAccountId());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setMessage("Virement effectué avec succès");
        return response;
    }

    // Masquage compte pour les logs
    private String mask(String accountId) {
        return accountId == null ? "" : "****" + accountId.substring(accountId.length() - 4);
    }
}