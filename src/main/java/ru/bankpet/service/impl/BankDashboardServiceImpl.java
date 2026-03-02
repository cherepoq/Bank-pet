package ru.bankpet.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.bankpet.dto.BankDashboardDto;
import ru.bankpet.dto.PaymentDecisionDto;
import ru.bankpet.dto.PaymentRequestDto;
import ru.bankpet.dto.TransactionDto;
import ru.bankpet.entity.Account;
import ru.bankpet.entity.Card;
import ru.bankpet.entity.Client;
import ru.bankpet.entity.DigitalRubleWallet;
import ru.bankpet.entity.PaymentTransaction;
import ru.bankpet.repository.ClientRepository;
import ru.bankpet.repository.PaymentTransactionRepository;
import ru.bankpet.service.BankDashboardService;
import ru.bankpet.service.SpendingGuardianAgent;

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
    private final SpendingGuardianAgent guardianAgent;

    public BankDashboardServiceImpl(ClientRepository clientRepository,
                                    PaymentTransactionRepository transactionRepository,
                                    SpendingGuardianAgent guardianAgent) {
        this.clientRepository = clientRepository;
        this.transactionRepository = transactionRepository;
        this.guardianAgent = guardianAgent;
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
        Client client = getClient(clientId);
        Account account = client.getAccounts().getFirst();

        SpendingGuardianAgent.GuardianDecision decision =
                guardianAgent.evaluate(request.category(), request.amount(), request.confirmedByUser());

        if (!"APPROVED".equals(decision.status())) {
            return new PaymentDecisionDto(decision.status(), decision.message());
        }

        if (account.getBalance().compareTo(request.amount()) < 0) {
            return new PaymentDecisionDto("REJECTED", "Недостаточно средств на счёте.");
        }

        account.setBalance(account.getBalance().subtract(request.amount()));
        saveTransaction(client, request.title(), request.amount().negate(), "CARD", request.category());

        return new PaymentDecisionDto("APPROVED", "Платёж выполнен успешно.");
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
                .limit(5)
                .map(t -> new TransactionDto(t.getTitle(), t.getAmount(), t.getCreatedAt(), t.getSourceType(), t.getCategory()))
                .toList();

        return new BankDashboardDto(
                client.getFullName(),
                client.getPhone(),
                account.getBalance(),
                client.getDigitalRubleWallet().getBalance(),
                client.getDigitalRubleWallet().isLinked(),
                account.getCards().stream().map(Card::getMaskedPan).toList(),
                transactions
        );
    }
}
