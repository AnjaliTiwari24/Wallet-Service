package com.dinoventures.backend.wallet.config;

import com.dinoventures.backend.model.User;
import com.dinoventures.backend.repository.UserRepository;
import com.dinoventures.backend.wallet.Wallet;
import com.dinoventures.backend.wallet.WalletRepository;
import com.dinoventures.backend.wallet.asset.Asset;
import com.dinoventures.backend.wallet.asset.AssetRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
@Slf4j
public class WalletDataLoader implements CommandLineRunner {

    private final AssetRepository assetRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing wallet system...");

        try {
            initializeAssets();
            initializeSystemWallets();
            initializeUserWallets();
            log.info("Wallet system initialized successfully");
        } catch (Exception e) {
            log.warn("Wallet initialization error: {}", e.getMessage());
        }
    }

    private void initializeAssets() {
        log.info("Initializing assets...");

        createAssetIfNotExists("GOLD_COINS", "Gold Coins", "Premium in-game currency", Asset.AssetType.GOLD_COINS);
        createAssetIfNotExists("LOYALTY_POINTS", "Loyalty Points", "User loyalty rewards", Asset.AssetType.LOYALTY_POINTS);
        createAssetIfNotExists("CREDIT_TOKENS", "Credit Tokens", "Service credit tokens", Asset.AssetType.CREDIT_TOKENS);

        log.info("Assets initialized");
    }

    private void createAssetIfNotExists(String code, String name, String description, Asset.AssetType type) {
        if (assetRepository.findByCode(code).isEmpty()) {
            Asset asset = Asset.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .type(type)
                    .active(true)
                    .build();
            assetRepository.save(asset);
            log.info("Asset created: {}", code);
        }
    }

    private void initializeSystemWallets() {
        log.info("Initializing system wallets...");

        assetRepository.findAll().forEach(asset -> {
            // Treasury wallet
            createSystemWalletIfNotExists("TREASURY", asset, new BigDecimal("1000000.00"));
            // Bonus pool
            createSystemWalletIfNotExists("BONUS_POOL", asset, new BigDecimal("500000.00"));
        });

        log.info("System wallets initialized");
    }

    private void createSystemWalletIfNotExists(String systemId, Asset asset, BigDecimal balance) {
        if (walletRepository.findBySystemWalletIdAndAsset(systemId, asset).isEmpty()) {
            Wallet wallet = Wallet.builder()
                    .user(null)
                    .asset(asset)
                    .balance(balance)
                    .systemWalletId(systemId)
                    .isSystemWallet(true)
                    .build();
            walletRepository.save(wallet);
            log.info("System wallet created: systemId={}, assetCode={}", systemId, asset.getCode());
        }
    }

    private void initializeUserWallets() {
        log.info("Initializing user wallets...");

        userRepository.findAll().forEach(user -> {
            assetRepository.findAll().forEach(asset -> {
                if (walletRepository.findByUserAndAsset(user, asset).isEmpty()) {
                    Wallet wallet = Wallet.builder()
                            .user(user)
                            .asset(asset)
                            .balance(getInitialBalance(asset.getCode()))
                            .isSystemWallet(false)
                            .build();
                    walletRepository.save(wallet);
                    log.info("User wallet created: userId={}, assetCode={}", user.getId(), asset.getCode());
                }
            });
        });

        log.info("User wallets initialized");
    }

    private BigDecimal getInitialBalance(String assetCode) {
        return switch (assetCode) {
            case "GOLD_COINS" -> new BigDecimal("500.00");
            case "LOYALTY_POINTS" -> new BigDecimal("1000.00");
            case "CREDIT_TOKENS" -> new BigDecimal("100.00");
            default -> BigDecimal.ZERO;
        };
    }
}
