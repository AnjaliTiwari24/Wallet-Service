package com.dinoventures.backend.repository;

import com.dinoventures.backend.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Page<Budget> findByUserId(Long userId, Pageable pageable);

    Page<Budget> findByUserIdAndActive(Long userId, Boolean active, Pageable pageable);

    Optional<Budget> findByUserIdAndCategoryAndMonthYear(Long userId, String category, String monthYear);

    void deleteByUserIdAndId(Long userId, Long budgetId);
}
