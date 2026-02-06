package com.dinoventures.backend.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when a wallet operation fails due to insufficient balance.
 */
public class InsufficientBalanceException extends RuntimeException {
    private final BigDecimal availableBalance;
    private final BigDecimal requiredAmount;

    public InsufficientBalanceException(String message, BigDecimal availableBalance, BigDecimal requiredAmount) {
        super(message);
        this.availableBalance = availableBalance;
        this.requiredAmount = requiredAmount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getRequiredAmount() {
        return requiredAmount;
    }
}
