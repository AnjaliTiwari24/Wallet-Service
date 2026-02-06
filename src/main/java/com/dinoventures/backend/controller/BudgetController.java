package com.dinoventures.backend.controller;

import com.dinoventures.backend.dto.ApiResponse;
import com.dinoventures.backend.dto.BudgetDTO;
import com.dinoventures.backend.service.BudgetService;
import com.dinoventures.backend.util.AuthenticationUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budgets")
@AllArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BudgetDTO>> createBudget(
            @Valid @RequestBody BudgetDTO dto) {
        log.info("Create budget request received");

        Long userId = authenticationUtil.getCurrentUserId();
        BudgetDTO response = budgetService.createBudget(userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Budget created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<BudgetDTO>>> getUserBudgets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get user budgets request received");

        Long userId = authenticationUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetDTO> response = budgetService.getUserBudgets(userId, pageable);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Budgets retrieved successfully", response));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<BudgetDTO>>> getActiveBudgets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get active budgets request received");

        Long userId = authenticationUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetDTO> response = budgetService.getActiveBudgets(userId, pageable);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Active budgets retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BudgetDTO>> getBudget(@PathVariable Long id) {
        log.info("Get budget request received for id: {}", id);

        Long userId = authenticationUtil.getCurrentUserId();
        BudgetDTO response = budgetService.getBudgetById(userId, id);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Budget retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BudgetDTO>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDTO dto) {
        log.info("Update budget request received for id: {}", id);

        Long userId = authenticationUtil.getCurrentUserId();
        BudgetDTO response = budgetService.updateBudget(userId, id, dto);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Budget updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Long id) {
        log.info("Delete budget request received for id: {}", id);

        Long userId = authenticationUtil.getCurrentUserId();
        budgetService.deleteBudget(userId, id);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Budget deleted successfully"));
    }
}
