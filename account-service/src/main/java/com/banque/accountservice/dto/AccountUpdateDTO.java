package com.banque.accountservice.dto;

import lombok.Data;

@Data
public class AccountUpdateDTO {
    private Long id;
    private String accountType; // Allow changing type if needed
    private Boolean active;
    private java.math.BigDecimal balance;
    // Add other updatable fields as needed, but DO NOT include accountNumber
}