package ru.bankpet.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class SpendingGuardianAgent {

    private final List<String> blockedCategories;
    private final List<String> riskyCategories;

    public SpendingGuardianAgent(
            @Value("${app.guardian.blocked-categories}") List<String> blockedCategories,
            @Value("${app.guardian.risky-categories}") List<String> riskyCategories
    ) {
        this.blockedCategories = blockedCategories;
        this.riskyCategories = riskyCategories;
    }

    public GuardianDecision evaluate(String category, BigDecimal amount, Boolean confirmedByUser) {
        String normalizedCategory = category.trim().toUpperCase();
        if (blockedCategories.contains(normalizedCategory)) {
            return new GuardianDecision("REJECTED", "Финансовый ИИ-агент: траты на категорию '" + category + "' заблокированы.");
        }

        boolean risky = riskyCategories.contains(normalizedCategory) || amount.compareTo(new BigDecimal("50000")) > 0;
        if (risky && !Boolean.TRUE.equals(confirmedByUser)) {
            return new GuardianDecision("NEEDS_CONFIRMATION",
                    "ИИ-агент: похоже на импульсивную/рискованную трату. Подтвердите оплату.");
        }

        return new GuardianDecision("APPROVED", "Платёж одобрен.");
    }

    public record GuardianDecision(String status, String message) {
    }
}
