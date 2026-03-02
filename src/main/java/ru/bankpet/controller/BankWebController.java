package ru.bankpet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.bankpet.repository.ClientRepository;
import ru.bankpet.service.BankDashboardService;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/app")
public class BankWebController {

    private final BankDashboardService service;
    private final ClientRepository clientRepository;

    public BankWebController(BankDashboardService service, ClientRepository clientRepository) {
        this.service = service;
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        UUID clientId = demoClientId();
        model.addAttribute("dashboard", service.getDashboard(clientId));
        model.addAttribute("clientId", clientId);
        return "dashboard";
    }

    @PostMapping("/link")
    public String link() {
        service.linkDigitalRuble(demoClientId());
        return "redirect:/app";
    }

    @PostMapping("/topup")
    public String topup(@RequestParam BigDecimal amount) {
        service.topUpDigitalRuble(demoClientId(), amount);
        return "redirect:/app";
    }

    private UUID demoClientId() {
        return clientRepository.findByPhone("+7-900-123-45-67")
                .orElseThrow(() -> new IllegalStateException("Demo client not found"))
                .getId();
    }
}
