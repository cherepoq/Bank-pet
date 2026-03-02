package ru.bankpet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record SpendingFilterSettingsDto(
        boolean llmAgentEnabled,
        boolean hardBlockEnabled,
        @DecimalMin("1.00") BigDecimal confirmationThreshold,
        @NotBlank String blockedCategoriesCsv,
        @NotBlank String riskyCategoriesCsv
) {
}
