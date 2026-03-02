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
        String p = profile == null ? "BALANCED" : profile.toUpperCase();
        return switch (p) {
            case "STRICT" -> strictMsg(severity, blocked);
            case "FRIENDLY" -> friendlyMsg(severity, blocked);
            case "RUDE" -> rudeMsg(severity, blocked);
            case "BULLY" -> bullyMsg(severity, blocked);
            case "PICKME" -> pickmeMsg(severity, blocked);
            case "WIFE" -> wifeMsg(severity, blocked);
            case "MOTHER" -> motherMsg(severity, blocked);
            case "BOSS" -> bossMsg(severity, blocked);
            default -> balancedMsg(severity, blocked);
        };
    }

    private String balancedMsg(String severity, boolean blocked) {
        return switch (severity) {
            case "HARD" -> blocked ? "🚫 Категория заблокирована вашими настройками." : "⛔ Высокий риск. Рекомендую не платить.";
            case "MEDIUM" -> "⚠ Обнаружен риск импульсивной траты. Подтвердите решение.";
            default -> "🙂 Проверьте покупку перед оплатой.";
        };
    }

    private String strictMsg(String severity, boolean blocked) {
        return switch (severity) {
            case "HARD" -> blocked ? "🚫 Строгий режим: категория запрещена. Платеж остановлен." : "⛔ Строгий режим: не рекомендую, риск слишком высокий.";
            case "MEDIUM" -> "⚠ Строгий режим: трата сомнительная, подтвердите только при необходимости.";
            default -> "🙂 Строгий режим: лучше перепроверьте покупку перед оплатой.";
        };
    }

    private String friendlyMsg(String severity, boolean blocked) {
        return switch (severity) {
            case "HARD" -> blocked ? "🙅‍♂️ Дружеский стоп: эту покупку лучше не делать." : "😬 Похоже на ненужную трату, давайте лучше отменим.";
            case "MEDIUM" -> "🤔 Может отложим? Похоже на импульсивную покупку.";
            default -> "🙂 Небольшое напоминание: проверьте, точно ли нужно сейчас.";
        };
    }

    private String rudeMsg(String severity, boolean blocked) {
        return switch (severity) {
            case "HARD" -> blocked ? "🚫 Даже не начинай: это под блоком." : "😤 Серьёзно? Это плохая идея, отменяй.";
            case "MEDIUM" -> "😒 Импульсивно. Не делай вид, что это необходимость.";
            default -> "🙄 Подумай ещё раз, оно того не стоит.";
        };
    }

    private String bullyMsg(String severity, boolean blocked) {
        return switch (severity) {
            case "HARD" -> blocked ? "🛑 Стоп-игра: тут блок, без шансов." : "😈 Ты же сам знаешь, что это лишнее. Отменяй.";
            case "MEDIUM" -> "😏 Похоже на спонтанный слив денег. Точно уверен?";
            default -> "😉 Можешь лучше распорядиться этими деньгами.";
        };
    }

    private String pickmeMsg(String severity, boolean blocked) {
        return switch (severity) {
            case "HARD" -> blocked ? "💅 Это табу, солнышко. Блокируем." : "🙃 Я бы на твоем месте не платила, честно.";
            case "MEDIUM" -> "✨ Давай будем рациональными и пропустим эту покупку?";
            default -> "🌸 Может лучше сохранить деньги на что-то важнее?";
        };
    }

    private String wifeMsg(String severity, boolean blocked) {
        return switch (severity) {
            case "HARD" -> blocked ? "🏠 Нет, это мы не покупаем. Категория под запретом." : "😑 Дорогой, это лишнее. Отменяем.";
            case "MEDIUM" -> "😌 Давай без импульсивных трат, хорошо?";
            default -> "🙂 Подумай, нам это точно нужно сейчас?";
        };
    }

    private String motherMsg(String severity, boolean blocked) {
        return switch (severity) {
            case "HARD" -> blocked ? "👩‍🍼 Нельзя, я же говорила — эта категория под запретом." : "😠 Не трать зря, это рискованно.";
            case "MEDIUM" -> "🤨 Сначала подумай головой, потом плати.";
            default -> "🙂 Давай аккуратнее с деньгами, ладно?";
        };
    }

    private String bossMsg(String severity, boolean blocked) {
        return switch (severity) {
            case "HARD" -> blocked ? "📉 Отклонено: политика расходов запрещает эту операцию." : "⛔ Отклоняю: риск не соответствует вашим финансовым целям.";
            case "MEDIUM" -> "⚠ Требую обоснование: трата похожа на импульсивную.";
            default -> "🧾 Рекомендую перепроверить целесообразность платежа.";
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
