package ru.bankpet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.bankpet.dto.NfcPurchaseRequestDto;
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
    public String dashboard(Model model,
                            @ModelAttribute("notice") String notice,
                            @ModelAttribute("showAgentPopup") Boolean showAgentPopup,
                            @ModelAttribute("confirmTitle") String confirmTitle,
                            @ModelAttribute("confirmAmount") BigDecimal confirmAmount,
                            @ModelAttribute("confirmCategory") String confirmCategory,
                            @ModelAttribute("confirmMessage") String confirmMessage,
                            @ModelAttribute("confirmSeverity") String confirmSeverity,
                            @ModelAttribute("confirmSourceType") String confirmSourceType) {
        UUID clientId = demoClientId();
        model.addAttribute("dashboard", service.getDashboard(clientId));
        model.addAttribute("filters", service.getFilterSettings(clientId));
        model.addAttribute("clientId", clientId);
        model.addAttribute("notice", notice);
        model.addAttribute("showAgentPopup", showAgentPopup);
        model.addAttribute("confirmTitle", confirmTitle);
        model.addAttribute("confirmAmount", confirmAmount);
        model.addAttribute("confirmCategory", confirmCategory);
        model.addAttribute("confirmMessage", confirmMessage);
        model.addAttribute("confirmSeverity", confirmSeverity);
        model.addAttribute("confirmSourceType", confirmSourceType);
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
    public String pay(@RequestParam BigDecimal amount,
                      @RequestParam String category,
                      @RequestParam(defaultValue = "BANK_APP_TOKEN") String deviceToken,
                      RedirectAttributes redirectAttributes) {
        String merchant = category;
        NfcPurchaseRequestDto request = new NfcPurchaseRequestDto(merchant, amount, category, deviceToken, null);
        PaymentDecisionDto result = service.processNfcPayment(demoClientId(), request);
        return handleDecision("NFC: " + merchant, amount, category, "NFC", result, redirectAttributes);
    }

    @PostMapping("/sync-history")
    public String syncHistory(RedirectAttributes redirectAttributes) {
        int synced = service.syncExternalHistory(demoClientId());
        redirectAttributes.addFlashAttribute("notice", "История из внешних банков синхронизирована: " + synced + " операций.");
        return "redirect:/app";
    }

    @PostMapping("/filters")
    public String updateFilters(@RequestParam(defaultValue = "false") boolean llmAgentEnabled,
                                @RequestParam(defaultValue = "false") boolean hardBlockEnabled,
                                @RequestParam BigDecimal confirmationThreshold,
                                @RequestParam String blockedCategoriesCsv,
                                @RequestParam String riskyCategoriesCsv,
                                @RequestParam String agentProfile,
                                RedirectAttributes redirectAttributes) {
        service.updateFilterSettings(
                demoClientId(),
                new SpendingFilterSettingsDto(
                        llmAgentEnabled,
                        hardBlockEnabled,
                        confirmationThreshold,
                        blockedCategoriesCsv,
                        riskyCategoriesCsv,
                        agentProfile
                )
        );
        redirectAttributes.addFlashAttribute("notice", "Настройки фильтров сохранены.");
        return "redirect:/app";
    }

    @PostMapping("/pay/confirm")
    public String confirmPay(@RequestParam String title,
                             @RequestParam BigDecimal amount,
                             @RequestParam String category,
                             @RequestParam String sourceType,
                             RedirectAttributes redirectAttributes) {
        PaymentDecisionDto result;
        if ("NFC".equalsIgnoreCase(sourceType)) {
            result = service.processNfcPayment(demoClientId(), new NfcPurchaseRequestDto(category, amount, category, "BANK_APP_TOKEN", true));
        } else {
            result = service.processPayment(demoClientId(), new PaymentRequestDto(title, amount, category, true));
        }
        redirectAttributes.addFlashAttribute("notice", "[" + result.severity() + "] " + result.message());
        return "redirect:/app";
    }

    @PostMapping("/pay/cancel")
    public String cancelPay(@RequestParam BigDecimal amount, RedirectAttributes redirectAttributes) {
        service.registerDeclinedImpulse(demoClientId(), amount);
        redirectAttributes.addFlashAttribute("notice", "Отлично! Вы отказались от импульсивной траты 👏");
        return "redirect:/app";
    }

    private String handleDecision(String title, BigDecimal amount, String category, String sourceType,
                                  PaymentDecisionDto result,
                                  RedirectAttributes redirectAttributes) {
        if ("NEEDS_CONFIRMATION".equals(result.status())) {
            redirectAttributes.addFlashAttribute("showAgentPopup", true);
            redirectAttributes.addFlashAttribute("confirmTitle", title);
            redirectAttributes.addFlashAttribute("confirmAmount", amount);
            redirectAttributes.addFlashAttribute("confirmCategory", category);
            redirectAttributes.addFlashAttribute("confirmMessage", result.message());
            redirectAttributes.addFlashAttribute("confirmSeverity", result.severity());
            redirectAttributes.addFlashAttribute("confirmSourceType", sourceType);
            return "redirect:/app";
        }
        redirectAttributes.addFlashAttribute("notice", "[" + result.severity() + "] " + result.message());
        return "redirect:/app";
    }

    private UUID demoClientId() {
        return clientRepository.findByPhone("+7-900-123-45-67")
                .orElseThrow(() -> new IllegalStateException("Demo client not found"))
                .getId();
    }
}
