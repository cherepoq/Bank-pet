package ru.bankpet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDto(String title, BigDecimal amount, LocalDateTime createdAt, String sourceType) {
}
