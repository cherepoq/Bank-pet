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
            return new GuardianDecision("REJECTED", "HARD", buildMessage(preferences.agentProfile(), "HARD", true));
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
            return new GuardianDecision("NEEDS_CONFIRMATION", "HARD", buildMessage(preferences.agentProfile(), "HARD", false) + llmComment);
        }
        if (totalRisk >= 65 && !Boolean.TRUE.equals(confirmedByUser)) {
            return new GuardianDecision("NEEDS_CONFIRMATION", "MEDIUM", buildMessage(preferences.agentProfile(), "MEDIUM", false) + llmComment);
        }
        if (totalRisk >= 45 && !Boolean.TRUE.equals(confirmedByUser)) {
            return new GuardianDecision("NEEDS_CONFIRMATION", "SOFT", buildMessage(preferences.agentProfile(), "SOFT", false) + llmComment);
        }

        return new GuardianDecision("APPROVED", "SOFT", "✅ Платёж одобрен.");
    }

    private String buildMessage(String profile, String severity, boolean blocked) {
        return switch (profile == null ? "BALANCED" : profile.toUpperCase()) {
            case "STRICT" -> switch (severity) {
                case "HARD" -> blocked ? "🚫 Строгий режим: категория запрещена. Платеж остановлен." : "⛔ Строгий режим: не рекомендую, риск слишком высокий.";
                case "MEDIUM" -> "⚠ Строгий режим: трата сомнительная, подтвердите только при острой необходимости.";
                default -> "🙂 Строгий режим: лучше перепроверьте покупку перед оплатой.";
            };
            case "FRIENDLY" -> switch (severity) {
                case "HARD" -> blocked ? "🙅‍♂️ Дружеский стоп: эту покупку лучше не делать." : "😬 Похоже на ненужную трату, давайте лучше отменим.";
                case "MEDIUM" -> "🤔 Может отложим? Похоже на импульсивную покупку.";
                default -> "🙂 Небольшое напоминание: проверьте, точно ли нужно сейчас.";
            };
            default -> switch (severity) {
                case "HARD" -> blocked ? "🚫 Категория заблокирована вашими настройками." : "⛔ Высокий риск. Рекомендую не платить.";
                case "MEDIUM" -> "⚠ Обнаружен риск импульсивной траты. Подтвердите решение.";
                default -> "🙂 Проверьте покупку перед оплатой.";
            };
        };
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
                                      String riskyCategoriesCsv,
                                      String agentProfile) {}

    public record GuardianDecision(String status, String severity, String message) {}
}
