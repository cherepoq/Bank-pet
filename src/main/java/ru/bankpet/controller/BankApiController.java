package ru.bankpet.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.web.bind.annotation.*;
import ru.bankpet.dto.*;
import ru.bankpet.service.BankDashboardService;

import java.math.BigDecimal;
import java.util.Map;
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

    @PostMapping("/payments/nfc")
    public PaymentDecisionDto processNfcPayment(@PathVariable UUID clientId,
                                                @Valid @RequestBody NfcPurchaseRequestDto request) {
        return dashboardService.processNfcPayment(clientId, request);
    }

    @PostMapping("/history/sync")
    public Map<String, Object> syncHistory(@PathVariable UUID clientId) {
        int synced = dashboardService.syncExternalHistory(clientId);
        return Map.of("status", "OK", "synced", synced);
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
