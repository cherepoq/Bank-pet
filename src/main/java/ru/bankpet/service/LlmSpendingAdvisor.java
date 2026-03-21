package ru.bankpet.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;

@Component
public class LlmSpendingAdvisor {

    private static final Set<String> IMPULSIVE_KEYWORDS = Set.of(
            "iphone", "crypto", "ставка", "казино", "донат", "skin", "luxury", "premium", "ozon", "wb", "вкусняш"
    );

    public Advice analyze(String title, String category, BigDecimal amount) {
        String normalized = (title + " " + category).toLowerCase(Locale.ROOT);
        boolean hasImpulsivePattern = IMPULSIVE_KEYWORDS.stream().anyMatch(normalized::contains);

        int riskScore = 20;
        if (hasImpulsivePattern) riskScore += 35;
        if (amount.compareTo(new BigDecimal("7000")) > 0) riskScore += 12;
        if (amount.compareTo(new BigDecimal("30000")) > 0) riskScore += 18;
        if (amount.compareTo(new BigDecimal("100000")) > 0) riskScore += 15;

        String explanation = hasImpulsivePattern
                ? "LLM-агент увидел поведенческий паттерн маркетплейс/импульсивной траты."
                : "LLM-агент не видит явной импульсивной модели.";

        return new Advice(Math.min(riskScore, 100), explanation);
    }

    public record Advice(int riskScore, String explanation) {}
}
