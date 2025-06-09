package com.banque.accountservice.service;

import com.banque.accountservice.dto.*;
import com.banque.accountservice.dto.AccountResponseDTO;
import com.banque.accountservice.model.AccountType;


import java.math.BigDecimal;
import java.util.List;

public interface AccountService {


    // account-service/src/main/java/com/banque/accountservice/service/AccountService.java

    String getClientEmailByAccountNumber(String accountNumber);
    /**
     * Update account information
     *
     * @param accountUpdateDTO The updated account information
     * @return Updated account details
     */
    AccountDTO updateAccountInfo(AccountUpdateDTO accountUpdateDTO);

    /**
     * Create a new account for a client
     *
     * @param accountCreationDTO The details for creating the account
     * @return Response with the created account information
     */
    AccountResponseDTO createAccount(AccountCreationDTO accountCreationDTO);

    /**
     * Get account details by account ID
     *
     * @param accountId The account ID
     * @return The account details
     */
    AccountDTO getAccountById(Long accountId);

    /**
     * Get account details by account number
     *
     * @param accountNumber The account number
     * @return The account details
     */
    AccountDTO getAccountByAccountNumber(String accountNumber);

    /**
     * Get all accounts for a client
     *
     * @param clientId The client ID
     * @return List of client's accounts
     */
    List<AccountDTO> getAccountsByClientId(Long clientId);

    /**
     * Retrieves accounts by account type
     *
     * @param accountType Account type
     * @return List of accounts
     */
    List<AccountResponseDTO> getAccountsByType(AccountType accountType);

    /**
     * Get all active accounts for a client
     *
     * @param clientId The client ID
     * @return List of client's active accounts
     */
    List<AccountDTO> getActiveAccountsByClientId(Long clientId);

    /**
     * Update the balance of an account
     *
     * @param accountNumber The account number
     * @param amount The amount to update (positive for credit, negative for debit)
     * @return Updated account details
     */
    AccountDTO updateBalance(String accountNumber, BigDecimal amount);


    /**
     * Updates account active status
     *
     * @param accountId Account ID
     * @param active New active status
     * @return Updated account details
     */
    AccountResponseDTO updateAccountStatus(Long accountId, boolean active);


    /**
     * Close an account
     *
     * @param accountId The account ID
     * @return Response with the result of the operation
     */
    AccountResponseDTO closeAccount(Long accountId);

    /**
     * Check if an account exists by account number
     *
     * @param accountNumber The account number
     * @return true if the account exists, false otherwise
     */
    boolean accountExists(String accountNumber);

    /**
     * Check if an account has sufficient balance for a withdrawal
     *
     * @param accountNumber The account number
     * @param amount The amount to check
     * @return true if balance is sufficient, false otherwise
     */
    boolean hasSufficientBalance(String accountNumber, BigDecimal amount);
}