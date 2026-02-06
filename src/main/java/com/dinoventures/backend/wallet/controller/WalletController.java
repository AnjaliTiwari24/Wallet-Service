package com.dinoventures.backend.wallet.controller;

import com.dinoventures.backend.dto.ApiResponse;
import com.dinoventures.backend.model.User;
import com.dinoventures.backend.util.AuthenticationUtil;
import com.dinoventures.backend.wallet.dto.BalanceResponse;
import com.dinoventures.backend.wallet.dto.BonusRequest;
import com.dinoventures.backend.wallet.dto.SpendRequest;
import com.dinoventures.backend.wallet.dto.TopUpRequest;
import com.dinoventures.backend.wallet.service.WalletService;
import com.dinoventures.backend.wallet.service.WalletTransactionResult;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
@AllArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping("/top-up")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WalletTransactionResult>> topUp(
            @Valid @RequestBody TopUpRequest request) {
        log.info("Top-up request received: assetCode={}, amount={}", request.getAssetCode(), request.getAmount());

        User user = authenticationUtil.getCurrentUser();
        WalletTransactionResult result = walletService.topUp(
                user,
                request.getAssetCode(),
                request.getAmount(),
                request.getIdempotencyKey(),
                request.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Top-up successful", result));
    }

    @PostMapping("/bonus")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WalletTransactionResult>> bonus(
            @Valid @RequestBody BonusRequest request) {
        log.info("Bonus request received: assetCode={}, amount={}", request.getAssetCode(), request.getAmount());

        User user = authenticationUtil.getCurrentUser();
        WalletTransactionResult result = walletService.bonus(
                user,
                request.getAssetCode(),
                request.getAmount(),
                request.getIdempotencyKey(),
                request.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Bonus credited successfully", result));
    }

    @PostMapping("/spend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WalletTransactionResult>> spend(
            @Valid @RequestBody SpendRequest request) {
        log.info("Spend request received: assetCode={}, amount={}", request.getAssetCode(), request.getAmount());

        User user = authenticationUtil.getCurrentUser();
        WalletTransactionResult result = walletService.spend(
                user,
                request.getAssetCode(),
                request.getAmount(),
                request.getIdempotencyKey(),
                request.getDescription()
        );

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Spend successful", result));
    }

    @GetMapping("/balance/{assetCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @PathVariable String assetCode) {
        log.info("Get balance request: assetCode={}", assetCode);

        User user = authenticationUtil.getCurrentUser();
        var balance = walletService.getBalance(user, assetCode);

        BalanceResponse response = BalanceResponse.builder()
                .assetCode(assetCode)
                .balance(balance)
                .build();

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Balance retrieved successfully", response));
    }
}
