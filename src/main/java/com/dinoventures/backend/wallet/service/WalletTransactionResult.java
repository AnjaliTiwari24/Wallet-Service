package com.dinoventures.backend.wallet.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionResult {
    private Long transactionId;
    private String idempotencyKey;
    private BigDecimal amount;
    private String transactionType;
    private Long creditWalletId;
    private Long debitWalletId;
    private BigDecimal newCreditBalance;
    private BigDecimal newDebitBalance;
    private LocalDateTime createdAt;
}
