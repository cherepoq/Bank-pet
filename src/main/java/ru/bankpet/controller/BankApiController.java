package ru.bankpet.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.web.bind.annotation.*;
import ru.bankpet.dto.BankDashboardDto;
import ru.bankpet.dto.PaymentDecisionDto;
import ru.bankpet.dto.PaymentRequestDto;
import ru.bankpet.dto.SpendingFilterSettingsDto;
import ru.bankpet.service.BankDashboardService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients/{clientId}")
public class BankApiController {

    private final BankDashboardService dashboardService;

    public BankApiController(BankDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public BankDashboardDto dashboard(@PathVariable UUID clientId) {
        return dashboardService.getDashboard(clientId);
    }

    @PostMapping("/digital-ruble/link")
    public BankDashboardDto linkDigitalRuble(@PathVariable UUID clientId) {
        return dashboardService.linkDigitalRuble(clientId);
    }

    @PostMapping("/digital-ruble/top-up")
    public BankDashboardDto topUpDigitalRuble(@PathVariable UUID clientId,
                                              @RequestParam @DecimalMin("0.01") BigDecimal amount) {
        return dashboardService.topUpDigitalRuble(clientId, amount);
    }

    @PostMapping("/payments")
    public PaymentDecisionDto processPayment(@PathVariable UUID clientId,
                                             @Valid @RequestBody PaymentRequestDto request) {
        return dashboardService.processPayment(clientId, request);
    }

    @GetMapping("/spending-filters")
    public SpendingFilterSettingsDto getSpendingFilters(@PathVariable UUID clientId) {
        return dashboardService.getFilterSettings(clientId);
    }

    @PutMapping("/spending-filters")
    public SpendingFilterSettingsDto updateSpendingFilters(@PathVariable UUID clientId,
                                                            @Valid @RequestBody SpendingFilterSettingsDto request) {
        return dashboardService.updateFilterSettings(clientId, request);
    }
}
