package ru.bankpet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record NfcPurchaseRequestDto(
        @NotBlank String merchant,
        @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String category,
        String deviceToken,
        Boolean confirmedByUser
) {
}
