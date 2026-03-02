package ru.bankpet.service;

import ru.bankpet.dto.*;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankDashboardService {

    BankDashboardDto getDashboard(UUID clientId);

    BankDashboardDto topUpDigitalRuble(UUID clientId, BigDecimal amount);

    BankDashboardDto linkDigitalRuble(UUID clientId);

    PaymentDecisionDto processPayment(UUID clientId, PaymentRequestDto request);

    PaymentDecisionDto processNfcPayment(UUID clientId, NfcPurchaseRequestDto request);

    int syncExternalHistory(UUID clientId);

    void registerDeclinedImpulse(UUID clientId);

    SpendingFilterSettingsDto getFilterSettings(UUID clientId);

    SpendingFilterSettingsDto updateFilterSettings(UUID clientId, SpendingFilterSettingsDto request);
}
