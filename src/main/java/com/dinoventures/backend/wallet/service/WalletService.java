package com.dinoventures.backend.wallet.service;

import com.dinoventures.backend.exception.InsufficientBalanceException;
import com.dinoventures.backend.exception.ResourceNotFoundException;
import com.dinoventures.backend.model.User;
import com.dinoventures.backend.wallet.Wallet;
import com.dinoventures.backend.wallet.WalletRepository;
import com.dinoventures.backend.wallet.asset.Asset;
import com.dinoventures.backend.wallet.asset.AssetRepository;
import com.dinoventures.backend.wallet.ledger.LedgerEntry;
import com.dinoventures.backend.wallet.ledger.LedgerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final AssetRepository assetRepository;
    private final LedgerRepository ledgerRepository;

    /**
     * Top-up: Transfer credits from System Treasury to User Wallet
     * Uses idempotency key to prevent duplicate transactions
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public WalletTransactionResult topUp(User user, String assetCode, BigDecimal amount, String idempotencyKey, String description) {
        log.info("Top-up request: userId={}, assetCode={}, amount={}, idempotencyKey={}", user.getId(), assetCode, amount, idempotencyKey);

        // Check idempotency
        var existingLedger = ledgerRepository.findByIdempotencyKey(idempotencyKey);
        if (existingLedger.isPresent()) {
            log.info("Idempotent request detected, returning previous result");
            return mapLedgerEntryToResult(existingLedger.get());
        }

        // Validate asset
        Asset asset = assetRepository.findByCodeAndActiveTrue(assetCode)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetCode));

        // Get user wallet with lock
        Wallet userWallet = walletRepository.findUserWalletForUpdate(user, asset)
                .orElseThrow(() -> new ResourceNotFoundException("User wallet not found"));

        // Get system wallet with lock
        Wallet systemWallet = walletRepository.findSystemWalletForUpdate("TREASURY", asset)
                .orElseThrow(() -> new ResourceNotFoundException("System treasury not found"));

        // Validate system wallet has sufficient balance
        if (systemWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient system treasury balance",
                    systemWallet.getBalance(),
                    amount
            );
        }

        // Update balances
        systemWallet.setBalance(systemWallet.getBalance().subtract(amount));
        userWallet.setBalance(userWallet.getBalance().add(amount));

        walletRepository.save(systemWallet);
        walletRepository.save(userWallet);

        // Create ledger entry
        LedgerEntry ledger = LedgerEntry.builder()
                .debitWallet(systemWallet)
                .creditWallet(userWallet)
                .amount(amount)
                .transactionType(LedgerEntry.TransactionType.TOP_UP)
                .idempotencyKey(idempotencyKey)
                .description(description)
                .build();

        ledgerRepository.save(ledger);

        log.info("Top-up completed: transactionId={}, amount={}", ledger.getId(), amount);

        return mapLedgerEntryToResult(ledger);
    }

    /**
     * Bonus: System issues credits directly to user wallet
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public WalletTransactionResult bonus(User user, String assetCode, BigDecimal amount, String idempotencyKey, String description) {
        log.info("Bonus request: userId={}, assetCode={}, amount={}, idempotencyKey={}", user.getId(), assetCode, amount, idempotencyKey);

        // Check idempotency
        var existingLedger = ledgerRepository.findByIdempotencyKey(idempotencyKey);
        if (existingLedger.isPresent()) {
            log.info("Idempotent bonus request detected, returning previous result");
            return mapLedgerEntryToResult(existingLedger.get());
        }

        // Validate asset
        Asset asset = assetRepository.findByCodeAndActiveTrue(assetCode)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetCode));

        // Get user wallet
        Wallet userWallet = walletRepository.findUserWalletForUpdate(user, asset)
                .orElseThrow(() -> new ResourceNotFoundException("User wallet not found"));

        // Get system wallet
        Wallet systemWallet = walletRepository.findSystemWalletForUpdate("BONUS_POOL", asset)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus pool not found"));

        // Validate system wallet has sufficient balance
        if (systemWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient bonus pool balance",
                    systemWallet.getBalance(),
                    amount
            );
        }

        // Update balances
        systemWallet.setBalance(systemWallet.getBalance().subtract(amount));
        userWallet.setBalance(userWallet.getBalance().add(amount));

        walletRepository.save(systemWallet);
        walletRepository.save(userWallet);

        // Create ledger entry
        LedgerEntry ledger = LedgerEntry.builder()
                .debitWallet(systemWallet)
                .creditWallet(userWallet)
                .amount(amount)
                .transactionType(LedgerEntry.TransactionType.BONUS)
                .idempotencyKey(idempotencyKey)
                .description(description)
                .build();

        ledgerRepository.save(ledger);

        log.info("Bonus completed: transactionId={}, amount={}", ledger.getId(), amount);

        return mapLedgerEntryToResult(ledger);
    }

    /**
     * Spend: Debit from user wallet to system wallet
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public WalletTransactionResult spend(User user, String assetCode, BigDecimal amount, String idempotencyKey, String description) {
        log.info("Spend request: userId={}, assetCode={}, amount={}, idempotencyKey={}", user.getId(), assetCode, amount, idempotencyKey);

        // Check idempotency
        var existingLedger = ledgerRepository.findByIdempotencyKey(idempotencyKey);
        if (existingLedger.isPresent()) {
            log.info("Idempotent spend request detected, returning previous result");
            return mapLedgerEntryToResult(existingLedger.get());
        }

        // Validate asset
        Asset asset = assetRepository.findByCodeAndActiveTrue(assetCode)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetCode));

        // Get user wallet
        Wallet userWallet = walletRepository.findUserWalletForUpdate(user, asset)
                .orElseThrow(() -> new ResourceNotFoundException("User wallet not found"));

        // Validate sufficient balance
        if (userWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance",
                    userWallet.getBalance(),
                    amount
            );
        }

        // Get system wallet
        Wallet systemWallet = walletRepository.findSystemWalletForUpdate("TREASURY", asset)
                .orElseThrow(() -> new ResourceNotFoundException("System treasury not found"));

        // Update balances
        userWallet.setBalance(userWallet.getBalance().subtract(amount));
        systemWallet.setBalance(systemWallet.getBalance().add(amount));

        walletRepository.save(userWallet);
        walletRepository.save(systemWallet);

        // Create ledger entry
        LedgerEntry ledger = LedgerEntry.builder()
                .debitWallet(userWallet)
                .creditWallet(systemWallet)
                .amount(amount)
                .transactionType(LedgerEntry.TransactionType.SPEND)
                .idempotencyKey(idempotencyKey)
                .description(description)
                .build();

        ledgerRepository.save(ledger);

        log.info("Spend completed: transactionId={}, amount={}", ledger.getId(), amount);

        return mapLedgerEntryToResult(ledger);
    }

    /**
     * Get current wallet balance
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(User user, String assetCode) {
        log.info("Get balance request: userId={}, assetCode={}", user.getId(), assetCode);

        Asset asset = assetRepository.findByCodeAndActiveTrue(assetCode)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetCode));

        Wallet wallet = walletRepository.findByUserAndAsset(user, asset)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        log.info("Wallet found: walletId={}, userId={}, assetCode={}, balance={}", 
                wallet.getId(), wallet.getUser().getId(), asset.getCode(), wallet.getBalance());

        return wallet.getBalance();
    }

    /**
     * Initialize wallet for a new user
     */
    @Transactional
    public void initializeUserWallets(User user) {
        log.info("Initializing wallets for user: {}", user.getId());

        assetRepository.findAll().stream()
                .filter(Asset::getActive)
                .forEach(asset -> {
                    Wallet wallet = Wallet.builder()
                            .user(user)
                            .asset(asset)
                            .balance(BigDecimal.ZERO)
                            .isSystemWallet(false)
                            .build();
                    walletRepository.save(wallet);
                    log.info("Wallet created: userId={}, assetCode={}", user.getId(), asset.getCode());
                });
    }

    private WalletTransactionResult mapLedgerEntryToResult(LedgerEntry ledger) {
        return WalletTransactionResult.builder()
                .transactionId(ledger.getId())
                .idempotencyKey(ledger.getIdempotencyKey())
                .amount(ledger.getAmount())
                .transactionType(ledger.getTransactionType().toString())
                .creditWalletId(ledger.getCreditWallet().getId())
                .debitWalletId(ledger.getDebitWallet().getId())
                .newCreditBalance(ledger.getCreditWallet().getBalance())
                .newDebitBalance(ledger.getDebitWallet().getBalance())
                .createdAt(ledger.getCreatedAt())
                .build();
    }
}
