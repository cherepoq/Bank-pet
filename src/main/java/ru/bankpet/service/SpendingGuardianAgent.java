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
            return new GuardianDecision("REJECTED", "HARD", "🚫 Жёсткий стоп: эту категорию мы блокируем без вариантов.");
        }

        boolean riskyByCategory = risky.contains(normalizedCategory);
        boolean riskyByAmount = amount.compareTo(preferences.confirmationThreshold()) > 0;
        int llmRisk = 0;
        String llmComment = "";

        if (preferences.llmAgentEnabled()) {
            LlmSpendingAdvisor.Advice advice = llmSpendingAdvisor.analyze(title, category, amount);
            llmRisk = advice.riskScore();
            llmComment = " " + advice.explanation() + " Риск: " + advice.riskScore() + "/100.";
        }

        int totalRisk = Math.max(llmRisk, riskyByCategory ? 70 : 0);
        if (riskyByAmount) totalRisk = Math.max(totalRisk, 75);

        if (totalRisk >= 85 && !Boolean.TRUE.equals(confirmedByUser)) {
            return new GuardianDecision("NEEDS_CONFIRMATION", "HARD",
                    "⛔ Это очень сомнительная трата. Я настоятельно не рекомендую оплачивать." + llmComment);
        }
        if (totalRisk >= 65 && !Boolean.TRUE.equals(confirmedByUser)) {
            return new GuardianDecision("NEEDS_CONFIRMATION", "MEDIUM",
                    "⚠ Похоже на импульсивную покупку. Подтвердите, если точно нужно." + llmComment);
        }
        if (totalRisk >= 45 && !Boolean.TRUE.equals(confirmedByUser)) {
            return new GuardianDecision("NEEDS_CONFIRMATION", "SOFT",
                    "🙂 Небольшое предупреждение: проверьте, точно ли хотите эту покупку." + llmComment);
        }

        return new GuardianDecision("APPROVED", "SOFT", "✅ Платёж одобрен.");
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

    public record GuardianDecision(String status, String severity, String message) {}
}
