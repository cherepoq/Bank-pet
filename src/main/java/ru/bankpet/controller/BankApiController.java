package ru.bankpet.controller;

import jakarta.validation.constraints.DecimalMin;
import org.springframework.web.bind.annotation.*;
import ru.bankpet.dto.BankDashboardDto;
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
}
