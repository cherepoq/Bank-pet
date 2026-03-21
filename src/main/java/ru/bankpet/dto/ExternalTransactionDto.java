package ru.bankpet.dto;

import java.math.BigDecimal;

public record ExternalTransactionDto(String title, BigDecimal amount, String category, String sourceBank) {
}
