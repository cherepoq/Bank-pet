package ru.bankpet.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.bankpet.dto.BankDashboardDto;
import ru.bankpet.dto.TransactionDto;
import ru.bankpet.entity.*;
import ru.bankpet.repository.ClientRepository;
import ru.bankpet.repository.PaymentTransactionRepository;
import ru.bankpet.service.BankDashboardService;

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

    public BankDashboardServiceImpl(ClientRepository clientRepository,
                                    PaymentTransactionRepository transactionRepository) {
        this.clientRepository = clientRepository;
        this.transactionRepository = transactionRepository;
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

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setClient(client);
        transaction.setAmount(amount.negate());
        transaction.setSourceType("RUB_ACCOUNT");
        transaction.setTitle("Пополнение цифрового рубля");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return toDto(client);
    }

    @Override
    public BankDashboardDto linkDigitalRuble(UUID clientId) {
        Client client = getClient(clientId);
        client.getDigitalRubleWallet().setLinked(true);
        return toDto(client);
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
                .map(t -> new TransactionDto(t.getTitle(), t.getAmount(), t.getCreatedAt(), t.getSourceType()))
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
