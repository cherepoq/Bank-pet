package ru.bankpet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record PaymentRequestDto(
        @NotBlank String title,
        @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String category,
        Boolean confirmedByUser
) {
}
