package com.banque.accountservice.repository;

import com.banque.accountservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByClientId(Long clientId);

    Optional<Account> findByAccountNumber(String accountNumber);

    // Avec la convention Spring Data JPA
    List<Account> findByClientIdAndActiveTrue(Long clientId);

    @Query("SELECT a FROM Account a WHERE a.client.id = :clientId AND a.active = true")
    List<Account> findActiveAccountsByClientId(@Param("clientId") Long clientId);

    boolean existsByAccountNumber(String accountNumber);
}