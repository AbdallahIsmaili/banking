package com.banque.accountservice.config;

import java.math.BigDecimal;

public class BusinessRuleConstants {
    // Limites de transaction
    public static final BigDecimal MAX_DAILY_WITHDRAWAL = new BigDecimal("2000.00");
    public static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("10000.00");

    // Soldes minimums par type de compte
    public static final BigDecimal MIN_BALANCE_SAVINGS = new BigDecimal("100.00");
    public static final BigDecimal MIN_BALANCE_CHECKING = new BigDecimal("0.00");

    // Autres règles
    public static final boolean ALLOW_OVERDRAFT = false;
    public static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("-1000.00");

    // Fees
    public static final BigDecimal ACCOUNT_MAINTENANCE_FEE = BigDecimal.valueOf(5);

    // Interest rates (percentage)
    public static final BigDecimal SAVINGS_INTEREST_RATE = BigDecimal.valueOf(2.5);
    public static final BigDecimal FIXED_DEPOSIT_INTEREST_RATE = BigDecimal.valueOf(5.0);
}