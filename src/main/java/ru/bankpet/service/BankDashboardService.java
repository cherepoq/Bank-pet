package ru.bankpet.service;

import ru.bankpet.dto.BankDashboardDto;
import ru.bankpet.dto.PaymentDecisionDto;
import ru.bankpet.dto.PaymentRequestDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankDashboardService {

    BankDashboardDto getDashboard(UUID clientId);

    BankDashboardDto topUpDigitalRuble(UUID clientId, BigDecimal amount);

    BankDashboardDto linkDigitalRuble(UUID clientId);

    PaymentDecisionDto processPayment(UUID clientId, PaymentRequestDto request);
}
