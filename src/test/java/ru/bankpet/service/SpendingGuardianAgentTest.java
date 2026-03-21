package ru.bankpet.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpendingGuardianAgentTest {

    @Test
    void shouldRejectBlockedCategoryWhenHardBlockEnabled() {
        SpendingGuardianAgent agent = new SpendingGuardianAgent(new FixedAdvisor(10, "stub"));

        SpendingGuardianAgent.GuardianDecision decision = agent.evaluate(
                "Ставка",
                "betting",
                new BigDecimal("1000"),
                false,
                new SpendingGuardianAgent.GuardianPreferences(
                        true,
                        true,
                        new BigDecimal("50000"),
                        "BETTING,CASINO",
                        "OZON",
                        "BALANCED"
                )
        );

        assertEquals("REJECTED", decision.status());
        assertEquals("HARD", decision.severity());
    }

    @Test
    void shouldNeedHardConfirmationForVeryHighLlmRisk() {
        SpendingGuardianAgent agent = new SpendingGuardianAgent(new FixedAdvisor(90, "high risk"));

        SpendingGuardianAgent.GuardianDecision decision = agent.evaluate(
                "iPhone premium",
                "electronics",
                new BigDecimal("15000"),
                false,
                defaultPrefs("FRIENDLY")
        );

        assertEquals("NEEDS_CONFIRMATION", decision.status());
        assertEquals("HARD", decision.severity());
        assertTrue(decision.message().contains("Риск: 90/100"));
    }

    @Test
    void shouldApproveAfterUserConfirmationEvenForRiskyCategory() {
        SpendingGuardianAgent agent = new SpendingGuardianAgent(new FixedAdvisor(80, "risk"));

        SpendingGuardianAgent.GuardianDecision decision = agent.evaluate(
                "OZON покупка",
                "ozon",
                new BigDecimal("3000"),
                true,
                defaultPrefs("STRICT")
        );

        assertEquals("APPROVED", decision.status());
    }

    @Test
    void shouldUseSelectedPersonaMessage() {
        SpendingGuardianAgent agent = new SpendingGuardianAgent(new FixedAdvisor(70, "risk"));

        SpendingGuardianAgent.GuardianDecision decision = agent.evaluate(
                "WB корзина",
                "wb",
                new BigDecimal("5000"),
                false,
                defaultPrefs("BOSS")
        );

        assertEquals("NEEDS_CONFIRMATION", decision.status());
        assertTrue(decision.message().contains("Требую обоснование"));
    }

    private SpendingGuardianAgent.GuardianPreferences defaultPrefs(String profile) {
        return new SpendingGuardianAgent.GuardianPreferences(
                true,
                true,
                new BigDecimal("50000"),
                "BETTING,CASINO",
                "OZON,WB,ВКУСНЯШКИ",
                profile
        );
    }

    private static class FixedAdvisor extends LlmSpendingAdvisor {
        private final int risk;
        private final String explanation;

        private FixedAdvisor(int risk, String explanation) {
            this.risk = risk;
            this.explanation = explanation;
        }

        @Override
        public Advice analyze(String title, String category, BigDecimal amount) {
            return new Advice(risk, explanation);
        }
    }
}
