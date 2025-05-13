package com.banque.accountservice.service;

import com.banque.accountservice.client.NotificationClient;
import com.banque.accountservice.dto.AccountCreationDTO;
import com.banque.accountservice.dto.AccountDTO;
import com.banque.accountservice.dto.AccountResponseDTO;
import com.banque.accountservice.exception.AccountNotFoundException;
import com.banque.accountservice.exception.InsufficientBalanceException;
import com.banque.accountservice.model.Account;
import com.banque.accountservice.model.Client;
import com.banque.accountservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final NotificationClient notificationClient;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, NotificationClient notificationClient) {
        this.accountRepository = accountRepository;
        this.notificationClient = notificationClient;
    }

    @Override
    @Transactional
    public AccountResponseDTO createAccount(AccountCreationDTO accountCreationDTO) {
        // Validate inputs
        if (accountCreationDTO == null || accountCreationDTO.getInitialDeposit() == null || accountCreationDTO.getClientId() == null) {
            LOGGER.error("Invalid account creation request: {}", accountCreationDTO);
            return AccountResponseDTO.failure("Invalid account creation request");
        }

        try {
            // Generate a unique account number
            String accountNumber = generateUniqueAccountNumber();

            // Create a new account entity
            Account account = new Account();
            Client client = new Client();
            client.setId(accountCreationDTO.getClientId()); // Assign client ID
            account.setClient(client);

            account.setAccountNumber(accountNumber);
            account.setAccountType(accountCreationDTO.getAccountType());
            account.setBalance(accountCreationDTO.getInitialDeposit());
            account.setActive(true);
            account.setCreatedAt(LocalDateTime.now());

            // Save the account
            Account savedAccount = accountRepository.save(account);

            // Send notification about account creation
            sendNotification(() -> notificationClient.sendAccountCreationNotification(savedAccount.getClient().getId(), accountNumber),
                    "Failed to send account creation notification");

            return AccountResponseDTO.success("Account created successfully", accountNumber);

        } catch (Exception ex) {
            LOGGER.error("Error while creating account: {}", ex.getMessage());
            return AccountResponseDTO.failure("Account creation failed");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public AccountDTO getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return convertToDTO(account);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public AccountDTO getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber, "account number"));

        return convertToDTO(account);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<AccountDTO> getAccountsByClientId(Long clientId) {
        List<Account> accounts = accountRepository.findByClientId(clientId);

        return accounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<AccountDTO> getActiveAccountsByClientId(Long clientId) {
        List<Account> activeAccounts = accountRepository.findActiveAccountsByClientId(clientId);

        return activeAccounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountDTO updateBalance(String accountNumber, BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber, "account number"));

        // Check for sufficient balance if trying to withdraw
        if (amount.compareTo(BigDecimal.ZERO) < 0 &&
                account.getBalance().add(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException(accountNumber, account.getBalance(), amount.abs());
        }

        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(LocalDateTime.now());

        Account updatedAccount = accountRepository.save(account);

        // Send notification about balance update
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            sendNotification(() -> notificationClient.sendDepositNotification(account.getClient().getId(), accountNumber, amount),
                    "Failed to send deposit notification");
        } else {
            sendNotification(() -> notificationClient.sendWithdrawalNotification(account.getClient().getId(), accountNumber, amount.abs()),
                    "Failed to send withdrawal notification");
        }

        return convertToDTO(updatedAccount);
    }

    @Override
    @Transactional
    public AccountResponseDTO closeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (!account.isActive()) {
            return AccountResponseDTO.failure("Account is already closed");
        }

        account.setActive(false);
        account.setClosedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        sendNotification(() -> notificationClient.sendAccountClosureNotification(account.getClient().getId(), account.getAccountNumber()),
                "Failed to send account closure notification");

        return AccountResponseDTO.success("Account closed successfully", account.getAccountNumber());
    }

    @Override
    public boolean accountExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    @Override
    public boolean hasSufficientBalance(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber, "account number"));

        return account.getBalance().compareTo(amount) >= 0;
    }

    private AccountDTO convertToDTO(Account account) {
        AccountDTO accountDTO = new AccountDTO();
        BeanUtils.copyProperties(account, accountDTO);
        return accountDTO;
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;

        do {
            accountNumber = String.format("%010d", random.nextInt(1_000_000_000));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private void sendNotification(Runnable notificationTask, String errorMessage) {
        try {
            notificationTask.run();
        } catch (Exception e) {
            LOGGER.error(errorMessage + ": {}", e.getMessage());
        }
    }
}