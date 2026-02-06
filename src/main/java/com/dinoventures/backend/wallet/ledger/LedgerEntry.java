package com.dinoventures.backend.wallet.ledger;

import com.dinoventures.backend.wallet.Wallet;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_ledger_debit_wallet", columnList = "debit_wallet_id"),
    @Index(name = "idx_ledger_credit_wallet", columnList = "credit_wallet_id"),
    @Index(name = "idx_ledger_idempotency_key", columnList = "idempotency_key"),
    @Index(name = "idx_ledger_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Debit wallet is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "debit_wallet_id", nullable = false)
    private Wallet debitWallet;

    @NotNull(message = "Credit wallet is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credit_wallet_id", nullable = false)
    private Wallet creditWallet;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(length = 100, unique = true, nullable = false)
    private String idempotencyKey;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        TOP_UP,
        BONUS,
        SPEND,
        TRANSFER,
        REFUND
    }
}
