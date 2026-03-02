package ru.bankpet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.bankpet.dto.PaymentDecisionDto;
import ru.bankpet.dto.PaymentRequestDto;
import ru.bankpet.dto.SpendingFilterSettingsDto;
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
    public String dashboard(Model model, @ModelAttribute("notice") String notice) {
        UUID clientId = demoClientId();
        model.addAttribute("dashboard", service.getDashboard(clientId));
        model.addAttribute("filters", service.getFilterSettings(clientId));
        model.addAttribute("clientId", clientId);
        model.addAttribute("notice", notice);
        return "dashboard";
    }

    @PostMapping("/link")
    public String link(RedirectAttributes redirectAttributes) {
        service.linkDigitalRuble(demoClientId());
        redirectAttributes.addFlashAttribute("notice", "Кошелёк цифрового рубля успешно привязан.");
        return "redirect:/app";
    }

    @PostMapping("/topup")
    public String topup(@RequestParam BigDecimal amount, RedirectAttributes redirectAttributes) {
        service.topUpDigitalRuble(demoClientId(), amount);
        redirectAttributes.addFlashAttribute("notice", "Кошелёк пополнен на " + amount + " ₽.");
        return "redirect:/app";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam String title,
                      @RequestParam BigDecimal amount,
                      @RequestParam String category,
                      RedirectAttributes redirectAttributes,
                      Model model) {
        PaymentRequestDto request = new PaymentRequestDto(title, amount, category, null);
        PaymentDecisionDto result = service.processPayment(demoClientId(), request);

        if ("NEEDS_CONFIRMATION".equals(result.status())) {
            model.addAttribute("confirmTitle", title);
            model.addAttribute("confirmAmount", amount);
            model.addAttribute("confirmCategory", category);
            model.addAttribute("confirmMessage", result.message());
            return "payment-confirm";
        }

        redirectAttributes.addFlashAttribute("notice", result.message());
        return "redirect:/app";
    }

    @PostMapping("/filters")
    public String updateFilters(@RequestParam(defaultValue = "false") boolean llmAgentEnabled,
                                @RequestParam(defaultValue = "false") boolean hardBlockEnabled,
                                @RequestParam BigDecimal confirmationThreshold,
                                @RequestParam String blockedCategoriesCsv,
                                @RequestParam String riskyCategoriesCsv,
                                RedirectAttributes redirectAttributes) {
        service.updateFilterSettings(
                demoClientId(),
                new SpendingFilterSettingsDto(
                        llmAgentEnabled,
                        hardBlockEnabled,
                        confirmationThreshold,
                        blockedCategoriesCsv,
                        riskyCategoriesCsv
                )
        );
        redirectAttributes.addFlashAttribute("notice", "Настройки фильтров сохранены.");
        return "redirect:/app";
    }

    @PostMapping("/pay/confirm")
    public String confirmPay(@RequestParam String title,
                             @RequestParam BigDecimal amount,
                             @RequestParam String category,
                             @RequestParam boolean confirmed,
                             RedirectAttributes redirectAttributes) {
        PaymentRequestDto request = new PaymentRequestDto(title, amount, category, confirmed);
        PaymentDecisionDto result = service.processPayment(demoClientId(), request);
        redirectAttributes.addFlashAttribute("notice", result.message());
        return "redirect:/app";
    }

    private UUID demoClientId() {
        return clientRepository.findByPhone("+7-900-123-45-67")
                .orElseThrow(() -> new IllegalStateException("Demo client not found"))
                .getId();
    }
}
