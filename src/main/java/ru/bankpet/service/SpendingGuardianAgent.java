package ru.bankpet.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SpendingGuardianAgent {

    private final LlmSpendingAdvisor llmSpendingAdvisor;

    public SpendingGuardianAgent(LlmSpendingAdvisor llmSpendingAdvisor) {
        this.llmSpendingAdvisor = llmSpendingAdvisor;
    }

    public GuardianDecision evaluate(String title, String category, BigDecimal amount, Boolean confirmedByUser,
                                     GuardianPreferences preferences) {
        String normalizedCategory = category.trim().toUpperCase();
        Set<String> blocked = csvToSet(preferences.blockedCategoriesCsv());
        Set<String> risky = csvToSet(preferences.riskyCategoriesCsv());

        if (preferences.hardBlockEnabled() && blocked.contains(normalizedCategory)) {
            return new GuardianDecision("REJECTED", "Финансовый ИИ-агент: траты на категорию '" + category + "' заблокированы.");
        }

        boolean riskyByCategory = risky.contains(normalizedCategory);
        boolean riskyByAmount = amount.compareTo(preferences.confirmationThreshold()) > 0;
        boolean riskyByLlm = false;
        String llmComment = "";

        if (preferences.llmAgentEnabled()) {
            LlmSpendingAdvisor.Advice advice = llmSpendingAdvisor.analyze(title, category, amount);
            riskyByLlm = advice.riskScore() >= 65;
            llmComment = " " + advice.explanation() + " Риск: " + advice.riskScore() + "/100.";
        }

        if ((riskyByCategory || riskyByAmount || riskyByLlm) && !Boolean.TRUE.equals(confirmedByUser)) {
            return new GuardianDecision("NEEDS_CONFIRMATION",
                    "ИИ-агент: похоже на рискованную трату. Подтвердите оплату." + llmComment);
        }

        return new GuardianDecision("APPROVED", "Платёж одобрен.");
    }

    private Set<String> csvToSet(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    public record GuardianPreferences(boolean llmAgentEnabled,
                                      boolean hardBlockEnabled,
                                      BigDecimal confirmationThreshold,
                                      String blockedCategoriesCsv,
                                      String riskyCategoriesCsv) {}

    public record GuardianDecision(String status, String message) {}
}
