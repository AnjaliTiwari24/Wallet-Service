package com.dinoventures.backend.wallet.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

    Optional<LedgerEntry> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT le FROM LedgerEntry le WHERE le.debitWallet.id = :walletId OR le.creditWallet.id = :walletId ORDER BY le.createdAt DESC")
    List<LedgerEntry> findWalletTransactions(@Param("walletId") Long walletId);

    @Query("SELECT le FROM LedgerEntry le WHERE (le.debitWallet.id = :walletId OR le.creditWallet.id = :walletId) AND le.transactionType = :type ORDER BY le.createdAt DESC")
    List<LedgerEntry> findWalletTransactionsByType(@Param("walletId") Long walletId, @Param("type") LedgerEntry.TransactionType type);
}
