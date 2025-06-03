package com.securitybanking.transaction.service;

import com.securitybanking.transaction.dto.TransactionRequest;
import com.securitybanking.transaction.entity.Transaction;
import com.securitybanking.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(TransactionRequest request) {
        Transaction tx = new Transaction();
        tx.setAmount(request.getAmount());
        tx.setSourceAccountId(request.getSourceAccountId());
        tx.setDestinationAccountId(request.getDestinationAccountId());
        tx.setTransactionDate(LocalDateTime.now());

        return transactionRepository.save(tx);
    }
}
