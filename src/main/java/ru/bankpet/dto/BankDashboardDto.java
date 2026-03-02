package ru.bankpet.dto;

import java.math.BigDecimal;
import java.util.List;

public record BankDashboardDto(
        String clientName,
        String phone,
        BigDecimal accountBalance,
        BigDecimal digitalRubleBalance,
        boolean digitalRubleLinked,
        int avoidedImpulseCount,
        List<String> cards,
        List<TransactionDto> recentTransactions
) {
}
