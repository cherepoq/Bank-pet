package ru.bankpet.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.bankpet.dto.*;
import ru.bankpet.entity.*;
import ru.bankpet.repository.ClientRepository;
import ru.bankpet.repository.PaymentTransactionRepository;
import ru.bankpet.repository.SpendingFilterSettingsRepository;
import ru.bankpet.service.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BankDashboardServiceImpl implements BankDashboardService {

    private final ClientRepository clientRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final SpendingFilterSettingsRepository filterSettingsRepository;
    private final SpendingGuardianAgent guardianAgent;
    private final NfcPaymentGateway nfcPaymentGateway;
    private final ExternalBankHistorySyncService externalHistorySyncService;

    public BankDashboardServiceImpl(ClientRepository clientRepository,
                                    PaymentTransactionRepository transactionRepository,
                                    SpendingFilterSettingsRepository filterSettingsRepository,
                                    SpendingGuardianAgent guardianAgent,
                                    NfcPaymentGateway nfcPaymentGateway,
                                    ExternalBankHistorySyncService externalHistorySyncService) {
        this.clientRepository = clientRepository;
        this.transactionRepository = transactionRepository;
        this.filterSettingsRepository = filterSettingsRepository;
        this.guardianAgent = guardianAgent;
        this.nfcPaymentGateway = nfcPaymentGateway;
        this.externalHistorySyncService = externalHistorySyncService;
    }

    @Override
    public BankDashboardDto getDashboard(UUID clientId) {
        Client client = getClient(clientId);
        return toDto(client);
    }

    @Override
    public BankDashboardDto topUpDigitalRuble(UUID clientId, BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Client client = getClient(clientId);
        Account account = client.getAccounts().getFirst();
        DigitalRubleWallet wallet = client.getDigitalRubleWallet();

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Not enough money on account");
        }
        if (!wallet.isLinked()) {
            throw new IllegalStateException("Digital ruble wallet is not linked");
        }

        account.setBalance(account.getBalance().subtract(amount));
        wallet.setBalance(wallet.getBalance().add(amount));

        saveTransaction(client, "Пополнение цифрового рубля", amount.negate(), "RUB_ACCOUNT", "DIGITAL_RUBLE");
        return toDto(client);
    }

    @Override
    public BankDashboardDto linkDigitalRuble(UUID clientId) {
        Client client = getClient(clientId);
        client.getDigitalRubleWallet().setLinked(true);
        return toDto(client);
    }

    @Override
    public PaymentDecisionDto processPayment(UUID clientId, PaymentRequestDto request) {
        return processPaymentInternal(clientId, request.title(), request.amount(), request.category(), request.confirmedByUser(), "CARD");
    }

    @Override
    public PaymentDecisionDto processNfcPayment(UUID clientId, NfcPurchaseRequestDto request) {
        NfcPaymentGateway.NfcAuthorization auth = nfcPaymentGateway.authorize(request);
        if (!auth.approved()) {
            return new PaymentDecisionDto("REJECTED", "HARD", auth.message());
        }

        return processPaymentInternal(
                clientId,
                "NFC: " + request.merchant(),
                request.amount(),
                request.category(),
                request.confirmedByUser(),
                "NFC"
        );
    }

    @Override
    public int syncExternalHistory(UUID clientId) {
        Client client = getClient(clientId);
        List<ExternalTransactionDto> externalTransactions = externalHistorySyncService.pullLatest();
        externalTransactions.forEach(t -> saveTransaction(
                client,
                "Импорт: " + t.title() + " [" + t.sourceBank() + "]",
                t.amount(),
                "OPEN_BANKING",
                t.category()
        ));
        return externalTransactions.size();
    }


    @Override
    public void registerDeclinedImpulse(UUID clientId) {
        Client client = getClient(clientId);
        SpendingFilterSettings settings = getOrCreateSettings(client);
        settings.setAvoidedImpulseCount(settings.getAvoidedImpulseCount() + 1);
        filterSettingsRepository.save(settings);
    }

    @Override
    public SpendingFilterSettingsDto getFilterSettings(UUID clientId) {
        SpendingFilterSettings settings = getOrCreateSettings(getClient(clientId));
        return toSettingsDto(settings);
    }

    @Override
    public SpendingFilterSettingsDto updateFilterSettings(UUID clientId, SpendingFilterSettingsDto request) {
        SpendingFilterSettings settings = getOrCreateSettings(getClient(clientId));
        settings.setLlmAgentEnabled(request.llmAgentEnabled());
        settings.setHardBlockEnabled(request.hardBlockEnabled());
        settings.setConfirmationThreshold(request.confirmationThreshold());
        settings.setBlockedCategoriesCsv(request.blockedCategoriesCsv());
        settings.setRiskyCategoriesCsv(request.riskyCategoriesCsv());
        filterSettingsRepository.save(settings);
        return toSettingsDto(settings);
    }

    private PaymentDecisionDto processPaymentInternal(UUID clientId, String title, BigDecimal amount,
                                                      String category, Boolean confirmedByUser, String sourceType) {
        Client client = getClient(clientId);
        Account account = client.getAccounts().getFirst();
        SpendingFilterSettings settings = getOrCreateSettings(client);

        SpendingGuardianAgent.GuardianDecision decision = guardianAgent.evaluate(
                title,
                category,
                amount,
                confirmedByUser,
                new SpendingGuardianAgent.GuardianPreferences(
                        settings.isLlmAgentEnabled(),
                        settings.isHardBlockEnabled(),
                        settings.getConfirmationThreshold(),
                        settings.getBlockedCategoriesCsv(),
                        settings.getRiskyCategoriesCsv()
                )
        );

        if (!"APPROVED".equals(decision.status())) {
            return new PaymentDecisionDto(decision.status(), decision.severity(), decision.message());
        }

        if (account.getBalance().compareTo(amount) < 0) {
            return new PaymentDecisionDto("REJECTED", "HARD", "Недостаточно средств на счёте.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        saveTransaction(client, title, amount.negate(), sourceType, category);

        return new PaymentDecisionDto("APPROVED", decision.severity(), "Платёж выполнен успешно.");
    }

    private SpendingFilterSettingsDto toSettingsDto(SpendingFilterSettings settings) {
        return new SpendingFilterSettingsDto(
                settings.isLlmAgentEnabled(),
                settings.isHardBlockEnabled(),
                settings.getConfirmationThreshold(),
                settings.getBlockedCategoriesCsv(),
                settings.getRiskyCategoriesCsv()
        );
    }

    private SpendingFilterSettings getOrCreateSettings(Client client) {
        return filterSettingsRepository.findByClientId(client.getId()).orElseGet(() -> {
            SpendingFilterSettings settings = new SpendingFilterSettings();
            settings.setClient(client);
            settings.setLlmAgentEnabled(true);
            settings.setHardBlockEnabled(true);
            settings.setConfirmationThreshold(new BigDecimal("50000.00"));
            settings.setBlockedCategoriesCsv("BETTING,SCAM,GAMBLING");
            settings.setRiskyCategoriesCsv("GAMES,ALCOHOL,LUXURY,CRYPTO,OZON,WB,ВКУСНЯШКИ,CASINO");
            settings.setAvoidedImpulseCount(0);
            return filterSettingsRepository.save(settings);
        });
    }

    private void saveTransaction(Client client, String title, BigDecimal amount, String sourceType, String category) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setClient(client);
        transaction.setAmount(amount);
        transaction.setSourceType(sourceType);
        transaction.setCategory(category);
        transaction.setTitle(title);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private Client getClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
    }

    private BankDashboardDto toDto(Client client) {
        Account account = client.getAccounts().getFirst();
        List<TransactionDto> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getClient().getId().equals(client.getId()))
                .sorted(Comparator.comparing(PaymentTransaction::getCreatedAt).reversed())
                .limit(8)
                .map(t -> new TransactionDto(t.getTitle(), t.getAmount(), t.getCreatedAt(), t.getSourceType(), t.getCategory()))
                .toList();

        return new BankDashboardDto(
                client.getFullName(),
                client.getPhone(),
                account.getBalance(),
                client.getDigitalRubleWallet().getBalance(),
                client.getDigitalRubleWallet().isLinked(),
                getOrCreateSettings(client).getAvoidedImpulseCount(),
                account.getCards().stream().map(Card::getMaskedPan).toList(),
                transactions
        );
    }
}
