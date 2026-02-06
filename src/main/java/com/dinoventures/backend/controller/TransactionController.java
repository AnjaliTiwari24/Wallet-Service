package com.dinoventures.backend.controller;

import com.dinoventures.backend.dto.ApiResponse;
import com.dinoventures.backend.dto.TransactionDTO;
import com.dinoventures.backend.service.TransactionService;
import com.dinoventures.backend.util.AuthenticationUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@AllArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(
            @Valid @RequestBody TransactionDTO dto) {
        log.info("Create transaction request received");

        Long userId = authenticationUtil.getCurrentUserId();
        TransactionDTO response = transactionService.createTransaction(userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Transaction created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getUserTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Get user transactions request received");

        Long userId = authenticationUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TransactionDTO> response = transactionService.getUserTransactions(userId, pageable);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Transactions retrieved successfully", response));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getUserTransactionsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get transactions by type: {}", type);

        Long userId = authenticationUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<TransactionDTO> response = transactionService.getUserTransactionsByType(userId, type, pageable);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Transactions retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransaction(@PathVariable Long id) {
        log.info("Get transaction request received for id: {}", id);

        Long userId = authenticationUtil.getCurrentUserId();
        TransactionDTO response = transactionService.getTransactionById(userId, id);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Transaction retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO dto) {
        log.info("Update transaction request received for id: {}", id);

        Long userId = authenticationUtil.getCurrentUserId();
        TransactionDTO response = transactionService.updateTransaction(userId, id, dto);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Transaction updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        log.info("Delete transaction request received for id: {}", id);

        Long userId = authenticationUtil.getCurrentUserId();
        transactionService.deleteTransaction(userId, id);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Transaction deleted successfully"));
    }
}
