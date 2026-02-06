package com.dinoventures.backend.service;

import com.dinoventures.backend.dto.BudgetDTO;
import com.dinoventures.backend.exception.ResourceNotFoundException;
import com.dinoventures.backend.model.Budget;
import com.dinoventures.backend.model.User;
import com.dinoventures.backend.repository.BudgetRepository;
import com.dinoventures.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Transactional
    public BudgetDTO createBudget(Long userId, BudgetDTO dto) {
        log.info("Creating budget for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        budgetRepository.findByUserIdAndCategoryAndMonthYear(userId, dto.getCategory(), dto.getMonthYear())
                .ifPresent(b -> {
                    throw new IllegalArgumentException("Budget already exists for this category and month");
                });

        Budget budget = Budget.builder()
                .user(user)
                .category(dto.getCategory())
                .limitAmount(dto.getLimitAmount())
                .monthYear(dto.getMonthYear())
                .active(true)
                .build();

        Budget saved = budgetRepository.save(budget);
        log.info("Budget created successfully: {}", saved.getId());

        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<BudgetDTO> getUserBudgets(Long userId, Pageable pageable) {
        log.info("Fetching budgets for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return budgetRepository.findByUserId(userId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<BudgetDTO> getActiveBudgets(Long userId, Pageable pageable) {
        log.info("Fetching active budgets for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return budgetRepository.findByUserIdAndActive(userId, true, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public BudgetDTO getBudgetById(Long userId, Long budgetId) {
        log.info("Fetching budget: {} for user: {}", budgetId, userId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Budget not found for this user");
        }

        return mapToDTO(budget);
    }

    @Transactional
    public BudgetDTO updateBudget(Long userId, Long budgetId, BudgetDTO dto) {
        log.info("Updating budget: {} for user: {}", budgetId, userId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Budget not found for this user");
        }

        budget.setCategory(dto.getCategory());
        budget.setLimitAmount(dto.getLimitAmount());
        budget.setMonthYear(dto.getMonthYear());
        budget.setActive(dto.getActive() != null ? dto.getActive() : true);

        Budget updated = budgetRepository.save(budget);
        log.info("Budget updated successfully: {}", updated.getId());

        return mapToDTO(updated);
    }

    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        log.info("Deleting budget: {} for user: {}", budgetId, userId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Budget not found for this user");
        }

        budgetRepository.delete(budget);
        log.info("Budget deleted successfully: {}", budgetId);
    }

    private BudgetDTO mapToDTO(Budget budget) {
        return BudgetDTO.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .limitAmount(budget.getLimitAmount())
                .monthYear(budget.getMonthYear())
                .active(budget.getActive())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
