package com.dinoventures.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class BudgetDTO {

    private Long id;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Limit is required")
    @DecimalMin(value = "0.01", message = "Limit must be greater than 0")
    private BigDecimal limitAmount;

    @NotBlank(message = "Month-year is required (format: YYYY-MM)")
    private String monthYear;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
